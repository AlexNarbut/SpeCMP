package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

class BaseDeviceViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val device = AlarmDevice(1, "DX4G", "3.57", DeviceOperationMode.MAIN, emptySet(), can = null)

    private val link = MutableStateFlow(DeviceLinkState.READY)
    private val sessionRepository = object : DeviceSessionRepository {
        override fun linkState(): Flow<DeviceLinkState> = link
        override suspend fun readBluetoothModule(): Response<BluetoothModule> =
            Response.Error(IllegalStateException("usb only"))
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
    private val connectionRepository = object : DeviceConnectionRepository {
        override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> =
            emptyFlow()
        override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
        override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
    }

    private class TestViewModel(deviceSession: DeviceSession) : BaseDeviceViewModel(deviceSession) {
        val readyCalls = mutableListOf<Long>()

        init {
            onEachDeviceReady { readyCalls += it.systemId }
        }

        fun work(block: suspend CoroutineScope.() -> Unit): Job? = launchOnDevice(block)

        fun device(): AlarmDevice? = currentDevice
    }

    private fun TestScope.viewModel() = TestViewModel(
        DeviceSession(
            sessionRepository,
            connectionRepository,
            AppDispatchers(dispatcher, dispatcher, dispatcher),
            backgroundScope.coroutineContext
        )
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `device work is cancelled on disconnect and onReady reruns on reconnect`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(device, viewModel.device())

        val never = CompletableDeferred<Unit>()
        val job = viewModel.work { never.await() }
        assertTrue(job != null && job.isActive)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertTrue(job.isCancelled)
        assertNull(viewModel.work { })
        assertNull(viewModel.device())

        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(listOf(device.systemId, device.systemId), viewModel.readyCalls)
        assertTrue(viewModel.work { } != null)
    }
}
