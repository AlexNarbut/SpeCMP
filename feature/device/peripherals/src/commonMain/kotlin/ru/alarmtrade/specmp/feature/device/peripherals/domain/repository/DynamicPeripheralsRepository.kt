package ru.alarmtrade.specmp.feature.device.peripherals.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral

/**
 * Динамическая таблица блока. Блок входит в режим программирования сам (сохранённый PIN); ошибки —
 * [ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException].
 */
interface DynamicPeripheralsRepository {
    /** Таблица раз в 2 с; поток завершается на первой ошибке. */
    fun slots(): Flow<Response<List<DynamicPeripheral>>>

    /** Успех — блок принял команду и ждёт устройство; приживилось ли — видно по [slots]. */
    suspend fun pair(slot: Int, address: String?): Response<Unit>

    suspend fun delete(slot: Int): Response<Unit>

    suspend fun update(slot: Int): Response<Unit>
}
