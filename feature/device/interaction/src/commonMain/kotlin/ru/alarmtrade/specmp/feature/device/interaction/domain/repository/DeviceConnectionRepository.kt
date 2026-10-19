package ru.alarmtrade.specmp.feature.device.interaction.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState

/**
 * Фасад над session-частью connector-интеракторов Specialist SDK.
 * Открывает/закрывает сессию подключения к устройству, отдаёт доменный статус.
 */
interface DeviceConnectionRepository {
    /** Открыть BLE-сессию с устройством; поток статусов до терминального. */
    fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>>

    /** Открыть USB-сессию с устройством; поток статусов до терминального. */
    fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>>

    /** Закрыть текущую сессию (в SDK она одна на BLE и USB). */
    suspend fun closeSessions(): Response<Unit>
}
