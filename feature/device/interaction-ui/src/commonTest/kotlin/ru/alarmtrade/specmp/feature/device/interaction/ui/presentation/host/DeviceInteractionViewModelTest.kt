package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

class DeviceInteractionViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val device = AlarmDevice(1, "DX4G", "3.57", DeviceOperationMode.MAIN, emptySet(), can = null)

    private val link = MutableStateFlow(DeviceLinkState.READY)
    private var readDevice = device
    private val sessionRepository = object : DeviceSessionRepository {
        override fun linkState(): Flow<DeviceLinkState> = link
        override suspend fun readBluetoothModule(): Response<BluetoothModule> =
            Response.Error(IllegalStateException("usb only"))
        override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(readDevice)
        override suspend fun readImei(): Response<String> = Response.Success("")
        override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
        override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
        override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
        override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> =
            Response.Success(BootModeResult.Entered)
        override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
        override suspend fun setServicePin(pin: ServicePin): Response<Unit> = Response.Success(Unit)
    }
    private var closeCount = 0
    private val connectionRepository = object : DeviceConnectionRepository {
        override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> =
            emptyFlow()
        override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
        override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
        override suspend fun closeSessions(): Response<Unit> {
            closeCount++
            return Response.Success(Unit)
        }
    }

    private fun TestScope.session() = DeviceSession(
        sessionRepository,
        connectionRepository,
        AppDispatchers(dispatcher, dispatcher, dispatcher),
        backgroundScope.coroutineContext
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `keeps last device while disconnected`() = runTest(dispatcher) {
        val viewModel = DeviceInteractionViewModel(session())
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()

        assertEquals(DeviceSessionState.Disconnected, viewModel.state.value.session)
        assertEquals(device, viewModel.state.value.device)
    }

    @Test
    fun `exit closes session and emits Exit`() = runTest(dispatcher) {
        val viewModel = DeviceInteractionViewModel(session())
        val effects = mutableListOf<DeviceInteractionEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }

        viewModel.onExit()
        viewModel.onExit()
        advanceUntilIdle()

        assertEquals(1, closeCount)
        assertEquals(listOf<DeviceInteractionEffect>(DeviceInteractionEffect.Exit), effects)
    }

    @Test
    fun `bootloader drops the child flow and return to main brings it back`() = runTest(dispatcher) {
        val viewModel = DeviceInteractionViewModel(session())
        advanceUntilIdle()
        assertEquals(device, viewModel.state.value.device)

        val bootDevice = device.copy(mode = DeviceOperationMode.BOOT)
        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertEquals(device, viewModel.state.value.device)
        readDevice = bootDevice
        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), viewModel.state.value.session)
        assertNull(viewModel.state.value.device)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertNull(viewModel.state.value.device)
        readDevice = device
        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)
        assertEquals(device, viewModel.state.value.device)
    }

    @Test
    fun `boot content is shown for the bootloader and stays through a link drop until the block is back`() =
        runTest(dispatcher) {
            val viewModel = DeviceInteractionViewModel(session())
            advanceUntilIdle()
            assertFalse(viewModel.state.value.showBootContent)
            assertFalse(viewModel.state.value.wasBoot)

            link.value = DeviceLinkState.DISCONNECTED
            readDevice = device.copy(mode = DeviceOperationMode.BOOT)
            advanceUntilIdle()
            // Потеря связи в рабочем режиме — обычный оверлей, не экран загрузчика.
            assertFalse(viewModel.state.value.showBootContent)

            link.value = DeviceLinkState.READY
            advanceUntilIdle()
            assertTrue(viewModel.state.value.wasBoot)
            assertTrue(viewModel.state.value.showBootContent)

            // Обрыв в загрузчике (например, при передаче) — решает экран загрузчика, а не оверлей хоста.
            link.value = DeviceLinkState.DISCONNECTED
            advanceUntilIdle()
            assertEquals(DeviceSessionState.Disconnected, viewModel.state.value.session)
            assertTrue(viewModel.state.value.showBootContent)

            readDevice = device
            link.value = DeviceLinkState.READY
            advanceUntilIdle()
            assertFalse(viewModel.state.value.wasBoot)
            assertFalse(viewModel.state.value.showBootContent)
        }

    @Test
    fun `expected reboot to the bootloader shows boot content before the link drops`() = runTest(dispatcher) {
        val session = session()
        val viewModel = DeviceInteractionViewModel(session)
        advanceUntilIdle()

        session.expectReboot(DeviceReboot.ToBoot)
        advanceUntilIdle()
        assertEquals(DeviceReboot.ToBoot, viewModel.state.value.reboot)
        assertTrue(viewModel.state.value.showBootContent)
        // Рабочий flow остаётся под экраном: если команда не удалась, пользователь вернётся туда же.
        assertEquals(device, viewModel.state.value.device)

        session.cancelReboot()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.showBootContent)
    }

    @Test
    fun `reboot to main keeps boot content after the block returns until it is acknowledged`() = runTest(dispatcher) {
        readDevice = device.copy(mode = DeviceOperationMode.BOOT)
        val session = session()
        val viewModel = DeviceInteractionViewModel(session)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showBootContent)

        session.expectReboot(DeviceReboot.ToMain)
        link.value = DeviceLinkState.DISCONNECTED
        readDevice = device
        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)
        assertTrue(viewModel.state.value.showBootContent)

        session.acknowledgeReboot()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.showBootContent)
        assertEquals(device, viewModel.state.value.device)
    }
}
