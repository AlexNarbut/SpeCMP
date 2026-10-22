package ru.alarmtrade.specmp.feature.device.settings.domain.repository

/** Настройки отображения редактора, сохраняемые в приложении между входами. */
interface DeviceSettingsPreferences {
    /** Ширина колонки дерева разделов (dp), заданная пользователем; `null` — по умолчанию раскладки. */
    fun treeWidthDp(): Int?

    fun setTreeWidthDp(width: Int)
}
