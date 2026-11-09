package ru.alarmtrade.specmp.feature.advancedmount.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess

interface AdvancedMountRepository {
    suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<DeviceAccess>

    /** Опрос с интервалом, пока есть подписчик. */
    fun observeUnreadChatCount(): Flow<Response<Int>>

    /** Открывает канал чата при подписке. */
    fun remoteMountAvailable(): Flow<Response<Boolean>>

    suspend fun isProductInfoEnabled(): Response<Boolean>
}
