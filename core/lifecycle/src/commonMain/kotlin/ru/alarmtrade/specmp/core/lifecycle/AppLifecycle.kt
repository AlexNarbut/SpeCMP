package ru.alarmtrade.specmp.core.lifecycle

import kotlinx.coroutines.flow.Flow

enum class AppLifecycleEvent {
    FOREGROUND,

    BACKGROUND,
}

/** Только смены состояния (стартовое не эмитится); перекрытие экрана другим экраном приложения сюда не попадает. */
interface AppLifecycle {
    val events: Flow<AppLifecycleEvent>
}

internal expect fun platformAppLifecycle(): AppLifecycle
