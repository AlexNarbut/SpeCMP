package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Channel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.ChannelLogic
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchOutputSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationPickKind
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

class DeviceSettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val viewModels = mutableListOf<DeviceSettingsViewModel>()

    private val device = AlarmDevice(
        systemId = 1234567,
        systemName = "DX-9X",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = emptySet(),
        can = null
    )

    private val shock = SwitchSetting(11, "3.2.1", "Датчик удара", null, SettingValue.Switch(false), null, null)
    private val delay = IntegerSetting(12, "3.2.4", "Задержка", null, SettingValue.Integer(30), 0..120, "с", null, null)
    private val sensors = SettingsSection(10, "3.2", "Датчики", null, listOf(shock, delay))
    private val door = InputSetting(
        41, "4.1", "Концевик", null,
        SettingValue.Input(InputValue(listOf(true, false), InputContactType.NORMALLY_OPEN)),
        Channel(1, "IN1", emptyList()), Polarity.MINUS,
        availableLogics = listOf(ChannelLogic(0, "Двери", null), ChannelLogic(1, "Багажник", null)),
        enabledLogics = emptyList()
    )
    private fun channel(id: Int, on: Boolean) = SwitchOutputSetting(
        id,
        "",
        "CH$id",
        null,
        SettingValue.SwitchOutput(on),
        Channel(id, "CH$id", emptyList()),
        Polarity.MINUS,
        emptyList()
    )
    private val timerChannels =
        SettingsSection(50, "4.1.1", "Выбор канала", null, listOf(channel(51, true), channel(52, false)))
    private val tree = SettingsSection(
        0,
        "",
        "",
        null,
        listOf(SettingsSection(1, "3", "Охрана", null, listOf(sensors)), door, timerChannels, can)
    )
    private val can get() = CanCodeSetting(61, "6.1", "CAN-код", null, SettingValue.Can(CanCode("2154", 17)))

    private val repository = FakeSettingsRepository(tree)
    private val sessionRepository = FakeSessionRepository(device)
    private val preferences = FakePreferences()
    private val singleCanDevice = device.copy(can = CanInfo(firmwareVersion = "1.0", code = "2154", isFactory = false))
    private val cars = FakeCars()
    private val local = FakeLocalConfigurations()

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
        source: SettingsSource = SettingsSource.Device,
        options: SettingsEditorOptions = SettingsEditorOptions()
    ): DeviceSettingsViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        // USB: без чтения Bluetooth-модуля при подключении.
        session.attach(ConnectionTransport.USB)
        val vm = DeviceSettingsViewModel(
            source,
            options,
            repository,
            local,
            preferences,
            cars,
            session,
            backgroundScope,
            dispatchers
        )
            .also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        settle()
        return vm
    }

    /**
     * Сессия устройства и сборщики эффектов живут в `backgroundScope`, а `advanceUntilIdle` его работу не ждёт:
     * дотягиваем её `runCurrent` (без бесконечных циклов — несколько проходов достаточно).
     */
    private fun TestScope.settle() {
        repeat(SETTLE_PASSES) {
            advanceUntilIdle()
            runCurrent()
        }
    }

    private fun DeviceSettingsViewModel.root() = (state.value.menu as SettingsMenu.Ready).root

    @Test
    fun `menu is read from the device with reload and the header shows the unit`() = test {
        sessionRepository.device = singleCanDevice
        val vm = viewModel()

        assertEquals(tree, vm.root())
        assertEquals(listOf(true), repository.openReloads)
        assertEquals(SettingsHeader("DX-9X", 1234567, "2154"), vm.state.value.header)
        assertTrue(vm.state.value.changedIds.isEmpty())
    }

    @Test
    fun `load from defaults compares, then puts values into the session and highlights them`() = test {
        repository.snapshotTree = tree.replace(shock.copy(value = SettingValue.Switch(true)))
        val vm = viewModel()
        val effects = mutableListOf<DeviceSettingsEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }

        vm.onLoadRequest()
        assertEquals(SettingsDialog.LoadSources, vm.state.value.dialog)
        vm.onLoadSourcePick(LoadSourceKind.DEFAULTS)
        settle()

        val diff = assertIs<DiffState.Ready>(vm.state.value.diff)
        assertEquals(1, diff.diff.total)
        assertEquals(SettingsSource.Defaults("DX-9X"), repository.snapshotSources.single())
        assertTrue(vm.state.value.changedIds.isEmpty())

        vm.onDiffConfirm()
        settle()

        assertNull(vm.state.value.diff)
        assertEquals(setOf(11), vm.state.value.changedIds)
        assertEquals(listOf<DeviceSettingsEffect>(DeviceSettingsEffect.Loaded(1)), effects)
        assertEquals(1, repository.discarded.size)
    }

    @Test
    fun `configuration of another model asks before comparing`() = test {
        val vm = viewModel()
        val other = SettingsSource.LocalConfiguration(5, "DX-4GS", "Нива")

        vm.onLoadSourcePick(LoadSourceKind.MINE)
        assertEquals(ConfigurationPickKind.MINE, vm.state.value.configurationPicker?.kind)
        vm.onConfigurationPicked(other)
        settle()

        val dialog = assertIs<SettingsDialog.ModelMismatch>(vm.state.value.dialog)
        assertEquals("DX-4GS", dialog.sourceDeviceName)
        assertNull(vm.state.value.diff)

        vm.onMismatchContinue()
        settle()
        assertIs<DiffState.Ready>(vm.state.value.diff)
    }

    @Test
    fun `load that does not fit the unit shows mismatch error`() = test {
        repository.snapshotTree = tree.replace(shock.copy(value = SettingValue.Switch(true)))
        repository.copyResult = Response.Error(SettingsMismatchException(IllegalStateException("size")))
        val vm = viewModel()

        vm.onLoadSourcePick(LoadSourceKind.DEFAULTS)
        settle()
        vm.onDiffConfirm()
        settle()

        assertEquals(SettingsDialog.LoadError(LoadErrorKind.MISMATCH), vm.state.value.dialog)
        assertTrue(vm.state.value.changedIds.isEmpty())
    }

    @Test
    fun `save to my configurations copies the current session`() = test {
        val vm = viewModel()

        vm.onSaveToRequest()
        vm.onSaveTargetPick(SaveTarget.MINE)
        assertNotNull(vm.state.value.saveForm)
        vm.onSaveFormChange("Нива без автозапуска", "")
        vm.onSaveFormSubmit()
        settle()

        assertEquals(
            listOf<Pair<String, SettingsSource>>("Нива без автозапуска" to SettingsSource.Device),
            local.created
        )
        assertNull(vm.state.value.saveForm)
    }

    @Test
    fun `my configuration saves itself, read-only source saves as new`() = test {
        val mine = viewModel(SettingsSource.LocalConfiguration(3, "DX-9X", "Моя"))
        assertEquals(BarAction.SAVE, mine.state.value.barAction)
        mine.onValueChange(11, SettingValue.Switch(true))
        settle()
        mine.onBarAction()
        settle()
        assertEquals(1, repository.saveCalls)
        assertTrue(mine.state.value.changedIds.isEmpty())

        val cloud = viewModel(SettingsSource.RemoteConfiguration(7, "DX-9X", "Облачная"))
        assertEquals(BarAction.SAVE_AS, cloud.state.value.barAction)
        assertTrue(cloud.state.value.source.isReadOnly)
        cloud.onValueChange(12, SettingValue.Integer(60))
        settle()
        cloud.onExitRequest()
        assertEquals(SettingsDialog.ExitSave, cloud.state.value.dialog)
        cloud.onExitSave()
        assertEquals(SettingsDialog.SaveTargets(exitAfter = true), cloud.state.value.dialog)
    }

    @Test
    fun `read error shows error and retry reads again`() = test {
        repository.openResult = Response.Error(IllegalStateException("timeout"))
        val vm = viewModel()
        assertIs<SettingsMenu.Error>(vm.state.value.menu)

        repository.openResult = null
        vm.onRetry()
        settle()

        assertEquals(tree, vm.root())
    }

    @Test
    fun `inline change is applied in the session and highlighted with section counters`() = test {
        val vm = viewModel()

        vm.onValueChange(11, SettingValue.Switch(true))
        settle()

        assertEquals(SettingValue.Switch(true), (vm.root().findNode(11) as Setting).value)
        assertEquals(setOf(11), vm.state.value.changedIds)
        // Корень (id 0) тоже считается: по нему помечается крошка «Все».
        assertEquals(mapOf(0 to 1, 1 to 1, 10 to 1), vm.state.value.sectionChanges)
    }

    @Test
    fun `returning the original value removes the highlight even if sdk still lists it as unsaved`() = test {
        val vm = viewModel()
        vm.onValueChange(12, SettingValue.Integer(45))
        settle()

        vm.onValueChange(12, SettingValue.Integer(30))
        settle()

        assertTrue(12 in repository.unsaved)
        assertTrue(vm.state.value.changedIds.isEmpty())
    }

    @Test
    fun `editor keeps the original value and applies the draft only on done`() = test {
        val vm = viewModel()

        vm.onSettingClick(12)
        vm.onDraftChange(SettingValue.Integer(60))
        assertEquals(SettingValue.Integer(30), vm.state.value.editing?.original)
        assertTrue(repository.setCalls.isEmpty())

        vm.onEditApply()
        settle()

        assertNull(vm.state.value.editing)
        assertEquals(listOf<Pair<Int, SettingValue>>(12 to SettingValue.Integer(60)), repository.setCalls)
    }

    @Test
    fun `out of range draft is not applied`() = test {
        val vm = viewModel()
        vm.onSettingClick(12)
        vm.onDraftChange(SettingValue.Integer(500))

        vm.onEditApply()
        settle()

        assertTrue(repository.setCalls.isEmpty())
    }

    @Test
    fun `switch does not open the editor`() = test {
        val vm = viewModel()
        vm.onSettingClick(11)
        assertNull(vm.state.value.editing)
    }

    @Test
    fun `write saves to the device, resets changes and reports the count`() = test {
        val vm = viewModel()
        val effects = mutableListOf<DeviceSettingsEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        vm.onValueChange(11, SettingValue.Switch(true))
        settle()

        vm.onWrite()
        settle()

        assertEquals(1, repository.saveCalls)
        assertTrue(vm.state.value.changedIds.isEmpty())
        assertEquals(false, vm.state.value.isWriting)
        assertEquals(listOf<DeviceSettingsEffect>(DeviceSettingsEffect.Written(1)), effects)
    }

    @Test
    fun `failed write keeps changes and shows the error dialog`() = test {
        val vm = viewModel()
        vm.onValueChange(11, SettingValue.Switch(true))
        settle()
        repository.saveResult = Response.Error(IllegalStateException("timeout"))

        vm.onWrite()
        settle()

        assertEquals(SettingsDialog.WriteError, vm.state.value.dialog)
        assertEquals(setOf(11), vm.state.value.changedIds)
        assertEquals(false, vm.state.value.isWriting)
    }

    @Test
    fun `exit with changes asks for confirmation, without changes exits`() = test {
        val vm = viewModel()
        val effects = mutableListOf<DeviceSettingsEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }

        vm.onExitRequest()
        runCurrent()
        assertEquals(listOf<DeviceSettingsEffect>(DeviceSettingsEffect.Exit(ExitTarget.BACK)), effects)

        vm.onValueChange(11, SettingValue.Switch(true))
        settle()
        vm.onExitRequest()
        assertEquals(SettingsDialog.ExitConfirm(ExitTarget.BACK), vm.state.value.dialog)

        vm.onExitWrite()
        settle()
        assertEquals(1, repository.saveCalls)
        assertEquals(DeviceSettingsEffect.Exit(ExitTarget.BACK), effects.last())
    }

    @Test
    fun `after reconnect the menu is read again and unsaved changes are dropped`() = test {
        val vm = viewModel()
        vm.onValueChange(11, SettingValue.Switch(true))
        settle()

        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        settle()
        repository.reset()
        sessionRepository.link.value = DeviceLinkState.READY
        settle()

        // Чтения из блока (reload) — при входе и после переподключения; без reload — перечитывание после правки.
        assertEquals(listOf(true, true), repository.openReloads.filter { it })
        assertEquals(tree, vm.root())
        assertTrue(vm.state.value.changedIds.isEmpty())
    }

    @Test
    fun `tree width is restored from preferences and saved after resize`() = test {
        preferences.width = 300
        val vm = viewModel()
        assertEquals(300, vm.state.value.treeWidthDp)

        vm.onTreeWidthChange(360)

        assertEquals(360, vm.state.value.treeWidthDp)
        assertEquals(360, preferences.width)
    }

    @Test
    fun `input functions are edited in the window and applied on done`() = test {
        val vm = viewModel()

        vm.onSettingClick(41)
        assertEquals(
            SettingValue.Input(InputValue(listOf(true, false), InputContactType.NORMALLY_OPEN)),
            vm.state.value.editing?.original
        )
        val draft = SettingValue.Input(InputValue(listOf(true, true), InputContactType.NORMALLY_CLOSED))
        vm.onDraftChange(draft)
        vm.onEditApply()
        settle()

        assertEquals(listOf<Pair<Int, SettingValue>>(41 to draft), repository.setCalls)
        assertEquals(draft, (vm.root().findNode(41) as Setting).value)
        assertEquals(setOf(41), vm.state.value.changedIds)
    }

    @Test
    fun `settings changed by the sdk as a side effect are shown after a change`() = test {
        val vm = viewModel()

        vm.onValueChange(52, SettingValue.SwitchOutput(true))
        settle()

        assertEquals(SettingValue.SwitchOutput(false), (vm.root().findNode(51) as Setting).value)
        assertEquals(SettingValue.SwitchOutput(true), (vm.root().findNode(52) as Setting).value)
        // SDK считает несохранённой только явно изменённую настройку (как в фейке): подсвечивается она.
        assertEquals(setOf(52), vm.state.value.changedIds)
    }

    @Test
    fun `car of the stored can code is loaded for the row caption`() = test {
        val vm = viewModel()
        assertEquals("Polo", vm.state.value.cars[17]?.model)
    }

    @Test
    fun `single car found by code is attached to the code`() = test {
        val vm = viewModel()
        vm.onSettingClick(61)
        vm.onDraftChange(SettingValue.Can(CanCode("7001", null)))

        vm.onCarSearch()
        settle()

        val editing = assertNotNull(vm.state.value.editing)
        assertEquals(SettingValue.Can(CanCode("7001", 21)), editing.draft)
        assertEquals(CarSearch.Found(listOf(cars.rio)), editing.carSearch)
    }

    @Test
    fun `several cars are offered to pick, none or no server keeps the code without a car`() = test {
        val vm = viewModel()
        vm.onSettingClick(61)
        vm.onDraftChange(SettingValue.Can(CanCode("7002", null)))
        vm.onCarSearch()
        settle()
        assertEquals(SettingValue.Can(CanCode("7002", null)), vm.state.value.editing?.draft)
        vm.onCarPick(22)
        assertEquals(SettingValue.Can(CanCode("7002", 22)), vm.state.value.editing?.draft)

        vm.onDraftChange(SettingValue.Can(CanCode("9999", null)))
        assertEquals(CarSearch.Idle, vm.state.value.editing?.carSearch)
        cars.fail = true
        vm.onCarSearch()
        settle()
        assertEquals(CarSearch.Failed(RequestError.Failed), vm.state.value.editing?.carSearch)
    }

    @Test
    fun `car picked from the catalog sets code and car in the draft, cancel keeps the draft`() = test {
        val vm = viewModel()
        vm.onSettingClick(61)
        val before = vm.state.value.editing?.draft

        vm.onVehiclePickerOpen()
        assertTrue(vm.state.value.vehiclePicker)
        vm.onVehiclePickerClose()
        assertFalse(vm.state.value.vehiclePicker)
        assertEquals(before, vm.state.value.editing?.draft)

        vm.onVehiclePickerOpen()
        vm.onCarPicked(cars.solaris)

        val state = vm.state.value
        assertFalse(state.vehiclePicker)
        assertEquals(SettingValue.Can(CanCode("7002", 22)), state.editing?.draft)
        assertEquals(cars.solaris, state.cars[22])
    }

    @Test
    fun `factory reset asks for confirmation, resets the unit and reads the menu again`() = test {
        val vm = viewModel()
        val effects = mutableListOf<DeviceSettingsEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        vm.onValueChange(11, SettingValue.Switch(true))
        settle()

        vm.onResetRequest()
        assertEquals(SettingsDialog.ResetConfirm, vm.state.value.dialog)
        assertEquals(0, repository.resetCalls)

        vm.onResetConfirm()
        settle()

        assertEquals(1, repository.resetCalls)
        assertEquals(false, vm.state.value.isResetting)
        assertEquals(tree, vm.root())
        assertTrue(vm.state.value.changedIds.isEmpty())
        assertTrue(DeviceSettingsEffect.ResetDone in effects)
    }

    @Test
    fun `failed factory reset shows the error and keeps the menu`() = test {
        val vm = viewModel()
        repository.resetResult = Response.Error(IllegalStateException("timeout"))

        vm.onResetRequest()
        vm.onResetConfirm()
        settle()

        assertEquals(SettingsDialog.ResetError, vm.state.value.dialog)
        assertEquals(false, vm.state.value.isResetting)
        assertEquals(tree, vm.root())
    }

    @Test
    fun `can code on a single can firmware unit leads to the firmware update instead of the editor`() = test {
        sessionRepository.device = singleCanDevice
        val vm = viewModel()
        val effects = mutableListOf<DeviceSettingsEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }

        vm.onSettingClick(61)
        assertNull(vm.state.value.editing)
        assertEquals(SettingsDialog.SingleCanFirmware, vm.state.value.dialog)

        vm.onSingleCanUpdate()
        runCurrent()
        assertEquals(listOf<DeviceSettingsEffect>(DeviceSettingsEffect.Exit(ExitTarget.FIRMWARE_UPDATE)), effects)
    }

    @Test
    fun `going to the firmware update with unsaved changes asks first and keeps the target`() = test {
        sessionRepository.device = singleCanDevice
        val vm = viewModel()
        val effects = mutableListOf<DeviceSettingsEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        vm.onValueChange(11, SettingValue.Switch(true))
        settle()

        vm.onSettingClick(61)
        vm.onSingleCanUpdate()
        assertEquals(SettingsDialog.ExitConfirm(ExitTarget.FIRMWARE_UPDATE), vm.state.value.dialog)

        vm.onExitDiscard()
        runCurrent()
        assertEquals(DeviceSettingsEffect.Exit(ExitTarget.FIRMWARE_UPDATE), effects.last())
    }

    @Test
    fun `search finds settings by number`() = test {
        val vm = viewModel()
        vm.onSearchQueryChange("3.2.4")
        settle()

        assertEquals(listOf(12), vm.state.value.searchHits.map { it.node.id })
    }
}

