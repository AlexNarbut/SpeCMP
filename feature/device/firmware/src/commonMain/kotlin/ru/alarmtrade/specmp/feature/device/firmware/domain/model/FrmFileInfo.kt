package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/**
 * Для какого блока прошивка `.frm` — из её заголовка: имя системы (как блок отдаёт его в информации о себе,
 * без пробелов-заполнителей) и версия (`VER1.00`); `null` — версии в заголовке нет.
 */
data class FrmFileInfo(val systemName: String, val version: String?) {
    /** Прошивка для блока с именем системы [deviceSystemName] (сравнение без заполнителей и регистра). */
    fun isFor(deviceSystemName: String): Boolean = systemName.equals(deviceSystemName.trim(), ignoreCase = true)
}
