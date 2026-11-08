package ru.alarmtrade.specmp.core.lifecycle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/** Возвраты приложения на передний план; гейт — там, где инициируется вызов: во ViewModel или в репозитории. */
interface AppForegroundProvider {
    /** Переходы фон → передний план; состояние на момент подписки возвратом не считается. */
    val foregroundReturns: Flow<Unit>
}

internal class AppLifecycleForegroundProvider(appLifecycle: AppLifecycle) : AppForegroundProvider {
    override val foregroundReturns: Flow<Unit> = appLifecycle.events
        .filter { it == AppLifecycleEvent.FOREGROUND }
        .map { }
}