/** Сессия SDK в памяти: правки меняют дерево и попадают в unsaved (SDK не убирает вернувшиеся значения). */
private class FakeSettingsRepository(private val initial: SettingsSection) : DeviceSettingsRepository {
    var current = initial
    val unsaved = mutableSetOf<Int>()
    var openResult: Response<SettingsSection>? = null
    var saveResult: Response<Unit> = Response.Success(Unit)
    val openReloads = mutableListOf<Boolean>()
    val setCalls = mutableListOf<Pair<Int, SettingValue>>()
    var saveCalls = 0

    /** Блок перечитан после переподключения: правки сессии потеряны. */
    fun reset() {
        current = initial
        unsaved.clear()
    }

    override suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection> {
        if (source is SettingsSource.File) return Response.Success(snapshotTree)
        openReloads += reload
        return openResult ?: Response.Success(current)
    }

    /** Меню снимка конфигурации (что загрузится). */
    var snapshotTree: SettingsSection = initial
    var copyResult: Response<SettingsSection>? = null
    val snapshotSources = mutableListOf<SettingsSource>()
    val discarded = mutableListOf<SettingsSource.File>()

    override suspend fun close(source: SettingsSource): Response<Unit> = Response.Success(Unit)

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> {
        setCalls += settingId to value
        val updated = when (val node: SettingNode? = current.findNode(settingId)) {
            is SwitchSetting -> node.copy(value = value as SettingValue.Switch)

            is IntegerSetting -> node.copy(value = value as SettingValue.Integer)

            is InputSetting -> node.copy(value = value as SettingValue.Input)

            is SwitchOutputSetting -> {
                // Как выбор таймерного канала: включение одного выключает остальные каналы раздела.
                if ((value as SettingValue.SwitchOutput).enabled) {
                    current.settings().filterIsInstance<SwitchOutputSetting>().filter { it.id != settingId }.forEach {
                        current = current.replace(it.copy(value = SettingValue.SwitchOutput(false)))
                    }
                }
                node.copy(value = value)
            }

            else -> return Response.Error(IllegalStateException("unsupported"))
        }
        current = current.replace(updated)
        unsaved += settingId
        return Response.Success(updated)
    }

