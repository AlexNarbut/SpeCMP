package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloud

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
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeCloudRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeLocalConfigurations
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.FakeSettingsRepository
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.cloudSummary
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.testDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

class CloudCardViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val link = MutableStateFlow(DeviceLinkState.READY)
    private val cloud = FakeCloudRepository()
    private val settings = FakeSettingsRepository()
    private val configurations = FakeLocalConfigurations()
    private val image = ConfigurationAttachment("Схема", "https://x/s.png", "https://x/full.png")
    private val pdf = ConfigurationAttachment("Карта", null, "https://x/map.pdf?v=1")
    private val card = CloudConfiguration(cloudSummary(3), "<p>html</p>", null, listOf(image, pdf))
    private val source = SettingsSource.RemoteConfiguration(3, "DX-9X", "Конфигурация 3")

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        cloud.card = Response.Success(card)
        settings.open = Response.Success(
            SettingsSection(
                0,
                "",
                "",
                null,
                listOf(CanCodeSetting(61, "6.1", "CAN-код", null, SettingValue.Can(CanCode("2154", 17)))),
            ),
        )
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun TestScope.viewModel(firmwareCanCode: String? = null): Pair<CloudCardViewModel, List<CloudCardEffect>> {
        val vm = CloudCardViewModel(
            id = 3,
            firmwareCanCode = firmwareCanCode,
            cloud = cloud,
            settings = settings,
            configurations = configurations,
            deviceSession = testDeviceSession(FakeDeviceSessionRepository(testDevice, link), dispatcher = dispatcher),
            dispatchers = testDispatchers(dispatcher),
        )
        val effects = mutableListOf<CloudCardEffect>()
        backgroundScope.launch { vm.state.collect {} }
        backgroundScope.launch { vm.sideEffect.collect { effects += it } }
        advanceUntilIdle()
        return vm to effects
    }

    @Test
    fun `card loads with can code read from its settings`() = runTest(dispatcher) {
        val (vm, _) = viewModel(firmwareCanCode = "2154")

        val state = vm.state.value
        assertEquals(LCEState.Content(card), state.card)
        assertEquals("2154", state.canCode)
        assertFalse(state.canMismatch)
        assertEquals("DX-9X", state.connectedDeviceName)
        assertEquals(listOf<SettingsSource>(source), settings.closed)
    }

    @Test
    fun `different can firmware is a mismatch`() = runTest(dispatcher) {
        val (vm, _) = viewModel(firmwareCanCode = "1000")

        assertTrue(vm.state.value.canMismatch)
    }

    @Test
    fun `retry after error loads the card`() = runTest(dispatcher) {
        cloud.card = Response.Error(IllegalStateException("offline"))
        val (vm, _) = viewModel()
        assertIs<LCEState.Error<*>>(vm.state.value.card)

        cloud.card = Response.Success(card)
        vm.onRetry()

        assertEquals(LCEState.Content(card), vm.state.value.card)
    }

    @Test
    fun `pdf opens by url, picture opens the viewer`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()

        vm.onAttachmentOpen(pdf)
        vm.onAttachmentOpen(image)

        assertEquals(listOf<CloudCardEffect>(CloudCardEffect.OpenUrl(pdf.url)), effects)
        assertEquals(image, vm.state.value.viewer)
        vm.onViewerClose()
        assertNull(vm.state.value.viewer)
    }

    @Test
    fun `save to mine creates a configuration from the card`() = runTest(dispatcher) {
        val (vm, effects) = viewModel()

        vm.onSaveRequest()
        assertEquals(CloudSaveForm("Конфигурация 3", "Описание 3"), vm.state.value.saveForm)
        vm.onSaveChange(" Нива ", "")
        vm.onSaveSubmit()

        assertEquals(listOf<Pair<String, SettingsSource>>("Нива" to source), configurations.createCalls)
        assertEquals(
            listOf<CloudCardEffect>(
                CloudCardEffect.SavedToMine(SettingsSource.LocalConfiguration(100, "DX-9X", "Новая")),
            ),
            effects,
        )
        assertNull(vm.state.value.saveForm)
        assertFalse(vm.state.value.busy)
    }

    @Test
    fun `failed save reports and blank name is ignored`() = runTest(dispatcher) {
        configurations.created = Response.Error(IllegalStateException("disk"))
        val (vm, effects) = viewModel()

        vm.onSaveRequest()
        vm.onSaveChange(" ", "")
        vm.onSaveSubmit()
        assertEquals(emptyList(), configurations.createCalls)

        vm.onSaveChange("Нива", "")
        vm.onSaveSubmit()
        assertEquals(listOf<CloudCardEffect>(CloudCardEffect.SaveFailed), effects)
    }

    @Test
    fun `description toggles`() = runTest(dispatcher) {
        val (vm, _) = viewModel()

        vm.onDescriptionToggle()
        assertTrue(vm.state.value.descriptionExpanded)
        vm.onSaveRequest()
        vm.onSaveDismiss()
        assertNull(vm.state.value.saveForm)
    }
}
