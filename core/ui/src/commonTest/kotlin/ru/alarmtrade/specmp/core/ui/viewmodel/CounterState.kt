package ru.alarmtrade.specmp.core.ui.viewmodel

internal data class CounterState(
    val count: Int = 0,
    val left: Int = 0,
    val right: Int = 0,
    val loaded: Int? = null,
    val loadError: String? = null,
    val query: String = "",
    val found: String? = null,
)
