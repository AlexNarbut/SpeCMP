package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/** Имя системы — как блок отдаёт его о себе, без пробелов-заполнителей; `null` — версии в заголовке нет. */
data class FrmFileInfo(val systemName: String, val version: String?) {
    /** Прошивка для блока с именем системы [deviceSystemName] (сравнение без заполнителей и регистра). */
    fun isFor(deviceSystemName: String): Boolean = systemName.equals(deviceSystemName.trim(), ignoreCase = true)
}
