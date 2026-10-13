package ru.alarmtrade.specmp.core.lifecycle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal actual fun platformAppLifecycle(): AppLifecycle = DesktopAppLifecycle

/** Desktop: разрешений и выключаемого адаптера нет — событий пока не даём (позже — фокус окна из `desktopApp`). */
private object DesktopAppLifecycle : AppLifecycle {
    override val events: Flow<AppLifecycleEvent> = emptyFlow()
}