    override suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>> = Response.Success(unsaved.toSet())

    override suspend fun save(source: SettingsSource): Response<Unit> {
        saveCalls++
        if (saveResult is Response.Success) unsaved.clear()
        return saveResult
    }

    // Копирование снимка в сессию блока: значения конфигурации, unsaved SDK не меняется.
    override suspend fun copy(
        from: SettingsSource,
        to: SettingsSource,
        saveChanges: Boolean
    ): Response<SettingsSection> {
        copyResult?.let { return it }
        if (from is SettingsSource.File) current = snapshotTree
        return Response.Success(current)
    }

    override suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File> {
        snapshotSources += from
        return Response.Success(SettingsSource.File("snapshot.dxs", deviceName))
    }

    override suspend fun discardSnapshot(snapshot: SettingsSource.File) {
        discarded += snapshot
    }

    var resetResult: Response<Unit> = Response.Success(Unit)
    var resetCalls = 0

    override suspend fun resetDeviceToFactory(): Response<Unit> {
        resetCalls++
        if (resetResult is Response.Success) reset()
        return resetResult
    }
}

private class FakeLocalConfigurations : LocalConfigurationsRepository {
    val created = mutableListOf<Pair<String, SettingsSource>>()

    override val changes: Flow<Unit> = emptyFlow()

