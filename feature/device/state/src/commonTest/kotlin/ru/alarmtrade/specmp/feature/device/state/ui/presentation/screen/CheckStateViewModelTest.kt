package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.device.state.ui.FakeDeviceStateRepository
import ru.alarmtrade.specmp.feature.device.state.ui.FakeProgramModeRepository
import ru.alarmtrade.specmp.feature.device.state.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.device.state.ui.vehicle

@OptIn(ExperimentalCoroutinesApi::class)
class CheckStateViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeSessionRepository()
    private val repository = FakeDeviceStateRepository()
    private val programMode = FakeProgramModeRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<CheckStateViewModel>()
    private val effects = mutableListOf<CheckStateEffect>()
    private var collector: Job? = null

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

    private fun TestScope.viewModel(fastMount: Boolean = false): CheckStateViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        session.attach(ConnectionTransport.BLE)
        val entry = ProgramModeEntry(session, programMode, cooldown)
        val vm = CheckStateViewModel(session, repository, entry, programMode, cooldown, dispatchers, fastMount, time)
            .also { viewModels += it }
        collector = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private fun TestScope.emit(state: VehicleState) {
        repository.emit(state)
        runCurrent()
    }

    @Test
    fun `waits for the first snapshot without zeros`() = test {
        val vm = viewModel()
        assertTrue(vm.state.value.loading)
        assertEquals(1, repository.active)

        emit(vehicle())
        assertEquals(SecurityState.DISARMED, vm.state.value.vehicle?.security)
        assertEquals(1, vm.state.value.tick)
    }

    @Test
    fun `marks zones changed since the previous snapshot until the next one`() = test {
        val vm = viewModel()
        emit(vehicle())
        assertTrue(vm.state.value.changedZones.isEmpty())

        emit(vehicle(zones = mapOf(Zone.DOOR_FRONT_LEFT to ZoneValue.ON)))
        assertEquals(setOf(Zone.DOOR_FRONT_LEFT), vm.state.value.changedZones)

        emit(vehicle(zones = mapOf(Zone.DOOR_FRONT_LEFT to ZoneValue.ON)))
        assertTrue(vm.state.value.changedZones.isEmpty())
    }

    @Test
    fun `flashes the guard status when security changes`() = test {
        val vm = viewModel()
        emit(vehicle())
        emit(vehicle(security = SecurityState.ARMED))
        assertTrue(vm.state.value.guardChanged)
    }

    @Test
    fun `reports stale data after ten seconds without snapshots`() = test {
        val vm = viewModel()
        emit(vehicle())
        assertNull(vm.state.value.staleSeconds)

        time += 11.seconds
        advanceTimeBy(1_100)
        runCurrent()
        assertEquals(11, vm.state.value.staleSeconds)

        emit(vehicle())
        advanceTimeBy(1_100)
        runCurrent()
        assertNull(vm.state.value.staleSeconds)
    }

    @Test
    fun `stops polling when the screen stops observing`() = test {
        viewModel()
        assertEquals(1, repository.active)

        collector?.cancel()
        advanceTimeBy(6_000)
        runCurrent()
        assertEquals(0, repository.active)
    }

    @Test
    fun `restarts polling after the unit reconnects`() = test {
        viewModel()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertEquals(0, repository.active)

        sessionRepository.link.value = DeviceLinkState.READY
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(2, repository.subscriptions)
        assertEquals(1, repository.active)
    }

    @Test
    fun `sends a command and reports the result`() = test {
        val vm = viewModel()
        emit(vehicle())

        vm.onCommand(StateCommand.ARM)
        runCurrent()
        assertEquals(listOf(StateCommand.ARM), repository.sent)
        assertNull(vm.state.value.pending)
        assertIs<CommandStatus.Finished>(vm.state.value.status(StateCommand.ARM))
        assertEquals(CheckStateEffect.CommandFinished(StateCommand.ARM, CommandOutcome.DONE), effects.last())

        advanceTimeBy(4_000)
        runCurrent()
        assertEquals(CommandStatus.Idle, vm.state.value.status(StateCommand.ARM))
    }

    @Test
    fun `rejected command is reported as rejected`() = test {
        repository.reply = Response.Success(CommandReply.REJECTED)
        val vm = viewModel()
        emit(vehicle())

        vm.onCommand(StateCommand.OPEN_TRUNK)
        runCurrent()
        assertEquals(CheckStateEffect.CommandFinished(StateCommand.OPEN_TRUNK, CommandOutcome.REJECTED), effects.last())
    }

    @Test
    fun `dangerous commands ask for confirmation first`() = test {
        val vm = viewModel()
        emit(vehicle())

        vm.onCommand(StateCommand.START_ENGINE)
        runCurrent()
        assertEquals(CheckStateDialog.Confirm(StateCommand.START_ENGINE), vm.state.value.dialog)
        assertTrue(repository.sent.isEmpty())

        vm.onConfirm()
        runCurrent()
        assertEquals(listOf(StateCommand.START_ENGINE), repository.sent)
    }

    @Test
    fun `commands follow the engine state`() = test {
        val vm = viewModel()
        emit(vehicle())
        val stopped = vm.state.value.commands.map { it.command }
        assertTrue(StateCommand.START_ENGINE in stopped && StateCommand.STOP_ENGINE !in stopped)

        emit(vehicle(engine = EngineInfo(EngineStatus.RUNNING, 80, 900, Autostart.Running(300))))
        val running = vm.state.value.commands.map { it.command }
        assertTrue(StateCommand.STOP_ENGINE in running && StateCommand.EXTEND_AUTOSTART in running)
        assertTrue(StateCommand.START_ENGINE !in running)
    }

    @Test
    fun `fast mount has no programming mode command`() = test {
        val vm = viewModel(fastMount = true)
        emit(vehicle())
        assertTrue(vm.state.value.commands.none { it.command == StateCommand.PROGRAM_MODE_ON })
    }

    @Test
    fun `fast mount takes the unit out of programming mode once`() = test {
        val vm = viewModel(fastMount = true)
        emit(vehicle(programMode = true))
        emit(vehicle(programMode = true))
        assertEquals(1, programMode.exits)
        assertTrue(vm.state.value.programExiting)
    }

    @Test
    fun `back with service mode on warns before leaving`() = test {
        val vm = viewModel()
        emit(vehicle(serviceMode = true))

        assertEquals(false, vm.onBackRequest())
        runCurrent()
        assertEquals(CheckStateDialog.Exit(service = true, program = false), vm.state.value.dialog)

        vm.onExitTurnOff()
        runCurrent()
        assertEquals(listOf(StateCommand.SERVICE_MODE_OFF), repository.sent)
        assertEquals(CheckStateEffect.Close, effects.last())
    }

    @Test
    fun `back without modes leaves at once`() = test {
        val vm = viewModel()
        emit(vehicle())
        assertTrue(vm.onBackRequest())
    }

    @Test
    fun `fast mount next with open tasks asks to confirm`() = test {
        val vm = viewModel(fastMount = true)
        vm.onNext(notDone = 2, total = 5)
        runCurrent()
        assertEquals(CheckStateDialog.FastFinish(2, 5), vm.state.value.dialog)

        vm.onFinishConfirm()
        runCurrent()
        assertEquals(CheckStateEffect.Next, effects.last())
    }

    @Test
    fun `lost connection during a command marks it as no link`() = test {
        val vm = viewModel()
        emit(vehicle())
        vm.onCommand(StateCommand.ARM)
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        assertNull(vm.state.value.pending)
    }
}
