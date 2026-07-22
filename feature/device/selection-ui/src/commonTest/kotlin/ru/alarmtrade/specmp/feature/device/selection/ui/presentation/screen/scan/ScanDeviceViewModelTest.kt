package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScanDeviceViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun dispatchers() = AppDispatchers(
        io = testDispatcher,
        default = testDispatcher,
        main = testDispatcher,
    )

    private fun viewModel(
        scan: FakeDeviceScanRepository = FakeDeviceScanRepository(),
        connection: FakeDeviceConnectionRepository = FakeDeviceConnectionRepository(),
        alarm: FakeAlarmDeviceRepository = FakeAlarmDeviceRepository(),
        permissions: FakePermissionsProvider = FakePermissionsProvider(),
    ) = ScanDeviceViewModel(scan, connection, alarm, permissions, dispatchers())

    @Test
    fun `scanning shows found devices as content`() = runTest(testDispatcher) {
        // На jvm deviceAccessRequirements() пуст → начальный шаг сразу Scanning,
        // реактивный скан стартует при первой подписке на state (без start-интента).
        val bleDevice = ScannedDevice.Ble(
            id = "AA:BB",
            name = "Pandora",
            deviceType = "ALARM",
            mode = ScannedDeviceMode.BASE,
            hasDeviceBond = false,
            hasPhoneBond = false,
        )
        val viewModel = viewModel(scan = FakeDeviceScanRepository(ble = listOf(bleDevice)))
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
        val devices = viewModel.state.value.devices
        assertTrue(devices is LCEState.Content)
        assertEquals(1, devices.content.size)
    }

    @Test
    fun `connecting to working ble device emits Connected with device info`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val viewModel = viewModel()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(
            DeviceUiItem.Ble(
                id = "AA:BB",
                name = "Pandora",
                address = "AC:BB:CC:DD:EE:FF",
                mode = ScannedDeviceMode.BASE,
                hasDeviceBond = false,
                hasPhoneBond = false,
            ),
        )
        advanceUntilIdle()

        val connected = effects.filterIsInstance<ScanDeviceEffect.Connected>().singleOrNull()
        assertTrue(connected != null)
        assertTrue(connected.result is DeviceConnectionResult.Working)
    }

    @Test
    fun `bond conflict blocks connection and emits BondConflict`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository()
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(
            DeviceUiItem.Ble(
                id = "AA:BB",
                name = "Pandora",
                address = "AC:BB:CC:DD:EE:FF",
                mode = ScannedDeviceMode.BASE,
                hasDeviceBond = false,
                hasPhoneBond = true,
            ),
        )
        advanceUntilIdle()

        val conflict = effects.filterIsInstance<ScanDeviceEffect.BondConflict>().singleOrNull()
        assertTrue(conflict != null)
        assertEquals(BondConflictKind.REMOVE_FROM_PHONE, conflict.kind)
        assertTrue(effects.none { it is ScanDeviceEffect.Connected })
    }

    @Test
    fun `failed ble connection emits ConnectionFailed`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.FAILED)),
        )
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(
            DeviceUiItem.Ble(
                id = "AA:BB",
                name = "Pandora",
                address = "AC:BB:CC:DD:EE:FF",
                mode = ScannedDeviceMode.BASE,
                hasDeviceBond = false,
                hasPhoneBond = false,
            ),
        )
        advanceUntilIdle()

        assertTrue(effects.any { it is ScanDeviceEffect.ConnectionFailed })
        assertTrue(connection.closed)
    }
}
