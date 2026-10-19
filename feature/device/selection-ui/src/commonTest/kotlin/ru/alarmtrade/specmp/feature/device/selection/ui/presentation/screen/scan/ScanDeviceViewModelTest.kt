package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycle
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycleEvent
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectedDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDevice
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.toUiItem
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

    private var lastSession: DeviceSession? = null

    private fun deviceSession(connection: FakeDeviceConnectionRepository) =
        DeviceSession(FakeDeviceSessionRepository(), connection, dispatchers()).also { lastSession = it }

    private fun viewModel(
        scan: FakeDeviceScanRepository = FakeDeviceScanRepository(),
        connection: FakeDeviceConnectionRepository = FakeDeviceConnectionRepository(),
        permissions: FakePermissionsProvider = FakePermissionsProvider(),
    ) = ScanDeviceViewModel(scan, connection, deviceSession(connection), permissions, dispatchers())

    @Test
    fun `scanning shows found devices as content`() = runTest(testDispatcher) {
        // На jvm deviceAccessRequirements() пуст → начальный шаг сразу Scanning,
        // реактивный скан стартует при первой подписке на state (без start-интента).
        val bleDevice = ScannedDevice.Ble(
            id = "AA:BB",
            name = "Pandora",
            kind = DeviceKind.ALARM,
            mode = ScannedDeviceMode.BASE,
            rssi = -50,
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
    fun `connecting to ble device emits SessionCreated`() = runTest(testDispatcher) {
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
                kind = DeviceKind.ALARM,
                rssi = -50,
                hasDeviceBond = false,
                hasPhoneBond = false,
            ),
        )
        advanceUntilIdle()

        val created = effects.filterIsInstance<ScanDeviceEffect.SessionCreated>().singleOrNull()
        assertTrue(created != null)
        assertEquals(ConnectedDevice("AA:BB", "Pandora", ConnectionTransport.BLE), created.device)
        assertEquals(ConnectionTransport.BLE, lastSession?.transport?.value)
    }

    @Test
    fun `stale phone bond is left to the SDK`() = runTest(testDispatcher) {
        // Телефон сопряжён, контекста у блока нет: SDK сам снимет старое сопряжение и сопряжётся заново.
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository()
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(bleItem(hasDeviceBond = false, hasPhoneBond = true))
        advanceUntilIdle()

        assertEquals(1, connection.bleConnectCount)
        assertTrue(effects.any { it is ScanDeviceEffect.SessionCreated })
    }

    @Test
    fun `unit bound to another phone blocks connection`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository()
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(bleItem(hasDeviceBond = true, hasPhoneBond = false))
        advanceUntilIdle()

        assertEquals(0, connection.bleConnectCount)
        assertTrue(effects.contains(ScanDeviceEffect.BoundToAnotherPhone))
        assertTrue(effects.none { it is ScanDeviceEffect.SessionCreated })
    }

    @Test
    fun `pairing required in normal mode asks to enable pairing`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.PAIRING_REQUIRED)),
        )
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(bleItem(hasDeviceBond = false, hasPhoneBond = true))
        advanceUntilIdle()

        assertEquals(1, connection.bleConnectCount)
        assertTrue(effects.contains(ScanDeviceEffect.PairingRequired))
        assertTrue(effects.none { it is ScanDeviceEffect.SessionCreated })
    }

    @Test
    fun `pairing required in pairing mode is retried once`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val pairingDevice = ScannedDevice.Ble(
            id = "AA:BB",
            name = "Pandora",
            kind = DeviceKind.ALARM,
            mode = ScannedDeviceMode.PAIRING,
            rssi = -50,
            hasDeviceBond = false,
            hasPhoneBond = true,
        )
        val connection = FakeDeviceConnectionRepository(
            bleAttempts = listOf(
                listOf(Response.Success(DeviceConnectionState.PAIRING_REQUIRED)),
                listOf(Response.Success(DeviceConnectionState.CONNECTED)),
            ),
        )
        val viewModel = viewModel(scan = FakeDeviceScanRepository(ble = listOf(pairingDevice)), connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(pairingDevice.toUiItem())
        advanceUntilIdle()

        assertEquals(2, connection.bleConnectCount)
        assertTrue(effects.any { it is ScanDeviceEffect.SessionCreated })
        assertTrue(effects.none { it is ScanDeviceEffect.PairingRequired })
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
                kind = DeviceKind.ALARM,
                rssi = -50,
                hasDeviceBond = false,
                hasPhoneBond = false,
            ),
        )
        advanceUntilIdle()

        assertTrue(effects.any { it is ScanDeviceEffect.ConnectionFailed })
        assertTrue(connection.closed)
    }

    @Test
    fun `stale session is closed before the first scan`() = runTest(testDispatcher) {
        val connection = FakeDeviceConnectionRepository()
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertEquals(1, connection.closeCount)
        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
    }

    @Test
    fun `failed connection closes session and restarts scan`() = runTest(testDispatcher) {
        val scan = FakeDeviceScanRepository()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.FAILED)),
            connectDelayMillis = 1_000,
        )
        val viewModel = viewModel(scan = scan, connection = connection)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        val scansBefore = scan.bleScanStarts
        val closesBefore = connection.closeCount

        viewModel.connect(bleItem())
        advanceUntilIdle()

        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
        assertEquals(closesBefore + 1, connection.closeCount)
        assertEquals(scansBefore + 1, scan.bleScanStarts)
    }

    @Test
    fun `cancel connecting closes session and restarts scan`() = runTest(testDispatcher) {
        val scan = FakeDeviceScanRepository()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.CONNECTING)),
            hangAfterStates = true,
        )
        val viewModel = viewModel(scan = scan, connection = connection)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        val scansBefore = scan.bleScanStarts
        val closesBefore = connection.closeCount

        viewModel.connect(bleItem())
        assertTrue(viewModel.state.value.step is ScanStep.Connecting)

        viewModel.onCancelConnecting()
        advanceUntilIdle()

        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
        assertEquals(closesBefore + 1, connection.closeCount)
        assertEquals(scansBefore + 1, scan.bleScanStarts)
    }

    @Test
    fun `second connect tap does not interrupt the running attempt`() = runTest(testDispatcher) {
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.CONNECTING)),
            hangAfterStates = true,
        )
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.connect(bleItem())
        viewModel.connect(bleItem())
        runCurrent()

        assertEquals(1, connection.bleConnectCount)
        assertTrue(viewModel.state.value.step is ScanStep.Connecting)

        viewModel.onCancelConnecting()
        advanceUntilIdle()
        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
    }

    @Test
    fun `back while connecting cancels and stays on screen`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.CONNECTING)),
            hangAfterStates = true,
        )
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch { viewModel.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()
        val closesBefore = connection.closeCount

        viewModel.connect(bleItem())
        viewModel.onBack()
        advanceUntilIdle()

        assertTrue(effects.none { it is ScanDeviceEffect.Close })
        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
        assertEquals(closesBefore + 1, connection.closeCount)
    }

    @Test
    fun `back while scanning emits Close`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.onBack()

        assertEquals(listOf<ScanDeviceEffect>(ScanDeviceEffect.Close), effects)
    }

    @Test
    fun `connection without terminal status fails by timeout and returns to scan`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(Response.Success(DeviceConnectionState.CONNECTING)),
            hangAfterStates = true,
        )
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch { viewModel.state.collect {} }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(bleItem())
        advanceTimeBy(59_000)
        assertTrue(viewModel.state.value.step is ScanStep.Connecting)

        advanceUntilIdle()

        assertTrue(effects.any { it is ScanDeviceEffect.ConnectionFailed })
        assertEquals(ScanStep.Scanning, viewModel.state.value.step)
    }

    @Test
    fun `usb failed status emits ConnectionFailed`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val connection = FakeDeviceConnectionRepository(
            states = listOf(
                Response.Success(DeviceConnectionState.CONNECTING),
                Response.Success(DeviceConnectionState.FAILED),
            ),
        )
        val viewModel = viewModel(connection = connection)
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        viewModel.connect(
            DeviceUiItem.Usb(
                id = "usb-1",
                name = "Pandora USB",
                address = "Pandora · 0483:5710",
                manufacturerName = "Pandora",
                vidPid = "0483:5710",
                systemId = null,
                firmwareVersion = null,
                isBootMode = null,
            ),
        )
        advanceUntilIdle()

        assertTrue(effects.any { it is ScanDeviceEffect.ConnectionFailed })
        assertTrue(effects.none { it is ScanDeviceEffect.SessionCreated })
    }

    @Test
    fun `error of one transport keeps devices found by another`() = runTest(testDispatcher) {
        val usbDevice = ScannedDevice.Usb(
            id = "usb-1",
            name = "Pandora USB",
            manufacturerName = "Pandora",
            vendorId = 0x0483,
            productId = 0x5710,
            systemId = null,
            firmwareVersion = null,
            isBootMode = null,
        )
        val viewModel = viewModel(
            scan = FakeDeviceScanRepository(
                bleResponse = Response.Error(IllegalStateException("BLE scan failed")),
                usb = listOf(usbDevice),
            ),
        )
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        val devices = viewModel.state.value.devices
        assertTrue(devices is LCEState.Content)
        assertEquals(listOf("usb-1"), devices.content.map { it.id })
    }

    @Test
    fun `unknown phone bond does not block connection`() = runTest(testDispatcher) {
        val effects = mutableListOf<ScanDeviceEffect>()
        val viewModel = viewModel()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }
        advanceUntilIdle()

        // iOS: устройство помнит телефон (hasContext), а о сопряжении на телефоне платформа не сообщает.
        viewModel.connect(bleItem(hasDeviceBond = true, hasPhoneBond = null))
        advanceUntilIdle()

        assertTrue(effects.none { it is ScanDeviceEffect.BoundToAnotherPhone })
        assertTrue(effects.any { it is ScanDeviceEffect.SessionCreated })
    }

    private fun bleItem(
        hasDeviceBond: Boolean = false,
        hasPhoneBond: Boolean? = false,
    ) = DeviceUiItem.Ble(
        id = "AA:BB",
        name = "Pandora",
        address = "AC:BB:CC:DD:EE:FF",
        mode = ScannedDeviceMode.BASE,
        kind = DeviceKind.ALARM,
        rssi = -50,
        hasDeviceBond = hasDeviceBond,
        hasPhoneBond = hasPhoneBond,
    )

    @Test
    fun `returning to the app restarts the scan flow`() = withAppLifecycle { lifecycle ->
        runTest(testDispatcher) {
            val scan = FakeDeviceScanRepository()
            val viewModel = viewModel(scan = scan)
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect {} }
            advanceUntilIdle()
            assertEquals(1, scan.bleScanStarts)

            lifecycle.emit(AppLifecycleEvent.FOREGROUND)
            advanceUntilIdle()

            assertEquals(ScanStep.Scanning, viewModel.state.value.step)
            assertEquals(2, scan.bleScanStarts)
        }
    }

    @Test
    fun `returning to the app does not interrupt connecting`() = withAppLifecycle { lifecycle ->
        runTest(testDispatcher) {
            val scan = FakeDeviceScanRepository()
            val connection = FakeDeviceConnectionRepository(states = emptyList(), hangAfterStates = true)
            val viewModel = viewModel(scan = scan, connection = connection)
            backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect {} }
            advanceUntilIdle()
            viewModel.connect(bleItem())
            runCurrent() // не advanceUntilIdle: он промотает таймаут подключения
            val scansBefore = scan.bleScanStarts

            lifecycle.emit(AppLifecycleEvent.FOREGROUND)
            runCurrent()

            assertTrue(viewModel.state.value.step is ScanStep.Connecting)
            assertEquals(scansBefore, scan.bleScanStarts)
        }
    }

    /** BaseViewModel берёт AppLifecycle из Koin: поднимаем Koin с фейком на время теста. */
    private fun withAppLifecycle(block: (MutableSharedFlow<AppLifecycleEvent>) -> Unit) {
        val events = MutableSharedFlow<AppLifecycleEvent>()
        startKoin { modules(module { single<AppLifecycle> { object : AppLifecycle { override val events = events } } }) }
        try {
            block(events)
        } finally {
            stopKoin()
        }
    }
}
