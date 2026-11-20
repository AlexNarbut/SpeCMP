package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_preparing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_item_stage_writing
import ru.alarmtrade.specmp.core.localization.resources.autostart_clone_title_success
import ru.alarmtrade.specmp.core.localization.resources.autostart_status_label_critical
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_format_clone_stage
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.core.testing.FakeHandsFreeSettings
import ru.alarmtrade.specmp.core.testing.FakeSpeaker
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeCloneRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession

@OptIn(ExperimentalCoroutinesApi::class)
class CloneViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository()
    private val repository = FakeCloneRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<CloneViewModel>()
    private val speaker = FakeSpeaker()
    private val handsFree = FakeHandsFreeSettings(enabled = true)
    private val effects = mutableListOf<CloneEffect>()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.viewModel(fromBypass: Boolean = false, subscribe: Boolean = true): CloneViewModel {
        val dispatchers = testDispatchers(dispatcher)
        val session =
            testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.BLE)
        val vm = CloneViewModel(session, repository, cooldown, dispatchers, speaker, handsFree, fromBypass).also {
            viewModels +=
                it
        }
        if (subscribe) backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private fun TestScope.started(): CloneViewModel = viewModel().apply {
        onStartClick()
        onConfirm()
        runCurrent()
    }

    @Test
    fun `attempts are read on device ready`() = test {
        val vm = viewModel()
        assertEquals(CloneAttempts.Known(3), vm.state.value.attempts)
        assertEquals(77L, vm.state.value.deviceId)
    }

    @Test
    fun `unknown attempts still allow start`() = test {
        repository.attempts = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()
        assertEquals(CloneAttempts.Unknown, vm.state.value.attempts)
        vm.onStartClick()
        runCurrent()
        assertEquals(CloneDialog.Confirm, vm.state.value.dialog)
    }

    @Test
    fun `zero attempts block start`() = test {
        repository.attempts = Response.Success(0)
        val vm = viewModel()
        vm.onStartClick()
        assertNull(vm.state.value.dialog)
        assertFalse(vm.state.value.canStart)
    }

    @Test
    fun `start asks for confirmation then runs stages`() = test {
        val vm = viewModel()
        vm.onStartClick()
        runCurrent()
        assertEquals(CloneDialog.Confirm, vm.state.value.dialog)
        vm.onConfirm()
        runCurrent()
        assertEquals(1, repository.runs)
        repository.send(CloneEvent.Stage(CloneStage.CALCULATING, inQueue = true))
        runCurrent()
        assertEquals(ClonePhase.Running(CloneStage.CALCULATING, inQueue = true), vm.state.value.phase)
    }

    @Test
    fun `repeated confirm does not restart the running procedure`() = test {
        val vm = viewModel()
        vm.onStartClick()
        runCurrent()
        vm.onConfirm()
        vm.onConfirm()
        runCurrent()
        assertEquals(1, repository.runs)
    }

    @Test
    fun `result refreshes attempts`() = test {
        val vm = started()
        repository.attempts = Response.Success(2)
        repository.send(CloneEvent.Finished(CloneResult.Completed(already = false)))
        runCurrent()
        assertEquals(ClonePhase.Finished(CloneResult.Completed(already = false)), vm.state.value.phase)
        assertEquals(CloneAttempts.Known(2), vm.state.value.attempts)
    }

    @Test
    fun `exit is locked while writing`() = test {
        val vm = started()
        repository.send(CloneEvent.Stage(CloneStage.WRITING))
        runCurrent()
        assertTrue(vm.state.value.locked)
        vm.onBack()
        runCurrent()
        assertNull(vm.state.value.dialog)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `exit before writing asks and cancels`() = test {
        val vm = started()
        repository.send(CloneEvent.Stage(CloneStage.READING))
        runCurrent()
        vm.onBack()
        runCurrent()
        assertEquals(CloneDialog.Exit, vm.state.value.dialog)
        assertTrue(effects.isEmpty())
        vm.onExitConfirm()
        runCurrent()
        assertIs<ClonePhase.Start>(vm.state.value.phase)
    }

    @Test
    fun `connection lost before calculation keeps the attempt`() = test {
        val vm = started()
        repository.send(CloneEvent.Stage(CloneStage.READING))
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertEquals(ClonePhase.Start(CloneInterruption.ATTEMPT_KEPT), vm.state.value.phase)
    }

    @Test
    fun `connection lost after calculation asks to check attempts`() = test {
        val vm = started()
        repository.send(CloneEvent.Stage(CloneStage.WRITING))
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertEquals(ClonePhase.Start(CloneInterruption.CHECK_ATTEMPTS), vm.state.value.phase)
    }

    @Test
    fun `wrong pin shows pin dialog and reruns with entered pin`() = test {
        val vm = started()
        repository.fail(CloneProcedureFailure.WRONG_PIN)
        runCurrent()
        val dialog = assertIs<CloneDialog.Pin>(vm.state.value.dialog)
        assertFalse(dialog.pin.wrong, "silent SDK attempt is not a wrong entered PIN")
    }

    @Test
    fun `sdk failure with live connection is an unknown error`() = test {
        val vm = started()
        repository.fail(CloneProcedureFailure.UNKNOWN)
        runCurrent()
        val result = assertIs<CloneResult.Failed>(assertIs<ClonePhase.Finished>(vm.state.value.phase).result)
        assertEquals(CloneFailure.UNKNOWN, result.failure)
    }

    @Test
    fun `retry after an sdk failure runs the procedure again`() = test {
        val vm = started()
        repository.send(CloneEvent.Stage(CloneStage.READING))
        repository.fail(CloneProcedureFailure.UNKNOWN)
        runCurrent()
        assertIs<ClonePhase.Finished>(vm.state.value.phase)
        vm.onStartClick()
        vm.onConfirm()
        runCurrent()
        assertEquals(2, repository.runs)
        assertEquals(ClonePhase.Running(CloneStage.PREPARING), vm.state.value.phase)
    }

    @Test
    fun `back on start leaves at once`() = test {
        val vm = viewModel()
        vm.onBack()
        runCurrent()
        assertEquals(listOf<CloneEffect>(CloneEffect.Close), effects)
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `entered pin is saved and the procedure reruns`() = test {
        val vm = started()
        repository.fail(CloneProcedureFailure.WRONG_PIN)
        runCurrent()
        assertEquals(15.seconds, assertIs<CloneDialog.Pin>(vm.state.value.dialog).pin.lockedFor)
        time += 16.seconds
        vm.onPinSubmit("1234")
        runCurrent()
        assertEquals(1, sessionRepository.pins.size)
        assertEquals(2, repository.runs)
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `procedure goes on while the screen is not subscribed`() = test {
        val vm = viewModel(subscribe = false)
        val screen = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        vm.onStartClick()
        vm.onConfirm()
        runCurrent()
        screen.cancel()
        advanceTimeBy(10.seconds)
        repository.send(CloneEvent.Stage(CloneStage.READING))
        runCurrent()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        assertEquals(ClonePhase.Running(CloneStage.READING), vm.state.value.phase)
        assertEquals(1, repository.runs)
    }

    @Test
    fun `stages and result are spoken while voice guidance is on`() = test {
        val writing = text(Strings.string.autostart_clone_item_stage_writing)
        val expected = listOf(
            text(Strings.string.autostart_voice_format_clone_stage, 1, 5, preparing()) to SpeechMode.FLUSH,
            text(Strings.string.autostart_voice_format_clone_stage, 4, 5, writing) to SpeechMode.FLUSH,
            text(Strings.string.autostart_status_label_critical) to SpeechMode.QUEUE,
            text(Strings.string.autostart_clone_title_success) to SpeechMode.FLUSH,
        )
        started()
        repository.send(CloneEvent.Stage(CloneStage.WRITING))
        runCurrent()
        repository.send(CloneEvent.Finished(CloneResult.Completed(already = false)))
        runCurrent()

        assertEquals(expected, speaker.phrases)
    }

    @Test
    fun `clone is silent while voice guidance is off`() = test {
        handsFree.setEnabled(false)
        started()
        repository.send(CloneEvent.Stage(CloneStage.READING))
        runCurrent()
        assertEquals(emptyList(), speaker.spoken)
    }

    @Test
    fun `leaving the clone screen stops speech`() = test {
        val store = ViewModelStore()
        store.put("clone", viewModel())
        store.clear()
        assertEquals(1, speaker.stops)
    }

    private fun preparing() = text(Strings.string.autostart_clone_item_stage_preparing)

    // Заодно греет кэш строк: озвучка в VM берёт их из него без переключения потока.
    private fun text(resource: StringResource, vararg args: Any) = runBlocking { getString(resource, *args) }
}
