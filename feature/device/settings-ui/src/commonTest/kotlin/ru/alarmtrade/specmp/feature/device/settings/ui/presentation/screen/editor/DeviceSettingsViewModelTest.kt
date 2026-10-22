package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

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
import ru.alarmtrade.specmp.feature.device.settings.domain.model.IntegerSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

class DeviceSettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val viewModels = mutableListOf<DeviceSettingsViewModel>()

    private val device = AlarmDevice(
        systemId = 1234567,
        systemName = "DX-9X",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = emptySet(),
        can = CanInfo(firmwareVersion = "1.0", code = "2154", isFactory = false)
    )

    private val shock = SwitchSetting(11, "3.2.1", "Датчик удара", null, SettingValue.Switch(false), null, null)
    private val delay = IntegerSetting(12, "3.2.4", "Задержка", null, SettingValue.Integer(30), 0..120, "с", null, null)
    private val sensors = SettingsSection(10, "3.2", "Датчики", null, listOf(shock, delay))
    private val tree =
        SettingsSection(0, "", "", null, listOf(SettingsSection(1, "3", "Охрана", null, listOf(sensors))))

    private val repository = FakeSettingsRepository(tree)
    private val sessionRepository = FakeSessionRepository(device)
    private val preferences = FakePreferences()

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

    private fun TestScope.viewModel(): DeviceSettingsViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        // USB: без чтения Bluetooth-модуля при подключении.
        session.attach(ConnectionTransport.USB)
        val vm = DeviceSettingsViewModel(
            SettingsSource.Device,
            repository,
            preferences,
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
        val vm = viewModel()

        assertEquals(tree, vm.root())
        assertEquals(listOf(true), repository.openReloads)
        assertEquals(SettingsHeader("DX-9X", 1234567, "2154"), vm.state.value.header)
        assertTrue(vm.state.value.changedIds.isEmpty())
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
        assertEquals(listOf<DeviceSettingsEffect>(DeviceSettingsEffect.Exit), effects)

        vm.onValueChange(11, SettingValue.Switch(true))
        settle()
        vm.onExitRequest()
        assertEquals(SettingsDialog.ExitConfirm, vm.state.value.dialog)

        vm.onExitWrite()
        settle()
        assertEquals(1, repository.saveCalls)
        assertEquals(DeviceSettingsEffect.Exit, effects.last())
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

        assertEquals(listOf(true, true), repository.openReloads)
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
        openReloads += reload
        return openResult ?: Response.Success(current)
    }

    override suspend fun close(source: SettingsSource): Response<Unit> = Response.Success(Unit)

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> {
        setCalls += settingId to value
        val updated = when (val node: SettingNode? = current.findNode(settingId)) {
            is SwitchSetting -> node.copy(value = value as SettingValue.Switch)
            is IntegerSetting -> node.copy(value = value as SettingValue.Integer)
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

    override suspend fun copy(from: SettingsSource, to: SettingsSource, saveChanges: Boolean) =
        Response.Error(IllegalStateException("not used"))

    override suspend fun resetDeviceToFactory(): Response<Unit> = Response.Success(Unit)
}

private class FakeSessionRepository(var device: AlarmDevice) : DeviceSessionRepository {
    val link = MutableStateFlow(DeviceLinkState.READY)

    override fun linkState(): Flow<DeviceLinkState> = link
    override suspend fun readBluetoothModule(): Response<BluetoothModule> = Response.Error(IllegalStateException())
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
    override suspend fun readImei(): Response<String> = Response.Success("")
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

private class FakeConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}

private const val SETTLE_PASSES = 5
