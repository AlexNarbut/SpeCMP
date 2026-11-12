package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

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
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ProgramModeEntry
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeProgramModeRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testAlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralFailure
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralPollStep
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.FakeSystemPeripheralsRepository
import ru.alarmtrade.specmp.feature.device.peripherals.ui.peripheral

@OptIn(ExperimentalCoroutinesApi::class)
class SystemDevicesViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeDeviceSessionRepository()
    private val repository = FakeSystemPeripheralsRepository()
    private val programMode = FakeProgramModeRepository()
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val viewModels = mutableListOf<SystemDevicesViewModel>()
    private val effects = mutableListOf<SystemDevicesEffect>()

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

    private fun TestScope.viewModel(): SystemDevicesViewModel {
        val session = testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = ConnectionTransport.BLE)
        val entry = ProgramModeEntry(session, programMode, cooldown)
        val vm = SystemDevicesViewModel(session, repository, entry, cooldown, testDispatchers(dispatcher))
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.sideEffect.collect { effects += it } }
        runCurrent()
        return vm
    }

    private fun TestScope.step(
        type: PeripheralType,
        index: Int,
        total: Int,
        vararg devices: ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral?,
    ) {
        val list = if (devices.size == 1 && devices[0] == null) null else devices.filterNotNull()
        repository.emit(Response.Success(PeripheralPollStep(type, index, total, list)))
        runCurrent()
    }

    @Test
    fun `first pass fills groups in catalog order and reports progress`() = test {
        val vm = viewModel()
        assertEquals(1, repository.polls)
        val total = repository.types.size
        assertEquals(PeripheralType.TAG, vm.state.value.pollingType)

        step(PeripheralType.TAG, 0, total, peripheral(number = 1), peripheral(number = 2))
        assertEquals(1, vm.state.value.polled)
        assertEquals(PeripheralType.KEYFOB, vm.state.value.pollingType)
        assertFalse(vm.state.value.firstPassDone)

        step(PeripheralType.RELAY, total - 1, total, peripheral(type = PeripheralType.RELAY))
        val state = vm.state.value
        assertTrue(state.firstPassDone)
        assertNull(state.pollingType)
        assertEquals(listOf(PeripheralType.TAG, PeripheralType.RELAY), state.groups.map { it.type })
        assertEquals("DX4G", state.deviceName)
        assertEquals("1.14", state.bluetoothVersion)
    }

    @Test
    fun `type without answer keeps devices from the previous pass`() = test {
        val vm = viewModel()
        val total = repository.types.size
        step(PeripheralType.TAG, 0, total, peripheral())
        step(PeripheralType.TAG, 0, total, null)

        assertEquals(1, vm.state.value.devices.size)
    }

    @Test
    fun `poll error before data shows retry and retry polls again`() = test {
        val vm = viewModel()
        repository.emit(Response.Error(IllegalStateException("no link")))
        runCurrent()
        assertTrue(vm.state.value.pollFailed)

        vm.onRetry()
        runCurrent()
        assertFalse(vm.state.value.pollFailed)
        assertEquals(2, repository.polls)
    }

    @Test
    fun `show problem highlights offline device first`() = test {
        val vm = viewModel()
        val total = repository.types.size
        val far = peripheral(number = 1, link = PeripheralLink.FAR)
        val offline = peripheral(type = PeripheralType.RELAY, link = PeripheralLink.OFFLINE, signalDbm = null)
        step(PeripheralType.TAG, 0, total, far)
        step(PeripheralType.RELAY, total - 1, total, offline)

        vm.onShowProblem()

        runCurrent()

        assertEquals(offline.key, vm.state.value.highlight?.key)
        assertEquals(offline.key, vm.state.value.selected)
    }

    @Test
    fun `blink reports and resets after a while`() = test {
        val vm = viewModel()
        val tag = peripheral()
        step(PeripheralType.TAG, 0, repository.types.size, tag)

        vm.onBlink(tag.key)
        runCurrent()
        assertEquals(tag.key, vm.state.value.blinking)
        assertEquals(SystemDevicesEffect.Blinking(PeripheralType.TAG, 1), effects.last())

        advanceTimeBy(4.seconds)
        runCurrent()
        assertNull(vm.state.value.blinking)
        assertEquals(listOf("blink ${tag.address}"), repository.calls)
    }

    @Test
    fun `firmware mode enters program mode, then closes the session`() = test {
        val vm = viewModel()
        val tag = peripheral()
        step(PeripheralType.TAG, 0, repository.types.size, tag)

        vm.onUpdate(tag.key)

        runCurrent()
        assertIs<FirmwareStep.Confirm>(vm.state.value.firmware)
        vm.onFirmwareConfirm()
        runCurrent()

        assertEquals(1, programMode.calls)
        assertIs<FirmwareStep.Closing>(vm.state.value.firmware)
        advanceTimeBy(2.seconds)
        runCurrent()
        assertEquals(SystemDevicesEffect.OpenDeviceSearch, effects.last())
    }

    @Test
    fun `wrong pin asks for pin and retries with it`() = test {
        programMode.results = listOf(
            Response.Success(ProgramModeResult.WRONG_PIN),
            Response.Success(ProgramModeResult.ENTERED),
        )
        val vm = viewModel()
        val tag = peripheral()
        step(PeripheralType.TAG, 0, repository.types.size, tag)
        vm.onUpdate(tag.key)
        runCurrent()
        vm.onFirmwareConfirm()
        runCurrent()
        assertFalse(vm.state.value.pin?.wrong ?: true)

        time += 20.seconds
        vm.onPinSubmit("1234")
        runCurrent()

        assertEquals(listOf("firmware ${tag.address}"), repository.calls)
        assertIs<FirmwareStep.Closing>(vm.state.value.firmware)
    }

    @Test
    fun `device not confirming firmware mode shows no answer`() = test {
        repository.firmwareReply = Response.Error(PeripheralException(PeripheralFailure.NOT_CONFIRMED))
        val vm = viewModel()
        val tag = peripheral()
        step(PeripheralType.TAG, 0, repository.types.size, tag)
        vm.onUpdate(tag.key)
        runCurrent()
        vm.onFirmwareConfirm()
        runCurrent()

        val step = assertIs<FirmwareStep.Failed>(vm.state.value.firmware)
        assertEquals(FirmwareFailure.NO_ANSWER, step.reason)
    }

    @Test
    fun `pairing link follows dynamic devices feature`() = test {
        sessionRepository.device = testAlarmDevice.copy(features = setOf(DeviceFeature.DYNAMIC_DEVICES))
        val vm = viewModel()

        assertTrue(vm.state.value.canOpenPairing)
    }
}
