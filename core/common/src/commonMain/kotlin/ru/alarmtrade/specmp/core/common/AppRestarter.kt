package ru.alarmtrade.specmp.core.common

/**
 * Перезапуск приложения целиком — например, чтобы применить новый язык (он применяется только на старте процесса).
 * Реализацию даёт платформа (composition root). Где перезапуск невозможен (iOS — `exit()` отклоняет App Store;
 * desktop) — no-op с `false`: вызывающий просит пользователя перезапустить приложение сам.
 */
fun interface AppRestarter {
    /** `true` — перезапуск начался (процесс завершается); `false` — не поддержан. */
    fun restart(): Boolean
}
