package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStep
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassStepKind
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassSubject
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.autostart.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeBypassRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeProgramModeRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.device.autostart.ui.testDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

@OptIn(ExperimentalCoroutinesApi::class)
class BypassLearningViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeSessionRepository(
        testDevice.copy(features = setOf(DeviceFeature.CLONE), can = CanInfo("1.0", "VWPOLO20", isFactory = false))
    )
    private val repository = FakeBypassRepository()
    private val programMode = FakeProgramModeRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<BypassLearningViewModel>()

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

    private fun TestScope.viewModel(): BypassLearningViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        session.attach(ConnectionTransport.BLE)
        val entry = ProgramModeEntry(session, programMode, cooldown)
        val vm = BypassLearningViewModel(session, repository, entry, cooldown, dispatchers).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
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
        assertFalse(vm.onBackRequest())
        runCurrent()
        assertEquals(BypassDialog.Exit, vm.state.value.dialog)
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
}
