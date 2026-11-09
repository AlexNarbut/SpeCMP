package ru.alarmtrade.specmp.feature.device.interaction.testing

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository

/**
 * Подключение отдаёт [states]; [hangAfterStates] — поток не завершается (SDK не дал финального статуса),
 * [connectDelayMillis] — задержка до первого статуса, [bleAttempts] — статусы по попыткам BLE (последняя повторяется).
 */
class FakeDeviceConnectionRepository(
    private val states: List<Response<DeviceConnectionState>> =
        listOf(Response.Success(DeviceConnectionState.CONNECTED)),
    private val hangAfterStates: Boolean = false,
    private val connectDelayMillis: Long = 0,
    private val bleAttempts: List<List<Response<DeviceConnectionState>>> = listOf(states),
) : DeviceConnectionRepository {
    var closeCount = 0
        private set
    val closed: Boolean get() = closeCount > 0
    var bleConnectCount = 0
        private set
    var serialConnectCount = 0
        private set

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> =
        connectFlow(bleAttempts[minOf(bleConnectCount++, bleAttempts.lastIndex)])

    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = connectFlow(states)

    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> {
        serialConnectCount++
        return connectFlow(states)
    }

    override suspend fun closeSessions(): Response<Unit> {
        closeCount++
        return Response.Success(Unit)
    }

    private fun connectFlow(attemptStates: List<Response<DeviceConnectionState>>) = flow {
        if (connectDelayMillis > 0) delay(connectDelayMillis)
        emitAll(attemptStates.asFlow())
        if (hangAfterStates) awaitCancellation()
    }
}
