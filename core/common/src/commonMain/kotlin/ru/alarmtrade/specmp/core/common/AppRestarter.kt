package ru.alarmtrade.specmp.core.common

/** Где перезапуск невозможен (iOS — `exit()` отклоняет App Store; desktop) — no-op с `false`. */
fun interface AppRestarter {
    /** `true` — перезапуск начался (процесс завершается); `false` — не поддержан. */
    fun restart(): Boolean
}
