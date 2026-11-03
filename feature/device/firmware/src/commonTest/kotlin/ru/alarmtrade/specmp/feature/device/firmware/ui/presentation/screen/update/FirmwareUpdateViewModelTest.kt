package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.WriteFailureReason
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeUpdateRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.navigation.FirmwareArchivePicker
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFilePickResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothManufacturer
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModuleMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

class FirmwareUpdateViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val workDevice = AlarmDevice(
        systemId = 77,
        systemName = "DX4G",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = emptySet(),
        can = null
    )
    private val bootDevice = workDevice.copy(mode = DeviceOperationMode.BOOT)

    private val alarmModule = FirmwareModule(
        id = 1,
        kind = FirmwareModuleKind.AlarmMain,
        systemName = "DX4G",
        title = "Pandora DX 4G",
        bootTarget = BootModeTarget.MainProgram
    )
    private val bluetoothModule = FirmwareModule(
        id = 2,
        kind = FirmwareModuleKind.BluetoothModule,
        systemName = "BTM2",
        title = "Pandora BT",
        bootTarget = BootModeTarget.BluetoothModule
    )
    private val otherAlarmModule = alarmModule.copy(id = 4, title = "Pandora Second", systemName = "DX2")

    private fun version(value: String, description: String = "") =
        FirmwareVersion(0, value, LocalDate(2026, 10, 14), description, "https://fw/$value")

    private val repository = FakeFirmwareRepository()
    private val sessionRepository = FakeSessionRepository(bootDevice)
    private val updateRepository = FakeUpdateRepository()
    private val requester = FirmwareBootRequester()
    private val picker = FirmwareArchivePicker()
    private val viewModels = mutableListOf<FirmwareUpdateViewModel>()
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

    private fun TestScope.viewModel(
        transport: ConnectionTransport = ConnectionTransport.USB
    ): Pair<FirmwareUpdateViewModel, MutableList<FirmwareUpdateEffect>> {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        session = DeviceSession(
            sessionRepository,
            FakeConnectionRepository(),
            dispatchers,
            backgroundScope.coroutineContext
        )
        session.attach(transport)
        val vm = FirmwareUpdateViewModel(session, repository, updateRepository, requester, picker, dispatchers)
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<FirmwareUpdateEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    private fun prepareMain() {
        repository.modulesResult = Response.Success(listOf(alarmModule, bluetoothModule, otherAlarmModule))
    }

    /** Блок в загрузчике, выбрана версия и подтверждено обновление: процесс запущен, прогресс подаётся из теста. */
    private fun TestScope.startedUpdate(): FirmwareUpdateViewModel {
        prepareMain()
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61", "notes"))
        val (vm, _) = viewModel()
        vm.onDownloadClick()
        runCurrent()
        vm.onConfirmUpdate()
        runCurrent()
        return vm
    }

    private fun linkDrop() {
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
    }

    private fun TestScope.linkBack(device: AlarmDevice) {
        sessionRepository.device = device
        sessionRepository.link.value = DeviceLinkState.READY
        runCurrent()
    }

    @Test
    fun `bootloader opens the source step with the requested channel and the main modules only`() = test {
        prepareMain()
        requester.request(FirmwareBootRequest(moduleId = 4, channel = FirmwareChannel.BETA))
        val (vm, _) = viewModel()

        val state = vm.state.value
        assertEquals(UpdatePhase.Source, state.phase)
        assertEquals(UpdateProfile.Main, state.profile)
        assertEquals("DX4G  77", state.deviceTitle)
        assertEquals(listOf("Pandora DX 4G (DX4G)", "Pandora Second (DX2)"), state.moduleTitles)
        assertEquals(1, state.selectedModule)
        assertEquals("3.57", state.currentVersion)
        assertEquals(FirmwareChannel.BETA, state.channel)
        // Запрос из экрана проверки забран: второй экран его не увидит.
        assertNull(requester.take())
    }

    @Test
    fun `without a request the first module and the release channel are used`() = test {
        prepareMain()
        val (vm, _) = viewModel()

        assertEquals(0, vm.state.value.selectedModule)
        assertEquals(FirmwareChannel.RELEASE, vm.state.value.channel)

        vm.onModuleSelected(1)
        vm.onChannelSelected(FirmwareChannel.ALPHA)
        runCurrent()
        assertEquals(1, vm.state.value.selectedModule)
        assertEquals(FirmwareChannel.ALPHA, vm.state.value.channel)
    }

    @Test
    fun `bluetooth module in the bootloader uses its own version and module`() = test {
        sessionRepository.bluetoothVersions = listOf(
            Response.Success(
                BluetoothModule(
                    BluetoothModuleMode.BOOT,
                    BluetoothManufacturer("1", "BT-DX4G", "DX4G", "1.0", "1.14")
                )
            )
        )
        prepareMain()
        requester.request(FirmwareBootRequest(2, FirmwareChannel.RELEASE))
        val (vm, _) = viewModel(ConnectionTransport.BLE)

        val state = vm.state.value
        assertEquals(UpdatePhase.Source, state.phase)
        assertEquals(UpdateProfile.Bluetooth, state.profile)
        assertEquals("Pandora BT (BTM2)", state.deviceTitle)
        assertEquals(listOf("Pandora BT (BTM2)"), state.moduleTitles)
        assertEquals("1.14", state.currentVersion)
    }

    @Test
    fun `no subsystems shows the data error and retry reads them again`() = test {
        repository.modulesResult = Response.Error(IllegalStateException("offline"))
        val (vm, _) = viewModel()
        assertEquals(UpdatePhase.DataError, vm.state.value.phase)

        prepareMain()
        vm.onRetry()
        runCurrent()

        assertEquals(UpdatePhase.Source, vm.state.value.phase)
    }

    @Test
    fun `download fetches the latest version and asks for confirmation`() = test {
        prepareMain()
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61", "notes"))
        val (vm, _) = viewModel()

        vm.onDownloadClick()
        runCurrent()

        val state = vm.state.value
        assertEquals(UpdatePhase.Confirm, state.phase)
        assertEquals(ConfirmUi("3.57", "3.61", "notes", older = false), state.confirm)
        assertEquals(listOf(1L to FirmwareChannel.RELEASE), repository.latestCalls)
    }

    @Test
    fun `an older server version is flagged on the confirmation`() = test {
        prepareMain()
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.9"))
        val (vm, _) = viewModel()

        vm.onDownloadClick()
        runCurrent()

        assertEquals(true, vm.state.value.confirm?.older)
    }

    @Test
    fun `failed download goes back to the source step with an error and cancel returns from confirmation`() = test {
        prepareMain()
        val (vm, _) = viewModel()

        vm.onDownloadClick()
        runCurrent()
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertEquals(FirmwareError.LoadFailed, vm.state.value.sourceError)

        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        vm.onDownloadClick()
        runCurrent()
        assertEquals(UpdatePhase.Confirm, vm.state.value.phase)
        assertEquals(null, vm.state.value.sourceError)

        vm.onCancelConfirm()
        runCurrent()
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertNull(vm.state.value.confirm)
    }

    @Test
    fun `update runs through the stages and a finished update leaves the block in the bootloader`() = test {
        val vm = startedUpdate()
        assertEquals(UpdatePhase.Prepare, vm.state.value.phase)
        assertEquals(listOf(BootModeTarget.MainProgram), updateRepository.calls.map { it.first })
        assertEquals("3.61", updateRepository.calls.single().second.version)

        updateRepository.progress.emit(FirmwareUpdateProgress.Preparing)
        updateRepository.progress.emit(FirmwareUpdateProgress.Sending(42))
        runCurrent()
        assertEquals(UpdatePhase.Transfer(42), vm.state.value.phase)

        updateRepository.progress.emit(FirmwareUpdateProgress.Verifying)
        runCurrent()
        assertEquals(UpdatePhase.Verify, vm.state.value.phase)

        updateRepository.progress.emit(FirmwareUpdateProgress.Applying)
        runCurrent()
        assertEquals(UpdatePhase.Apply, vm.state.value.phase)
        // Блок мог бы перезагрузиться сам: обрыв связи на применении ожидаем.
        assertEquals(DeviceReboot.ToMain, session.reboot.value)

        updateRepository.progress.emit(FirmwareUpdateProgress.Finished)
        runCurrent()
        assertEquals(UpdatePhase.Success, vm.state.value.phase)
        assertTrue(vm.state.value.locked)
        // Блок остался в загрузчике: экран успеха это показывает.
        assertTrue(vm.state.value.inBoot)

        // «OK» — обычное состояние выбора источника: можно выбрать другую прошивку или выйти командой.
        vm.onResultOk()
        runCurrent()
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertNull(vm.state.value.confirm)
        assertNull(session.reboot.value)
    }

    @Test
    fun `block that came back after applying without a result counts as success`() = test {
        val vm = startedUpdate()
        updateRepository.progress.emit(FirmwareUpdateProgress.Applying)
        runCurrent()

        // Связь рвётся (ожидаемо) — это не «потеря связи», экран остаётся на применении.
        linkDrop()
        runCurrent()
        assertEquals(UpdatePhase.Apply, vm.state.value.phase)

        linkBack(workDevice)
        assertEquals(UpdatePhase.Success, vm.state.value.phase)
        // Экран результата ждёт «OK» — ожидание не снимается само.
        assertEquals(DeviceReboot.ToMain, session.reboot.value)

        vm.onResultOk()
        runCurrent()
        assertNull(session.reboot.value)
    }

    @Test
    fun `block that returns to the bootloader after applying is an applying error`() = test {
        val vm = startedUpdate()
        updateRepository.progress.emit(FirmwareUpdateProgress.Applying)
        runCurrent()
        linkDrop()
        runCurrent()

        linkBack(bootDevice)

        assertEquals(UpdatePhase.Error(FailureKind.ApplyingError), vm.state.value.phase)
        assertNull(session.reboot.value)
    }

    @Test
    fun `write failure shows its reason and retry returns to the source step`() = test {
        val vm = startedUpdate()
        updateRepository.progress.emit(FirmwareUpdateProgress.Sending(10))
        updateRepository.progress.emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Write(WriteFailureReason.Crc)))
        runCurrent()

        assertEquals(UpdatePhase.Error(FailureKind.Crc), vm.state.value.phase)
        assertEquals(false, vm.state.value.locked)

        vm.onRetry()
        runCurrent()
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertNull(vm.state.value.confirm)
    }

    @Test
    fun `unknown failure without a link drop is an error`() = test {
        val vm = startedUpdate()
        updateRepository.progress.emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Unknown))
        runCurrent()
        advanceTimeBy(3_500)
        runCurrent()

        assertEquals(UpdatePhase.Error(FailureKind.Unknown), vm.state.value.phase)
    }

    @Test
    fun `link loss in the middle of the transfer shows lost and the source step after the block returns`() = test {
        val vm = startedUpdate()
        updateRepository.progress.emit(FirmwareUpdateProgress.Sending(30))
        runCurrent()

        linkDrop()
        runCurrent()
        assertEquals(UpdatePhase.Lost(updating = true), vm.state.value.phase)

        // Поток SDK падает уже после обрыва — это тот же обрыв, а не отдельная ошибка.
        updateRepository.progress.emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Unknown))
        runCurrent()
        assertEquals(UpdatePhase.Lost(updating = true), vm.state.value.phase)

        linkBack(bootDevice)
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertNull(vm.state.value.confirm)
    }

    /** Блок с Bluetooth-модулем в загрузчике (BLE), версия подтверждена, передача идёт. */
    private fun TestScope.startedBluetoothTransfer(): FirmwareUpdateViewModel {
        sessionRepository.bluetoothVersions = listOf(
            Response.Success(
                BluetoothModule(
                    BluetoothModuleMode.BOOT,
                    BluetoothManufacturer("1", "BT-DX4G", "DX4G", "1.0", "1.14")
                )
            )
        )
        prepareMain()
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.20"))
        requester.request(FirmwareBootRequest(2, FirmwareChannel.RELEASE))
        val (vm, _) = viewModel(ConnectionTransport.BLE)
        vm.onDownloadClick()
        runCurrent()
        vm.onConfirmUpdate()
        runCurrent()
        updateRepository.progress.tryEmit(FirmwareUpdateProgress.Sending(30))
        runCurrent()
        return vm
    }

    @Test
    fun `bluetooth transfer survives a link drop and continues after the module returns`() = test {
        val vm = startedBluetoothTransfer()
        assertEquals(listOf(BootModeTarget.BluetoothModule), updateRepository.calls.map { it.first })

        linkDrop()
        runCurrent()
        assertEquals(UpdatePhase.Transfer(30, reconnecting = true), vm.state.value.phase)
        assertTrue(vm.state.value.locked)

        linkBack(bootDevice)
        assertEquals(UpdatePhase.Transfer(30), vm.state.value.phase)
        // Тот же процесс: обновление не перезапускалось, прогресс SDK продолжает двигать экран.
        assertEquals(1, updateRepository.calls.size)
        updateRepository.progress.emit(FirmwareUpdateProgress.Sending(55))
        runCurrent()
        assertEquals(UpdatePhase.Transfer(55), vm.state.value.phase)
    }

    @Test
    fun `main program transfer over bluetooth survives a link drop without rereading the block`() = test {
        prepareMain()
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        val (vm, _) = viewModel(ConnectionTransport.BLE)
        vm.onDownloadClick()
        runCurrent()
        vm.onConfirmUpdate()
        runCurrent()
        updateRepository.progress.emit(FirmwareUpdateProgress.Sending(30))
        runCurrent()
        assertEquals(listOf(BootModeTarget.MainProgram), updateRepository.calls.map { it.first })
        val reads = sessionRepository.bluetoothReads

        linkDrop()
        runCurrent()
        assertEquals(UpdatePhase.Transfer(30, reconnecting = true), vm.state.value.phase)

        linkBack(bootDevice)
        assertEquals(UpdatePhase.Transfer(30), vm.state.value.phase)
        // Режим на переподключении не перечитывался: передача держит протокол блока.
        assertEquals(reads, sessionRepository.bluetoothReads)
        assertEquals(1, updateRepository.calls.size)

        // Передача закончилась — дальше (применение) режим блока снова определяется по связи.
        updateRepository.progress.emit(FirmwareUpdateProgress.Verifying)
        runCurrent()
        linkDrop()
        runCurrent()
        linkBack(bootDevice)
        assertTrue(sessionRepository.bluetoothReads > reads)
    }

    @Test
    fun `bluetooth transfer that the sdk gave up on is lost and returns to the source step`() = test {
        val vm = startedBluetoothTransfer()
        linkDrop()
        runCurrent()

        updateRepository.progress.emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Unknown))
        runCurrent()
        assertEquals(UpdatePhase.Lost(updating = true), vm.state.value.phase)

        linkBack(bootDevice)
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
    }

    @Test
    fun `link loss while choosing the source is lost without an update and the screen stays`() = test {
        prepareMain()
        val (vm, _) = viewModel()
        linkDrop()
        runCurrent()
        assertEquals(UpdatePhase.Lost(updating = false), vm.state.value.phase)

        vm.onReconnect()
        runCurrent()
        assertEquals(1, sessionRepository.reconnectCount)

        linkBack(bootDevice)
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
    }

    @Test
    fun `expected reboot into the bootloader shows entering until the block returns in it`() = test {
        sessionRepository.device = workDevice
        prepareMain()
        requester.request(FirmwareBootRequest(1, FirmwareChannel.RELEASE))
        val (vm, _) = viewModel()
        // Экран создан, пока блок ещё в рабочем режиме (хост показывает слот при ожидаемой перезагрузке).
        session.expectReboot(DeviceReboot.ToBoot)
        runCurrent()
        assertEquals(UpdatePhase.Entering, vm.state.value.phase)
        assertTrue(vm.state.value.locked)

        linkDrop()
        runCurrent()
        assertEquals(UpdatePhase.Entering, vm.state.value.phase)

        linkBack(bootDevice)
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertEquals(2, vm.state.value.moduleTitles.size)
    }

    @Test
    fun `back asks for confirmation on choice steps, is locked during the process and leaves after an error`() = test {
        prepareMain()
        val (vm, effects) = viewModel()

        vm.onBackClick()
        runCurrent()
        assertEquals(true, vm.state.value.exitDialog)
        vm.onExitDialogDismiss()
        runCurrent()
        assertEquals(false, vm.state.value.exitDialog)
        assertTrue(effects.isEmpty())

        vm.onBackClick()
        vm.onExitConfirm()
        runCurrent()
        assertEquals(listOf<FirmwareUpdateEffect>(FirmwareUpdateEffect.Exit), effects)
        assertEquals(false, vm.state.value.exitDialog)
    }

    @Test
    fun `back is ignored during the process and leaves at once after an error`() = test {
        val vm = startedUpdate()
        val effects = mutableListOf<FirmwareUpdateEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }

        vm.onBackClick()
        runCurrent()
        assertEquals(false, vm.state.value.exitDialog)
        assertTrue(effects.isEmpty())

        updateRepository.progress.emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Preparation))
        runCurrent()
        assertIs<UpdatePhase.Error>(vm.state.value.phase)
        vm.onBackClick()
        runCurrent()
        assertEquals(listOf<FirmwareUpdateEffect>(FirmwareUpdateEffect.Exit), effects)
    }

    @Test
    fun `support and archive are one-shot effects and archive opens only from the source step`() = test {
        prepareMain()
        val (vm, effects) = viewModel()

        vm.onArchiveClick()
        vm.onSupportClick()
        runCurrent()
        assertEquals(
            listOf(
                FirmwareUpdateEffect.OpenArchive(
                    FirmwareArchiveRequest(
                        1,
                        FirmwareChannel.RELEASE,
                        "3.57",
                        "DX4G  77",
                        UpdateProfile.Main,
                        deviceName = "DX4G"
                    )
                ),
                FirmwareUpdateEffect.OpenSupportChat
            ),
            effects
        )

        vm.onDisconnect()
        runCurrent()
        assertEquals(FirmwareUpdateEffect.Exit, effects.last())
    }

    @Test
    fun `version picked in the archive goes straight to the confirmation`() = test {
        prepareMain()
        val (vm, _) = viewModel()

        picker.pick(version("3.55", "old notes"))
        runCurrent()

        val state = vm.state.value
        assertEquals(UpdatePhase.Confirm, state.phase)
        assertEquals(ConfirmUi("3.57", "3.55", "old notes", older = true), state.confirm)

        // Выбор в другой фазе (например, пока идёт процесс) игнорируется.
        vm.onConfirmUpdate()
        runCurrent()
        picker.pick(version("3.99"))
        runCurrent()
        assertEquals(UpdatePhase.Prepare, vm.state.value.phase)
        assertEquals("3.55", vm.state.value.confirm?.newVersion)
    }

    @Test
    fun `file picked in the file manager goes to the confirmation and updates from that file`() = test {
        prepareMain()
        val (vm, _) = viewModel()

        vm.onFilePicked(FirmwareFilePickResult.Picked("fw_3_70.bin", "/tmp/fw_3_70.bin"))
        runCurrent()

        val state = vm.state.value
        assertEquals(UpdatePhase.Confirm, state.phase)
        assertEquals(ConfirmUi("3.57", "fw_3_70.bin", "", older = false, localFile = true), state.confirm)

        vm.onConfirmUpdate()
        runCurrent()
        assertEquals(listOf(BootModeTarget.MainProgram to "/tmp/fw_3_70.bin"), updateRepository.fileCalls)
        assertEquals(emptyList(), updateRepository.calls)
    }

    @Test
    fun `cancelled file pick changes nothing and a rejected file shows its error until the next action`() = test {
        prepareMain()
        val (vm, _) = viewModel()

        vm.onFilePicked(FirmwareFilePickResult.Cancelled)
        runCurrent()
        assertEquals(UpdatePhase.Source, vm.state.value.phase)
        assertEquals(null, vm.state.value.fileError)

        vm.onFilePicked(FirmwareFilePickResult.TooLarge)
        runCurrent()
        assertEquals(FileError.TooLarge, vm.state.value.fileError)
        vm.onFilePicked(FirmwareFilePickResult.Failed)
        runCurrent()
        assertEquals(FileError.Failed, vm.state.value.fileError)

        vm.onChannelSelected(FirmwareChannel.BETA)
        runCurrent()
        assertEquals(null, vm.state.value.fileError)
    }

    @Test
    fun `exit command is offered over bluetooth only`() = test {
        prepareMain()
        val (ble, _) = viewModel(ConnectionTransport.BLE)
        assertTrue(ble.state.value.canExitBoot)
    }

    @Test
    fun `usb has no exit command`() = test {
        prepareMain()
        val (usb, _) = viewModel(ConnectionTransport.USB)
        assertEquals(false, usb.state.value.canExitBoot)

        usb.onExitBootClick()
        runCurrent()
        assertEquals(0, sessionRepository.exitCalls)
    }

    @Test
    fun `exit command restarts the block and waits for it to come back`() = test {
        prepareMain()
        val (vm, _) = viewModel(ConnectionTransport.BLE)

        vm.onExitBootClick()
        runCurrent()

        assertEquals(listOf(BootModeTarget.MainProgram), sessionRepository.exitTargets)
        assertEquals(DeviceReboot.ToMain, session.reboot.value)
        assertEquals(UpdatePhase.Finishing, vm.state.value.phase)

        // Блок перезагрузился и вернулся в рабочий режим — ожидание снято, хост уйдёт со слота.
        linkDrop()
        runCurrent()
        linkBack(workDevice)
        assertNull(session.reboot.value)
    }

    @Test
    fun `failed exit command with the block still in the bootloader shows an error`() = test {
        prepareMain()
        sessionRepository.exitResults = listOf(Response.Error(IllegalStateException("no answer")))
        val (vm, _) = viewModel(ConnectionTransport.BLE)

        vm.onExitBootClick()
        runCurrent()

        val state = vm.state.value
        assertEquals(UpdatePhase.Source, state.phase)
        assertEquals(true, state.exitError)
        assertNull(session.reboot.value)
        // Одна команда на нажатие; повтор — кнопкой.
        assertEquals(1, sessionRepository.exitCalls)

        sessionRepository.exitResults = listOf(Response.Success(Unit))
        vm.onExitBootClick()
        runCurrent()
        assertEquals(false, vm.state.value.exitError)
        assertEquals(UpdatePhase.Finishing, vm.state.value.phase)
    }

    @Test
    fun `exit command is offered only on the source step`() = test {
        prepareMain()
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        val (vm, _) = viewModel(ConnectionTransport.BLE)
        vm.onDownloadClick()
        runCurrent()
        assertEquals(UpdatePhase.Confirm, vm.state.value.phase)

        vm.onExitBootClick()
        runCurrent()

        assertEquals(0, sessionRepository.exitCalls)
    }

    @Test
    fun `block can from the bootloader filters download, opens the archive on it and is shown on the source step`() =
        test {
            sessionRepository.device = bootDevice.copy(can = CanInfo("1.03", "2154", isFactory = false, version = 103))
            prepareMain()
            repository.latest[1L to FirmwareChannel.RELEASE] =
                Response.Success(version("3.61").copy(canCode = "3312", canVersion = "1.01"))
            val (vm, effects) = viewModel()

            assertEquals(BlockCanUi("2154", "1.03"), vm.state.value.blockCan)

            vm.onArchiveClick()
            runCurrent()
            val request = (effects.single() as FirmwareUpdateEffect.OpenArchive).args
            assertEquals("2154", request.deviceCanCode)
            assertEquals(103, request.deviceCanVersion)
            assertEquals("1.03", request.deviceCanProfile)
            assertEquals(false, request.deviceCanFactory)

            vm.onDownloadClick()
            runCurrent()
            assertEquals(listOf<FirmwareCan?>(FirmwareCan("2154", 103, "1.03")), repository.latestCans)
            assertEquals(ConfirmCanUi("2154", "3312", "1.01"), vm.state.value.confirm?.can)
            assertEquals(false, vm.state.value.confirm?.can?.same)
        }

    @Test
    fun `factory can settings open the archive without a code and show factory settings`() = test {
        sessionRepository.device = bootDevice.copy(can = CanInfo("0.00", "0000", isFactory = true))
        prepareMain()
        val (vm, effects) = viewModel()

        assertEquals(BlockCanUi(null, null), vm.state.value.blockCan)
        vm.onArchiveClick()
        runCurrent()
        val request = (effects.single() as FirmwareUpdateEffect.OpenArchive).args
        assertNull(request.deviceCanCode)
        assertEquals(true, request.deviceCanFactory)
    }

    @Test
    fun `block without single can firmware has no can row`() = test {
        prepareMain()
        val (vm, _) = viewModel()

        assertNull(vm.state.value.blockCan)
    }
}
