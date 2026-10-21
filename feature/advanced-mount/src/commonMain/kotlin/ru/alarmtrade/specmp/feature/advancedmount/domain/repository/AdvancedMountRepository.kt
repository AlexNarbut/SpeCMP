package ru.alarmtrade.specmp.feature.advancedmount.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess

/** Серверная часть расширенного монтажа (Specialist SDK). Тонкий: опрос и повторы — во ViewModel. */
interface AdvancedMountRepository {
    /** Проверить блок на сервере (доступ монтажника к блоку). */
    suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<DeviceAccess>

    /** Количество непрочитанных сообщений в чате техподдержки — опрос с интервалом, пока есть подписчик. */
    fun observeUnreadChatCount(): Flow<Response<Int>>

    /** Доступен ли удалённый монтаж (специалист может подключиться к блоку); открывает чат при подписке. */
    fun remoteMountAvailable(): Flow<Boolean>

    /** Включён ли у пользователя «Поиск товара» (кнопка «Информация о товаре»). */
    suspend fun isProductInfoEnabled(): Response<Boolean>
}