    override suspend fun all(): Response<List<LocalConfiguration>> = Response.Success(emptyList())

    override suspend fun create(
        name: String,
        description: String,
        deviceName: String,
        from: SettingsSource
    ): Response<SettingsSource.LocalConfiguration> {
        created += name to from
        return Response.Success(SettingsSource.LocalConfiguration(created.size.toLong(), deviceName, name))
    }

    override suspend fun updateInfo(configuration: LocalConfiguration, name: String, description: String) =
        Response.Error(IllegalStateException("not used"))

    override suspend fun delete(id: Long): Response<Unit> = Response.Success(Unit)
}

private class FakeSessionRepository(var device: AlarmDevice) : DeviceSessionRepository {
    val link = MutableStateFlow(DeviceLinkState.READY)

    override fun linkState(): Flow<DeviceLinkState> = link
    override suspend fun readBluetoothModule(): Response<BluetoothModule> = Response.Error(IllegalStateException())
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
    override suspend fun readImei(): Response<String> = Response.Success("")
    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
    override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> =
        Response.Success(BootModeResult.Entered)
    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
    override suspend fun setServicePin(pin: ServicePin): Response<Unit> = Response.Success(Unit)
}

private class FakePreferences : DeviceSettingsPreferences {
    var width: Int? = null
    override fun treeWidthDp(): Int? = width
    override fun setTreeWidthDp(width: Int) {
        this.width = width
    }
}

