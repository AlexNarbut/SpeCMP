package ru.alarmtrade.specmp.feature.device.firmware.domain.repository

/** Настройки архива прошивок, которые помнит приложение между запусками. */
interface FirmwareArchivePreferences {
    /** Порядок списка систем в архиве без блока. */
    fun systemsOrder(): SystemsOrder

    fun setSystemsOrder(order: SystemsOrder)
}

/** [Added] — как в базе устройств: последние добавленные сверху; [Alphabet] — по названию. */
enum class SystemsOrder { Added, Alphabet }
