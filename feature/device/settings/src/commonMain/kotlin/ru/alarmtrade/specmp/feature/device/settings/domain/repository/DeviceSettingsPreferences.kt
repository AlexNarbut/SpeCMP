package ru.alarmtrade.specmp.feature.device.settings.domain.repository

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response

/** Настройки отображения редактора, сохраняемые в приложении между входами. */
interface DeviceSettingsPreferences {
    /** Ширина колонки дерева разделов (dp), заданная пользователем; `null` — по умолчанию раскладки. */
    val treeWidthDp: StateFlow<Response<Int?>>

    suspend fun setTreeWidthDp(width: Int): Response<Unit>
}
