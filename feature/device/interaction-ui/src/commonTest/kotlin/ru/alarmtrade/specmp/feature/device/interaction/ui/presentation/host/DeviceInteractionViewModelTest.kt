package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeviceInteractionViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val device = AlarmDevice(1, "DX4G", "3.57", DeviceOperationMode.MAIN, emptySet())

    private val link = MutableStateFlow(DeviceLinkState.READY)
    private val sessionRepository = object : DeviceSessionRepository {
        override fun linkState(): Flow<DeviceLinkState> = link
        override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
    }
    private var closeCount = 0
    private val connectionRepository = object : DeviceConnectionRepository {
        override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
        override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
        override suspend fun closeSessions() {
            closeCount++
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `keeps last device while disconnected`() = runTest(dispatcher) {
        val viewModel = DeviceInteractionViewModel(DeviceSession(sessionRepository, connectionRepository, backgroundScope), backgroundScope)
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()

        assertEquals(DeviceSessionState.Disconnected, viewModel.state.value.session)
        assertEquals(device, viewModel.state.value.device)
    }

    @Test
    fun `exit closes session and emits Exit`() = runTest(dispatcher) {
        val viewModel = DeviceInteractionViewModel(DeviceSession(sessionRepository, connectionRepository, backgroundScope), backgroundScope)
        val effects = mutableListOf<DeviceInteractionEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.effect.collect { effects += it } }

        viewModel.onExit()
        advanceUntilIdle()

        assertEquals(1, closeCount)
        assertEquals(listOf<DeviceInteractionEffect>(DeviceInteractionEffect.Exit), effects)
    }
}
