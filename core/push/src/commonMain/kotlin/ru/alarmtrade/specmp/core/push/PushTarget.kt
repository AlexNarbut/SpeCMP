package ru.alarmtrade.specmp.core.push

/** Куда ведёт тап по уведомлению. */
sealed interface PushTarget {
    data object Chat : PushTarget

    data class Url(val url: String) : PushTarget

    /** Просто открыть приложение. */
    data object App : PushTarget
}
