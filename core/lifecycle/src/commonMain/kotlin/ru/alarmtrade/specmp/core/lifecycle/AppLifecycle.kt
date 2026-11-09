package ru.alarmtrade.specmp.core.lifecycle

import kotlinx.coroutines.flow.Flow

/** Смена состояния всего приложения (не отдельного экрана). */
enum class AppLifecycleEvent {
    /** Приложение вернулось на передний план: из системных настроек, другого приложения, после блокировки. */
    FOREGROUND,

    /** Приложение ушло в фон. */
    BACKGROUND,
}

/**
 * Жизненный цикл приложения для слоя ViewModel: события приходят только при смене состояния
 * (первое «приложение запущено» не эмитится). Перекрытие экрана другим экраном приложения сюда не попадает —
 * это жизненный цикл навигации.
 */
interface AppLifecycle {
    val events: Flow<AppLifecycleEvent>
}

/** Платформенная реализация: Android — `ProcessLifecycleOwner`, iOS — `UIApplication`, desktop — пока без событий. */
internal expect fun platformAppLifecycle(): AppLifecycle
