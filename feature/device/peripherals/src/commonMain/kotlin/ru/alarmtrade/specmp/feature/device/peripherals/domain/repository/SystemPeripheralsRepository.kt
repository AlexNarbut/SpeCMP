package ru.alarmtrade.specmp.feature.device.peripherals.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralPollStep
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType

/** Периферия по типам — только Bluetooth. */
interface SystemPeripheralsRepository {
    /**
     * Опрос по кругу: [types] по очереди, шаг на каждый тип. Неответивший тип — шаг с `devices = null`;
     * другие ошибки завершают поток последним `Response.Error`.
     */
    fun poll(types: List<PeripheralType>): Flow<Response<PeripheralPollStep>>

    /** Устройство моргает светодиодом. */
    suspend fun blink(address: String): Response<Unit>

    /**
     * Перевод устройства в режим прошивки; блок входит в режим программирования сам.
     * Ошибка — [ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralException].
     */
    suspend fun enterFirmwareMode(address: String): Response<Unit>
}
