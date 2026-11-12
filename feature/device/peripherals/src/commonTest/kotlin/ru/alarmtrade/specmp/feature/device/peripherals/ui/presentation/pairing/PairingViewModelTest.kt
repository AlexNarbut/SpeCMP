package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.FakeDynamicPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.ui.slot

@OptIn(ExperimentalCoroutinesApi::class)
class PairingViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository()
    private val repository = FakeDynamicPeripheralsRepository()
    private val programMode = FakeProgramModeRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<PairingViewModel>()
    private val effects = mutableListOf<PairingEffect>()

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

    private fun TestScope.viewModel(): PairingViewModel {
        val session = testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.BLE)
        val entry = ProgramModeEntry(session, programMode, cooldown)
        val vm = PairingViewModel(session, repository, entry, cooldown, testDispatchers(dispatcher))
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private fun TestScope.table(
        vararg slots: ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral,
    ) {
        repository.emit(Response.Success(slots.toList()))
        runCurrent()
    }

    @Test
    fun `enters program mode, then reads the table`() = test {
        val vm = viewModel()
        assertEquals(1, programMode.calls)
        assertEquals(PairingPhase.READING, vm.state.value.phase)

        table(slot(0, paired = true), slot(1))
        assertEquals(PairingPhase.READY, vm.state.value.phase)
        assertEquals(1, vm.state.value.occupied)
    }

    @Test
    fun `refused program mode shows the refused screen`() = test {
        programMode.results = listOf(Response.Error(IllegalStateException("armed")))
        val vm = viewModel()

        assertEquals(PairingPhase.REFUSED, vm.state.value.phase)
        assertEquals(0, repository.subscriptions)
    }

    @Test
    fun `wrong saved pin asks for pin and reads after the right one`() = test {
        programMode.results =
            listOf(Response.Success(ProgramModeResult.WRONG_PIN), Response.Success(ProgramModeResult.ENTERED))
        val vm = viewModel()
        assertNotNull(vm.state.value.pin)

        time += 20.seconds
        vm.onPinSubmit("1234")
        runCurrent()

        assertNull(vm.state.value.pin)
        assertEquals(1, repository.subscriptions)
    }

    @Test
    fun `pairing succeeds when the slot becomes paired`() = test {
        val vm = viewModel()
        table(slot(0), slot(1))

        vm.onPair(1)
        runCurrent()
        assertEquals(listOf("pair 1 null"), repository.calls)
        assertEquals(PairingOutcome.WAITING, vm.state.value.pairing?.outcome)

        advanceTimeBy(5.seconds)
        table(slot(0), slot(1, paired = true))
        assertEquals(PairingOutcome.SUCCESS, vm.state.value.pairing?.outcome)
        assertEquals(1, vm.state.value.flash?.slot)
    }

    @Test
    fun `pairing fails when time is up`() = test {
        val vm = viewModel()
        table(slot(0))

        vm.onPair(0)
        runCurrent()
        advanceTimeBy((PAIRING_SECONDS + 1).seconds)
        runCurrent()

        assertEquals(PairingOutcome.FAILED, vm.state.value.pairing?.outcome)
        assertEquals(0, vm.state.value.pairing?.secondsLeft)
    }

    @Test
    fun `tire sensor needs a valid address`() = test {
        val vm = viewModel()
        table(slot(4, type = PeripheralType.TPMS))

        vm.onPair(4)
        runCurrent()
        assertEquals(SlotSheet.ADDRESS, vm.state.value.sheet)

        vm.onAddressChange("3b0z19")
        runCurrent()
        vm.onAddressSubmit()
        runCurrent()
        assertTrue(vm.state.value.addressError)
        assertTrue(repository.calls.isEmpty())

        vm.onAddressChange("3b0019f5")
        runCurrent()
        vm.onAddressSubmit()
        runCurrent()
        assertEquals(listOf("pair 4 3B0019F5"), repository.calls)
    }

    @Test
    fun `delete reports after the slot is freed`() = test {
        val vm = viewModel()
        table(slot(0, paired = true))

        vm.onDelete(0)
        runCurrent()
        vm.onDeleteConfirm()
        runCurrent()
        assertEquals(setOf(0), vm.state.value.deleting)
        assertTrue(effects.isEmpty())

        table(slot(0))
        assertTrue(vm.state.value.deleting.isEmpty())
        assertEquals(PairingEffect.Snackbar(PairingMessage.DELETED), effects.last())
    }

    @Test
    fun `unsupported command shows a dialog`() = test {
        repository.reply = Response.Error(PeripheralException(PeripheralFailure.UNSUPPORTED))
        val vm = viewModel()
        table(slot(0, paired = true))

        vm.onUpdate(0)
        runCurrent()

        assertEquals(PairingDialog.Unsupported, vm.state.value.dialog)
    }

    @Test
    fun `cancel only hides the pairing card`() = test {
        val vm = viewModel()
        table(slot(0))
        vm.onPair(0)
        runCurrent()

        vm.onPairingCancel()
        runCurrent()

        assertNull(vm.state.value.pairing)
        assertEquals(0, programMode.exits)
        assertEquals(1, repository.subscriptions)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `read error with data resubscribes quietly`() = test {
        val vm = viewModel()
        table(slot(0))

        repository.emit(Response.Error(IllegalStateException("timeout")))
        runCurrent()
        advanceTimeBy(3.seconds)
        runCurrent()

        assertEquals(PairingPhase.READY, vm.state.value.phase)
        assertEquals(2, repository.subscriptions)
    }
}
