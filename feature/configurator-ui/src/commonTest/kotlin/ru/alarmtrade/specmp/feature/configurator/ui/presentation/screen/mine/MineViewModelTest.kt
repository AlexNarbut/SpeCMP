package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeLocalConfigurations
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeSettingsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.localConfiguration
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.testDeviceSession
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.testDispatchers
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaveResult

class MineViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val link = MutableStateFlow(DeviceLinkState.READY)
    private val configurations = FakeLocalConfigurations()
    private val settings = FakeSettingsRepository()
    private val niva = localConfiguration(1, "Нива", deviceName = "DX-9X")
    private val camry = localConfiguration(2, "Камри", deviceName = "DX-4GS")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        configurations.items = Response.Success(listOf(niva, camry))
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(pickDeviceName: String? = null): Pair<MineViewModel, List<MineEffect>> {
        val vm = MineViewModel(
            pickDeviceName = pickDeviceName,
            configurations = configurations,
            settings = settings,
            sessions = settings,
            deviceSession = testDeviceSession(link, dispatcher),
            dispatchers = testDispatchers(dispatcher),
        )
        val effects = mutableListOf<MineEffect>()
        backgroundScope.launch { vm.state.collect {} }
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }
        advanceUntilIdle()
        return vm to effects
    }

    @Test
    fun `list loads with derived lists and connected unit`() = runTest(dispatcher) {
        val (vm, _) = viewModel()

        val state = vm.state.value
        assertEquals(LCEState.Content(listOf(niva, camry)), state.list)
        assertEquals(listOf(niva, camry), state.filtered)
        assertEquals(listOf("DX-4GS", "DX-9X"), state.models)
        assertEquals("DX-9X", state.connectedDeviceName)
        assertFalse(state.pickMode)
    }

    @Test
    fun `pick mode starts filtered by the unit model`() = runTest(dispatcher) {
        val (vm, _) = viewModel(pickDeviceName = "DX-4GS")

        assertTrue(vm.state.value.pickMode)
        assertEquals(listOf(camry), vm.state.value.filtered)
    }

    @Test
    fun `storage change reloads and drops a vanished selection`() = runTest(dispatcher) {
        val (vm, _) = viewModel()
        vm.onSelect(2)
        assertEquals(camry, vm.state.value.selected)

        configurations.items = Response.Success(listOf(niva))
        configurations.changes.tryEmit(Response.Success(Unit))

        assertEquals(listOf(niva), vm.state.value.filtered)
        assertNull(vm.state.value.selectedId)
    }

    @Test
    fun `retry after error loads again`() = runTest(dispatcher) {
        configurations.items = Response.Error(IllegalStateException("disk"))
        val (vm, _) = viewModel()
        assertIs<LCEState.Error<*>>(vm.state.value.list)

        configurations.items = Response.Success(listOf(niva))
        vm.onRetry()

        assertEquals(LCEState.Content(listOf(niva)), vm.state.value.list)
    }

    @Test
    fun `search, filter, paging and reset`() = runTest(dispatcher) {
        val (vm, _) = viewModel()

        vm.onLoadMore()
        assertEquals(MineState.PAGE_SIZE * 2, vm.state.value.shown)
        vm.onQueryChange("кам")
        assertEquals(listOf(camry), vm.state.value.filtered)
        assertEquals(MineState.PAGE_SIZE, vm.state.value.shown)
        vm.onModelFilter("DX-9X")
        assertEquals(emptyList(), vm.state.value.filtered)
        vm.onResetFilter()
        assertEquals(listOf(niva, camry), vm.state.value.filtered)
    }

    @Test
    fun `rename submits and selects the new copy`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()

        vm.onRenameRequest(niva)
        vm.onRenameChange(" Нива 2 ", "")
        vm.onRenameSubmit()

        assertEquals(1001L, vm.state.value.selectedId)
        assertNull(vm.state.value.dialog)
        assertFalse(vm.state.value.busy)
        assertEquals(listOf<MineEffect>(MineEffect.Message(MineMessage.RENAMED)), effects)
    }

    @Test
    fun `copy reports the created configuration and closes the source session`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()

        vm.onCopy(niva, "Копия — Нива")

        assertEquals(
            listOf<MineEffect>(MineEffect.Copied(SettingsSource.LocalConfiguration(100, "DX-9X", "Новая"))),
            effects,
        )
        assertEquals(listOf<SettingsSource>(SettingsSource.LocalConfiguration(1, "DX-9X", "Нива")), settings.closed)
    }

    @Test
    fun `delete confirms through the dialog`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()

        vm.onDeleteRequest(camry)
        vm.onDeleteConfirm()

        assertEquals(listOf(2L), configurations.deleted)
        assertEquals(listOf<MineEffect>(MineEffect.Message(MineMessage.DELETED)), effects)
    }

    @Test
    fun `export hands the snapshot to the saver, result discards it`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()
        val snapshot = SettingsSource.File("/tmp/snapshot.dxs", "DX-9X")

        vm.onExport(niva)
        assertEquals(listOf<MineEffect>(MineEffect.ExportFile(snapshot.path, effects.exportName())), effects)
        assertTrue(vm.state.value.busy)
        vm.onExport(camry)
        assertEquals(1, effects.size)

        vm.onFileExported(SettingsFileSaveResult.Saved("Нива.dxs"))
        advanceUntilIdle()

        assertFalse(vm.state.value.busy)
        assertEquals(listOf(snapshot), settings.discarded)
        assertEquals(MineEffect.FileSaved("Нива.dxs"), effects.last())
    }

    @Test
    fun `failed export snapshot reports and frees the screen`() = runTest(dispatcher) {
        settings.snapshot = Response.Error(IllegalStateException("io"))
        val (vm, effects) = viewModel()

        vm.onExport(niva)

        assertFalse(vm.state.value.busy)
        assertEquals(listOf<MineEffect>(MineEffect.Message(MineMessage.FAILED)), effects)
        vm.onFileExported(SettingsFileSaveResult.Cancelled)
        assertEquals(emptyList(), settings.discarded)
    }

    private fun List<MineEffect>.exportName(): String = (first() as MineEffect.ExportFile).fileName
}
