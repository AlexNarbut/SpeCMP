package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneStage
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeCloneRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

@OptIn(ExperimentalCoroutinesApi::class)
class CloneViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeSessionRepository()
    private val repository = FakeCloneRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<CloneViewModel>()
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
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        session.attach(ConnectionTransport.BLE)
        val vm = CloneViewModel(session, repository, cooldown, dispatchers, fromBypass).also { viewModels += it }
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
        repository.attempts = null
        val vm = viewModel()
        assertEquals(CloneAttempts.Unknown, vm.state.value.attempts)
        vm.onStartClick()
        runCurrent()
        assertEquals(CloneDialog.Confirm, vm.state.value.dialog)
    }

    @Test
    fun `zero attempts block start`() = test {
        repository.attempts = 0
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
        repository.attempts = 2
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
        repository.send(CloneEvent.WrongPin)
        runCurrent()
        val dialog = assertIs<CloneDialog.Pin>(vm.state.value.dialog)
        assertFalse(dialog.pin.wrong, "silent SDK attempt is not a wrong entered PIN")
    }

    @Test
    fun `sdk failure with live connection is an unknown error`() = test {
        val vm = started()
        repository.send(CloneEvent.Failure)
        runCurrent()
        val result = assertIs<CloneResult.Failed>(assertIs<ClonePhase.Finished>(vm.state.value.phase).result)
        assertEquals(CloneFailure.UNKNOWN, result.failure)
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
        repository.send(CloneEvent.WrongPin)
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
}
