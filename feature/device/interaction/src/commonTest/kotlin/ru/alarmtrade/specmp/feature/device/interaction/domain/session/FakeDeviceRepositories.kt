package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository

val testDevice = AlarmDevice(
    systemId = 1076884189,
    systemName = "DX4G",
    firmwareVersion = "3.57",
    mode = DeviceOperationMode.MAIN,
    features = emptySet(),
    can = null,
)

class FakeDeviceSessionRepository(initial: DeviceLinkState = DeviceLinkState.DISCONNECTED) : DeviceSessionRepository {
    val link = MutableStateFlow(initial)
    /** Ответы readDevice по порядку; последний повторяется. */
    var readResults: List<Response<AlarmDevice>> = listOf(Response.Success(testDevice))
    var readCount = 0
        private set

    override fun linkState(): Flow<DeviceLinkState> = link

    override suspend fun readDevice(): Response<AlarmDevice> =
        readResults[minOf(readCount++, readResults.lastIndex)]

    var imei: Response<String> = Response.Success("351756051523999")
    var bootModeCount = 0
        private set

    override suspend fun readImei(): Response<String> = imei

    override suspend fun setBootMode(): Response<Unit> {
        bootModeCount++
        return Response.Success(Unit)
    }
}

class FakeDeviceConnectionRepository : DeviceConnectionRepository {
    var closeCount = 0
        private set

    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions() {
        closeCount++
    }
}
