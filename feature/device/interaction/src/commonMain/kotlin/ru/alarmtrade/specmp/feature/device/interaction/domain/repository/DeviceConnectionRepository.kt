package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState

interface DeviceConnectionRepository {
    fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>>

    fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>>

    /** PcBase, 115200; на Android — с системным запросом разрешения USB. */
    fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>>

    /** Закрыть текущую сессию (в SDK она одна на BLE, USB и COM). */
    suspend fun closeSessions(): Response<Unit>
}
