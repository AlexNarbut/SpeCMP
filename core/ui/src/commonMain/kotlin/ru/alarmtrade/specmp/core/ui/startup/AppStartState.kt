package ru.alarmtrade.specmp.core.ui.startup

import androidx.compose.runtime.Immutable

/** [isReady] — начальная настройка закончена: корень показывает UI, первые запросы уйдут с правильным языком. */
@Immutable
data class AppStartState(val isReady: Boolean = false)
