package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.file

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
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
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check.CheckDialog
import ru.alarmtrade.specmp.feature.device.interaction.di.createDeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown

class FirmwareFileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val device = AlarmDevice(
        systemId = 77,
        systemName = "DX4G",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = emptySet(),
        can = null,
    )

    private val mainModule = FirmwareModule(
        id = 1,
        kind = FirmwareModuleKind.AlarmMain,
        systemName = "DX4G",
        title = "Pandora DX 4G",
        bootTarget = BootModeTarget.MainProgram,
    )
    private val bluetoothModule = FirmwareModule(
        id = 2,
        kind = FirmwareModuleKind.BluetoothModule,
        systemName = "BTM2",
        title = "Pandora BT",
        bootTarget = BootModeTarget.BluetoothModule,
    )

    private val repository = FakeFirmwareRepository()
    private val sessionRepository = FakeSessionRepository(device)
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val requester = FirmwareBootRequester()
    private val viewModels = mutableListOf<FirmwareFileViewModel>()
    private lateinit var session: DeviceSession

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

    private fun TestScope.viewModel(): FirmwareFileViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        session = createDeviceSession(
            sessionRepository,
            FakeConnectionRepository(),
            dispatchers,
            backgroundScope.coroutineContext,
        ).also { it.start() }
        session.attach(ConnectionTransport.BLE)
        val vm = FirmwareFileViewModel(session, repository, requester, cooldown, dispatchers).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        return vm
    }

    @Test
    fun `main program of the block is the update target`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule, mainModule))
        val vm = viewModel()

        assertEquals("DX4G", vm.state.value.deviceName)
        assertEquals(77L, vm.state.value.deviceId)
        assertEquals(LCEState.Content(FileTarget.Supported(1)), vm.state.value.target)
    }

    @Test
    fun `block without main program module is not supported`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule))
        val vm = viewModel()
        assertEquals(LCEState.Content(FileTarget.NotSupported), vm.state.value.target)

        vm.onUpdateClick()
        runCurrent()
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `module list failure shows an error and retry loads it`() = test {
        repository.modulesResult = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()
        assertEquals(LCEState.Error(FirmwareError.LoadFailed), vm.state.value.target)

        repository.modulesResult = Response.Success(listOf(mainModule))
        vm.onRetry()
        runCurrent()
        assertEquals(LCEState.Content(FileTarget.Supported(1)), vm.state.value.target)
    }

    @Test
    fun `confirmed boot leaves the request for the bootloader screen`() = test {
        repository.modulesResult = Response.Success(listOf(mainModule))
        val vm = viewModel()

        vm.onUpdateClick()
        runCurrent()
        assertEquals(CheckDialog.ConfirmBoot(1), vm.state.value.dialog)

        vm.onConfirmBoot()
        runCurrent()
        assertEquals(listOf(BootModeTarget.MainProgram), sessionRepository.bootTargets.distinct())
        assertEquals(DeviceReboot.ToBoot, session.reboot.value)
        assertEquals(FirmwareBootRequest(1, FirmwareChannel.RELEASE), requester.take())
        assertNull(vm.state.value.dialog)
        assertEquals(false, vm.state.value.bootFailed)
    }

    @Test
    fun `dialog is dismissed`() = test {
        repository.modulesResult = Response.Success(listOf(mainModule))
        val vm = viewModel()
        vm.onUpdateClick()
        runCurrent()

        vm.onDialogDismiss()
        runCurrent()
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `command error shows the banner`() = test {
        repository.modulesResult = Response.Success(listOf(mainModule))
        sessionRepository.bootResults = listOf(Response.Error(IllegalStateException("no answer")))
        val vm = viewModel()

        vm.onUpdateClick()
        vm.onConfirmBoot()
        runCurrent()

        assertEquals(true, vm.state.value.bootFailed)
        assertNull(vm.state.value.dialog)
        assertNull(session.reboot.value)
        assertEquals(1, sessionRepository.bootCalls)
    }

    @Test
    fun `wrong pin opens the locked pin dialog, wrong entered pin counts and locks again`() = test {
        repository.modulesResult = Response.Success(listOf(mainModule))
        sessionRepository.bootResults = listOf(Response.Success(BootModeResult.WrongPin))
        val vm = viewModel()

        vm.onUpdateClick()
        vm.onConfirmBoot()
        runCurrent()
        val silent = vm.state.value.dialog
        assertIs<CheckDialog.Pin>(silent)
        assertEquals(false, silent.wrong)
        assertEquals(15.seconds, silent.lockedFor)

        // Пока идёт блокировка, ввод ничего не отправляет.
        vm.onPinSubmit("1234")
        runCurrent()
        assertEquals(1, sessionRepository.bootCalls)

        time += 15.seconds
        advanceTimeBy(500)
        runCurrent()
        assertNull((vm.state.value.dialog as CheckDialog.Pin).lockedFor)

        vm.onPinSubmit("0000")
        runCurrent()
        val wrong = vm.state.value.dialog
        assertIs<CheckDialog.Pin>(wrong)
        assertEquals(true, wrong.wrong)
        assertEquals(1, wrong.wrongCount)
        assertEquals(true, Duration.ZERO < (wrong.lockedFor ?: Duration.ZERO))
        assertNull(session.reboot.value)
    }

    @Test
    fun `reconnect of the same block does not reload the target`() = test {
        repository.modulesResult = Response.Success(listOf(mainModule))
        val vm = viewModel()
        repository.modulesResult = Response.Error(IllegalStateException("offline"))

        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.READY
        runCurrent()

        assertEquals(LCEState.Content(FileTarget.Supported(1)), vm.state.value.target)
    }
}