private class FakeCars : VehicleCatalogRepository {
    val polo = CarSummary(17, "Volkswagen", "Polo", "V", 2015..2020, "7000", null)
    val rio = CarSummary(21, "Kia", "Rio", "IV", 2017..2020, "7001", null)
    val solaris = CarSummary(22, "Hyundai", "Solaris", "II", 2017..2020, "7002", null)
    var fail = false

    override suspend fun findByCanCode(
        canCode: String,
        device: VehicleDeviceFilter?,
        groups: Set<CarFeatureGroup>
    ): Response<List<CarSummary>> = when {
        fail -> Response.Error(IllegalStateException("no server"))
        canCode == "7001" -> Response.Success(listOf(rio))
        canCode == "7002" -> Response.Success(listOf(rio, solaris))
        else -> Response.Success(emptyList())
    }

    override suspend fun summary(carId: Int): Response<CarSummary> =
        listOf(polo, rio, solaris).firstOrNull { it.id == carId }?.let { Response.Success(it) }
            ?: Response.Error(IllegalStateException("no car"))

    override suspend fun brands(query: CatalogQuery): Response<List<CarBrand>> = unused()
    override suspend fun models(query: CatalogQuery, brandId: Int): Response<List<CarModel>> = unused()
    override suspend fun trims(query: CatalogQuery, modelId: Int): Response<List<CarTrim>> = unused()
    override suspend fun card(carId: Int, groups: Set<CarFeatureGroup>): Response<CarCard> = unused()

    private fun unused(): Response.Error = Response.Error(UnsupportedOperationException("not used by settings"))
}

private class FakeConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}

private const val SETTLE_PASSES = 5
