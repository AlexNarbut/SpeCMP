package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.models

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
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
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation.SourceKey
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeModelsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeSettingsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.testDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

class ModelsViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val link = MutableStateFlow(DeviceLinkState.READY)
    private val models = FakeModelsRepository()
    private val settings = FakeSettingsRepository()
    private val file = SourceKey.File("/files/a.dxs", "", "a.dxs", inPlace = true)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(file: SourceKey.File? = null): Pair<ModelsViewModel, List<ModelsEffect>> {
        val vm =
            ModelsViewModel(
                file,
                models,
                settings,
                testDeviceSession(FakeDeviceSessionRepository(testDevice, link), dispatcher = dispatcher),
                testDispatchers(dispatcher),
            )
        val effects = mutableListOf<ModelsEffect>()
        backgroundScope.launch { vm.state.collect {} }
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }
        advanceUntilIdle()
        return vm to effects
    }

    @Test
    fun `first subscription loads recent and models, connected unit is on top`() = runTest(dispatcher) {
        val (vm, _) = viewModel(file)

        val state = vm.state.value
        assertEquals(LCEState.Content(listOf("DX-4GS", "DX-9X")), state.list)
        assertEquals(listOf("DX-4GS"), state.recent)
        assertEquals("DX-9X", state.connected)
        assertEquals("a.dxs", state.fileName)
    }

    @Test
    fun `retry after error reloads models`() = runTest(dispatcher) {
        models.models = Response.Error(IllegalStateException("offline"))
        val (vm, _) = viewModel()
        assertIs<LCEState.Error<*>>(vm.state.value.list)

        models.models = Response.Success(listOf("DX-9X"))
        vm.onRetry()

        assertEquals(LCEState.Content(listOf("DX-9X")), vm.state.value.list)
        assertEquals(2, models.modelsCalls)
    }

    @Test
    fun `pick from scratch opens defaults and moves the model to recent`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()

        vm.onPick("DX-9X")

        assertEquals(listOf<ModelsEffect>(ModelsEffect.Open(SourceKey.Defaults("DX-9X"))), effects)
        assertEquals(listOf("DX-9X", "DX-4GS"), vm.state.value.recent)
    }

    @Test
    fun `pick for a file checks it in the model and opens it`() = runTest(dispatcher) {
        val (vm, effects) = viewModel(file)

        vm.onPick("DX-9X")

        val source = SettingsSource.File("/files/a.dxs", "DX-9X", "a.dxs", inPlace = true)
        assertEquals(listOf<SettingsSource>(source), settings.opened)
        assertEquals(listOf<SettingsSource>(source), settings.closed)
        assertEquals(listOf<ModelsEffect>(ModelsEffect.Open(file.copy(deviceName = "DX-9X"))), effects)
        assertFalse(vm.state.value.checking)
    }

    @Test
    fun `file that does not open shows the error and dismiss clears it`() = runTest(dispatcher) {
        settings.open = Response.Error(IllegalStateException("broken"))
        val (vm, effects) = viewModel(file)

        vm.onPick("DX-4GS")

        assertEquals("DX-4GS", vm.state.value.fileError)
        assertEquals(emptyList(), effects)
        vm.onDialogDismiss()
        assertNull(vm.state.value.fileError)
    }

    @Test
    fun `query and recent removal update the state`() = runTest(dispatcher) {
        val (vm, _) = viewModel()

        vm.onQueryChange("9x")
        vm.onRecentRemove("DX-4GS")

        assertEquals("9x", vm.state.value.query)
        assertEquals(emptyList(), vm.state.value.recent)
    }
}
