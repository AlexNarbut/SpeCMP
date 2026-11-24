package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/** Имя системы — как блок отдаёт его о себе, без пробелов-заполнителей; `null` — версии в заголовке нет. */
data class FrmFileInfo(val systemName: String, val version: String?) {
    /** Прошивка для блока с именем системы [deviceSystemName] (сравнение без заполнителей и регистра). */
    fun isFor(deviceSystemName: String): Boolean = systemName.equals(deviceSystemName.trim(), ignoreCase = true)
}

/** Файл прошивки по COM по имени или ссылке: расширение `.frm` последнего сегмента пути (без query и fragment). */
fun isFrmFileName(nameOrUrl: String): Boolean = fileNameOf(nameOrUrl).endsWith(FRM_EXTENSION, ignoreCase = true)

/** Последний сегмент пути без query и fragment. */
fun fileNameOf(nameOrUrl: String): String = nameOrUrl.substringBefore('#').substringBefore('?').substringAfterLast('/')

private const val FRM_EXTENSION = ".frm"
