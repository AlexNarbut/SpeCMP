package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.check

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequest
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareBootRequester
import ru.alarmtrade.specmp.feature.device.firmware.ui.FakeFirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.ServicePinCooldown
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession

class FirmwareCheckViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val device = AlarmDevice(
        systemId = 77,
        systemName = "DX4G",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = emptySet(),
        can = null,
    )

    private val alarmModule = FirmwareModule(
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
    private val watchModule = FirmwareModule(
        id = 3,
        kind = FirmwareModuleKind.WatchBluetooth,
        systemName = "PW2",
        title = "Pandora Watch",
        bootTarget = null,
    )

    private fun version(value: String, description: String = "") =
        FirmwareVersion(0, value, LocalDate(2026, 10, 14), description, "https://fw/$value")

    private val repository = FakeFirmwareRepository()
    private val sessionRepository = FakeDeviceSessionRepository(device)
    private val time = TestTimeSource()
    private val cooldown = ServicePinCooldown(time)
    private val requester = FirmwareBootRequester()
    private val viewModels = mutableListOf<FirmwareCheckViewModel>()

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

    private lateinit var session: DeviceSession

    private fun TestScope.viewModel(
        transport: ConnectionTransport = ConnectionTransport.BLE,
        subscribed: Boolean = true,
    ): FirmwareCheckViewModel {
        val dispatchers = testDispatchers(dispatcher)
        session = testDeviceSession(sessionRepository, dispatcher = dispatcher, transport = transport)
        val vm = FirmwareCheckViewModel(session, repository, requester, cooldown, dispatchers).also { viewModels += it }
        if (subscribed) backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        return vm
    }

    private fun FirmwareCheckViewModel.cards(): List<ModuleCardUi> = (state.value.content as LCEState.Content).content

    private fun FirmwareCheckViewModel.card(id: Long): ModuleCardUi = cards().first { it.id == id }

    @Test
    fun `modules are read with current and server versions`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule, bluetoothModule, watchModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61", "notes"))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.14"))
        val vm = viewModel()

        assertEquals("DX4G", vm.state.value.deviceName)
        assertEquals(77L, vm.state.value.deviceId)

        val alarm = vm.card(1)
        assertEquals("Pandora DX 4G (DX4G)", alarm.title)
        assertEquals(CurrentVersionUi.Known("3.57"), alarm.currentVersion)
        assertEquals(CardStatusUi.UpdateAvailable("3.61", "notes"), alarm.status)

        val bluetooth = vm.card(2)
        assertEquals(CurrentVersionUi.Known("1.14"), bluetooth.currentVersion)
        assertEquals(CardStatusUi.Actual("1.14"), bluetooth.status)

        val watch = vm.card(3)
        assertEquals(CurrentVersionUi.Absent, watch.currentVersion)
        assertEquals(CardStatusUi.NotSupported, watch.status)
        // Для неподдерживаемого модуля версию на сервере не спрашиваем.
        assertTrue(repository.latestCalls.none { it.first == 3L })
    }

    @Test
    fun `server version older than installed is up to date and numbers are compared as numbers`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.9"))
        val vm = viewModel()
        // Блок 3.57 новее сервера 3.9 — актуальна (а при сравнении строк «3.9» > «3.57» показало бы обновление).
        assertEquals(CardStatusUi.Actual("3.57"), vm.card(1).status)
    }

    @Test
    fun `failed server request and failed bluetooth read are shown per card`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule, bluetoothModule))
        // Первое чтение — определение режима при подключении (BLE), второе — версия для карточки.
        sessionRepository.bluetoothVersions = listOf(
            sessionRepository.bluetoothVersions.single(),
            Response.Error(IllegalStateException("no answer")),
        )
        val vm = viewModel()

        assertEquals(CardStatusUi.Failed(FirmwareError.LoadFailed), vm.card(1).status)
        assertEquals(CurrentVersionUi.Absent, vm.card(2).currentVersion)
        // Текущая версия BLE-модуля неизвестна — обновление предлагается (лучше предложить, чем скрыть).
        assertEquals(CardStatusUi.Failed(FirmwareError.LoadFailed), vm.card(2).status)
    }

    @Test
    fun `module list failure shows an error and retry loads it`() = test {
        repository.modulesResult = Response.Error(IllegalStateException("offline"))
        val vm = viewModel()
        assertEquals(LCEState.Error(FirmwareError.LoadFailed), vm.state.value.content)

        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        vm.onRetry()
        runCurrent()

        assertIs<LCEState.Content<*, *>>(vm.state.value.content)
        assertIs<CardStatusUi.UpdateAvailable>(vm.card(1).status)
    }

    @Test
    fun `channel change asks the server again for supported modules and drops stale answers`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule, watchModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        repository.latest[1L to FirmwareChannel.BETA] = Response.Success(version("3.62-beta.2"))
        val vm = viewModel()

        vm.onChannelSelected(FirmwareChannel.BETA)
        runCurrent()

        assertEquals(FirmwareChannel.BETA, vm.state.value.channel)
        assertEquals(CardStatusUi.UpdateAvailable("3.62-beta.2", ""), vm.card(1).status)
        assertEquals(listOf(1L to FirmwareChannel.RELEASE, 1L to FirmwareChannel.BETA), repository.latestCalls)

        // Тот же канал — ничего не происходит.
        vm.onChannelSelected(FirmwareChannel.BETA)
        runCurrent()
        assertEquals(2, repository.latestCalls.size)
    }

    @Test
    fun `refresh asks the server again for one module`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule, bluetoothModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Error(IllegalStateException("503"))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.14"))
        val vm = viewModel()
        assertEquals(CardStatusUi.Failed(FirmwareError.LoadFailed), vm.card(1).status)

        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        vm.onRefreshModule(1)
        runCurrent()

        assertIs<CardStatusUi.UpdateAvailable>(vm.card(1).status)
        assertEquals(2, repository.latestCalls.count { it.first == 1L })
        assertEquals(1, repository.latestCalls.count { it.first == 2L })
    }

    @Test
    fun `description dialog shows the server version and update goes to confirmation`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61", "notes"))
        val vm = viewModel()

        vm.onDescriptionClick(1)
        runCurrent()
        assertEquals(CheckDialog.Description(1, "3.61", "notes"), vm.state.value.dialog)

        vm.onUpdateClick(1)
        runCurrent()
        assertEquals(CheckDialog.ConfirmBoot(1), vm.state.value.dialog)

        vm.onDialogDismiss()
        runCurrent()
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `unsupported module cannot enter the update mode`() = test {
        repository.modulesResult = Response.Success(listOf(watchModule))
        val vm = viewModel()

        vm.onUpdateClick(3)
        runCurrent()

        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `accepted command leaves the request and the expected reboot for the bootloader screen`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        val vm = viewModel()
        vm.onChannelSelected(FirmwareChannel.BETA)
        runCurrent()

        vm.onUpdateClick(1)
        vm.onConfirmBoot()
        runCurrent()

        assertEquals(listOf(BootModeTarget.MainProgram), sessionRepository.bootTargets.distinct())
        assertEquals(DeviceReboot.ToBoot, session.reboot.value)
        assertEquals(FirmwareBootRequest(1, FirmwareChannel.BETA), requester.take())
        assertNull(vm.state.value.dialog)
        assertEquals(false, vm.state.value.bootFailed)
    }

    @Test
    fun `wrong pin opens the pin dialog locked and sends nothing while the block is locked`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.16"))
        sessionRepository.bootResults = listOf(Response.Success(BootModeResult.WrongPin))
        val vm = viewModel()

        vm.onUpdateClick(2)
        vm.onConfirmBoot()
        runCurrent()

        val dialog = vm.state.value.dialog
        assertIs<CheckDialog.Pin>(dialog)
        assertEquals(2L, dialog.moduleId)
        // Молчаливая попытка (сохранённый PIN или 1111) — пользователь PIN ещё не вводил.
        assertEquals(false, dialog.wrong)
        assertEquals(0, dialog.wrongCount)
        assertEquals(15.seconds, dialog.lockedFor)
        assertNull(session.reboot.value)
        assertNull(requester.take())
        assertEquals(1, sessionRepository.bootCalls)

        // Пока идёт блокировка, ввод ничего не отправляет.
        vm.onPinSubmit("1234")
        runCurrent()
        assertEquals(1, sessionRepository.bootCalls)
        assertTrue(sessionRepository.pins.isEmpty())
    }

    @Test
    fun `entered pin is saved and the command is repeated after the lock`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.16"))
        sessionRepository.bootResults = listOf(
            Response.Success(BootModeResult.WrongPin),
            Response.Success(BootModeResult.Entered),
        )
        val vm = viewModel()
        vm.onUpdateClick(2)
        vm.onConfirmBoot()
        runCurrent()
        time += 15.seconds
        advanceTimeBy(500)
        runCurrent()
        assertNull((vm.state.value.dialog as CheckDialog.Pin).lockedFor)

        vm.onPinSubmit("1234")
        runCurrent()

        assertEquals(1, sessionRepository.pins.size)
        assertEquals(ServicePin.parseOrNull("1234"), sessionRepository.pins.single())
        assertEquals(listOf(BootModeTarget.BluetoothModule), sessionRepository.bootTargets.distinct())
        assertNull(vm.state.value.dialog)
        assertEquals(DeviceReboot.ToBoot, session.reboot.value)
    }

    @Test
    fun `wrong entered pin counts, locks again and clears the expected reboot`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.16"))
        sessionRepository.bootResults = listOf(Response.Success(BootModeResult.WrongPin))
        val vm = viewModel()
        vm.onUpdateClick(2)
        vm.onConfirmBoot()
        runCurrent()
        time += 15.seconds
        advanceTimeBy(500)
        runCurrent()

        vm.onPinSubmit("0000")
        runCurrent()

        val dialog = vm.state.value.dialog
        assertIs<CheckDialog.Pin>(dialog)
        assertEquals(true, dialog.wrong)
        assertEquals(1, dialog.wrongCount)
        assertEquals(Duration.ZERO < (dialog.lockedFor ?: Duration.ZERO), true)
        assertNull(session.reboot.value)
        assertNull(requester.take())
    }

    @Test
    fun `usb does not ask for a pin even while a lock is running`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        cooldown.start(77)
        val vm = viewModel(ConnectionTransport.USB)

        vm.onUpdateClick(1)
        vm.onConfirmBoot()
        runCurrent()

        assertTrue(sessionRepository.bootCalls >= 1)
        assertNull(vm.state.value.dialog)
    }

    @Test
    fun `lock from a previous attempt opens the pin dialog without sending the command`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.16"))
        cooldown.start(77)
        time += 5.seconds
        val vm = viewModel()

        vm.onUpdateClick(2)
        vm.onConfirmBoot()
        runCurrent()

        val dialog = vm.state.value.dialog
        assertIs<CheckDialog.Pin>(dialog)
        assertEquals(10.seconds, dialog.lockedFor)
        assertEquals(0, sessionRepository.bootCalls)
    }

    @Test
    fun `command error shows the banner and does not wait for a reboot`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        sessionRepository.bootResults = listOf(Response.Error(IllegalStateException("no answer")))
        val vm = viewModel()

        vm.onUpdateClick(1)
        vm.onConfirmBoot()
        runCurrent()

        assertEquals(true, vm.state.value.bootFailed)
        assertNull(vm.state.value.dialog)
        assertNull(session.reboot.value)
        assertNull(requester.take())
        // Ошибку нельзя повторять автоматически: одна команда на нажатие.
        assertEquals(1, sessionRepository.bootCalls)
    }

    @Test
    fun `short pin is ignored`() = test {
        repository.modulesResult = Response.Success(listOf(bluetoothModule))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("1.16"))
        sessionRepository.bootResults = listOf(Response.Success(BootModeResult.WrongPin))
        val vm = viewModel()
        vm.onUpdateClick(2)
        vm.onConfirmBoot()
        runCurrent()
        time += 15.seconds
        advanceTimeBy(500)
        runCurrent()

        vm.onPinSubmit("12")
        runCurrent()

        assertTrue(sessionRepository.pins.isEmpty())
        assertEquals(1, sessionRepository.bootCalls)
    }

    @Test
    fun `latest version of the main program is looked up under the block can, other modules without it`() = test {
        sessionRepository.device = device.copy(can = CanInfo("1.03", "2154", isFactory = false, version = 103))
        repository.modulesResult = Response.Success(listOf(alarmModule, bluetoothModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        repository.latest[2L to FirmwareChannel.RELEASE] = Response.Success(version("2.15"))
        viewModel()

        val cans = repository.latestCalls.map { it.first }.zip(repository.latestCans).toMap()
        assertEquals(FirmwareCan("2154", 103, "1.03"), cans[1L])
        assertNull(cans[2L])
    }

    @Test
    fun `factory can settings look up the latest version without a can filter`() = test {
        sessionRepository.device = device.copy(can = CanInfo("0.00", "0000", isFactory = true))
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        viewModel()

        assertEquals(listOf<FirmwareCan?>(null), repository.latestCans)
    }

    @Test
    fun `returning to the screen keeps the cards without asking the server again`() = test {
        repository.modulesResult = Response.Success(listOf(alarmModule))
        repository.latest[1L to FirmwareChannel.RELEASE] = Response.Success(version("3.61"))
        val vm = viewModel(subscribed = false)
        var screen = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()
        assertIs<CardStatusUi.UpdateAvailable>(vm.card(1).status)

        screen.cancel()
        advanceTimeBy(10_000)
        runCurrent()
        screen = backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        runCurrent()

        assertIs<CardStatusUi.UpdateAvailable>(vm.card(1).status)
        assertEquals(1, repository.latestCalls.size)
        screen.cancel()
    }
}
