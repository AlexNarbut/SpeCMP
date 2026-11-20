package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

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
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_engine_start
import ru.alarmtrade.specmp.core.localization.resources.autostart_bypass_item_ignition_on
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_msg_bypass_done
import ru.alarmtrade.specmp.core.localization.resources.autostart_voice_msg_bypass_start
import ru.alarmtrade.specmp.core.speech.SpeechMode
import ru.alarmtrade.specmp.core.testing.FakeHandsFreeSettings
import ru.alarmtrade.specmp.core.testing.FakeSpeaker
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeBypassRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testAlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession

@OptIn(ExperimentalCoroutinesApi::class)
class BypassLearningViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository(
        testAlarmDevice.copy(
            features = setOf(DeviceFeature.CLONE),
            can = CanInfo("1.0", "VWPOLO20", isFactory = false),
        ),
    )
    private val repository = FakeBypassRepository()
    private val programMode = FakeProgramModeRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<BypassLearningViewModel>()
    private val speaker = FakeSpeaker()
    private val handsFree = FakeHandsFreeSettings(enabled = true)
    private val effects = mutableListOf<BypassEffect>()

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

    private fun TestScope.viewModel(subscribe: Boolean = true): BypassLearningViewModel {
        val dispatchers = testDispatchers(dispatcher)
        val session =
            testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.BLE)
        val entry = ProgramModeEntry(session, programMode, cooldown)
        val vm = BypassLearningViewModel(session, repository, entry, cooldown, dispatchers, speaker, handsFree).also {
            viewModels +=
                it
        }
        if (subscribe) backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private fun TestScope.started(): BypassLearningViewModel = viewModel().apply {
        onStartClick()
        runCurrent()
    }

    @Test
    fun `device info fills the start screen`() = test {
        val vm = viewModel()
        assertEquals("VWPOLO20", vm.state.value.canCode)
        assertTrue(vm.state.value.cloneAvailable)
        assertFalse(vm.state.value.usb)
    }

    @Test
    fun `start enters program mode then shows steps and result`() = test {
        val vm = started()
        assertEquals(1, programMode.calls)
        assertEquals(1, repository.runs)
        val step = BypassStep(BypassStepKind.ACTION, BypassSubject.IGNITION_ON)
        repository.send(BypassEvent.Step(step))
        runCurrent()
        assertEquals(BypassPhase.Running(step), vm.state.value.phase)
        repository.send(BypassEvent.Finished(BypassResult.CloneRequired))
        runCurrent()
        assertEquals(BypassPhase.Finished(BypassResult.CloneRequired), vm.state.value.phase)
    }

    @Test
    fun `silent wrong pin asks for pin without error`() = test {
        programMode.results = listOf(Response.Success(ProgramModeResult.WRONG_PIN))
        val vm = started()
        val dialog = assertIs<BypassDialog.Pin>(vm.state.value.dialog)
        assertFalse(dialog.pin.wrong)
        assertEquals(0, repository.runs)
    }

    @Test
    fun `entered pin is saved and learning starts`() = test {
        programMode.results =
            listOf(Response.Success(ProgramModeResult.WRONG_PIN), Response.Success(ProgramModeResult.ENTERED))
        val vm = started()
        // После неверного PIN блок 15 с не принимает ввод — ждём конца блокировки.
        time += 16.seconds
        vm.onPinSubmit("1234")
        runCurrent()
        assertEquals(1, sessionRepository.pins.size)
        assertEquals(1, repository.runs)
        assertEquals(null, vm.state.value.dialog)
    }

    @Test
    fun `program mode failure is a start error`() = test {
        programMode.results = listOf(Response.Error(IllegalStateException("no answer")))
        val vm = started()
        assertEquals(BypassPhase.Finished(BypassResult.Error(BypassError.START_FAILED)), vm.state.value.phase)
    }

    @Test
    fun `connection lost on a step returns to start with message`() = test {
        val vm = started()
        repository.send(BypassEvent.Step(BypassStep(BypassStepKind.WAIT, BypassSubject.ECU_EXCHANGE)))
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertEquals(BypassPhase.Start(interrupted = true), vm.state.value.phase)
    }

    @Test
    fun `back during learning asks and exit cancels`() = test {
        val vm = started()
        vm.onBack()
        runCurrent()
        assertEquals(BypassDialog.Exit, vm.state.value.dialog)
        assertTrue(effects.isEmpty())
        vm.onExitConfirm()
        runCurrent()
        assertEquals(BypassPhase.Start(), vm.state.value.phase)
    }

    @Test
    fun `retry runs learning again`() = test {
        val vm = started()
        repository.send(BypassEvent.Finished(BypassResult.Error(BypassError.GENERAL)))
        runCurrent()
        vm.onStartClick()
        runCurrent()
        assertEquals(2, repository.runs)
    }

    @Test
    fun `sdk failure on a step with live connection is a start error`() = test {
        val vm = started()
        repository.send(BypassEvent.Step(BypassStep(BypassStepKind.WAIT, BypassSubject.LEARNING)))
        repository.fail()
        runCurrent()
        assertEquals(BypassPhase.Finished(BypassResult.Error(BypassError.START_FAILED)), vm.state.value.phase)
        vm.onStartClick()
        runCurrent()
        assertEquals(2, repository.runs)
    }

    @Test
    fun `sdk failure after connection loss keeps the interrupted start`() = test {
        val vm = started()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        repository.fail()
        runCurrent()
        assertEquals(BypassPhase.Start(interrupted = true), vm.state.value.phase)
    }

    @Test
    fun `back on start leaves at once`() = test {
        val vm = viewModel()
        vm.onBack()
        runCurrent()
        assertEquals(listOf<BypassEffect>(BypassEffect.Close), effects)
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `pin dialog shows the lock countdown`() = test {
        programMode.results = listOf(Response.Success(ProgramModeResult.WRONG_PIN))
        val vm = started()
        val dialog = assertIs<BypassDialog.Pin>(vm.state.value.dialog)
        assertEquals(15.seconds, dialog.pin.lockedFor)
    }

    @Test
    fun `learning goes on while the screen is not subscribed`() = test {
        val vm = viewModel(subscribe = false)
        val screen = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        vm.onStartClick()
        runCurrent()
        screen.cancel()
        advanceTimeBy(10.seconds)
        val step = BypassStep(BypassStepKind.ACTION, BypassSubject.IGNITION_ON)
        repository.send(BypassEvent.Step(step))
        runCurrent()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        assertEquals(BypassPhase.Running(step), vm.state.value.phase)
        assertEquals(1, repository.runs)
    }

    @Test
    fun `steps and result are spoken while voice guidance is on`() = test {
        val expected = listOf(
            text(Strings.string.autostart_voice_msg_bypass_start) to SpeechMode.FLUSH,
            text(Strings.string.autostart_bypass_item_ignition_on) to SpeechMode.FLUSH,
            text(Strings.string.autostart_voice_msg_bypass_done) to SpeechMode.FLUSH,
        )
        val vm = started()
        val action = BypassStep(BypassStepKind.ACTION, BypassSubject.IGNITION_ON)
        repository.send(BypassEvent.Step(action))
        runCurrent()
        repository.send(BypassEvent.Finished(BypassResult.Success))
        runCurrent()

        assertEquals(expected, speaker.phrases)
        assertEquals(BypassPhase.Finished(BypassResult.Success), vm.state.value.phase)
    }

    @Test
    fun `nothing is spoken while voice guidance is off and turning it on speaks the current step`() = test {
        val expected = listOf(text(Strings.string.autostart_bypass_item_engine_start))
        handsFree.setEnabled(false)
        started()
        repository.send(BypassEvent.Step(BypassStep(BypassStepKind.ACTION, BypassSubject.ENGINE_START)))
        runCurrent()
        assertEquals(emptyList(), speaker.spoken)

        handsFree.setEnabled(true)
        runCurrent()
        assertEquals(expected, speaker.spoken)
    }

    @Test
    fun `turning voice guidance off stops speech`() = test {
        started()
        val stops = speaker.stops
        handsFree.setEnabled(false)
        runCurrent()
        assertEquals(stops + 1, speaker.stops)
    }

    @Test
    fun `leaving the screen stops speech`() = test {
        val store = ViewModelStore()
        store.put("bypass", viewModel())
        store.clear()
        assertEquals(1, speaker.stops)
    }

    // Заодно греет кэш строк: озвучка в VM берёт их из него без переключения потока.
    private fun text(resource: StringResource) = runBlocking { getString(resource) }
}
