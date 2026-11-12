package ru.alarmtrade.specmp.core.common.layout

import kotlinx.coroutines.flow.StateFlow
import ru.alarmtrade.specmp.core.common.Response

/** Ширины перетаскиваемых панелей широкой раскладки (dp) по ключу панели, общие для всего приложения. */
interface PaneWidthStore {
    val widthsDp: StateFlow<Response<Map<String, Int>>>

    suspend fun setWidthDp(key: String, widthDp: Int): Response<Unit>
}
