package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response

/** Настройки архива прошивок, которые помнит приложение между запусками. */
interface FirmwareArchivePreferences {
    /** Порядок списка систем в архиве без блока. */
    val systemsOrder: StateFlow<Response<SystemsOrder>>

    suspend fun setSystemsOrder(order: SystemsOrder): Response<Unit>
}

/** [Added] — как в базе устройств: последние добавленные сверху; [Alphabet] — по названию. */
enum class SystemsOrder { Added, Alphabet }
