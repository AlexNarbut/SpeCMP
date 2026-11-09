package ru.alarmtrade.specmp.core.ui.theme

import ru.alarmtrade.specmp.core.common.di.platformSettings

/** Интерфейс — чтобы галереи и тесты рисовали тему без сохранённого выбора. */
interface AppThemeStore {
    /** Сохранённый выбор: `true` — тёмная, `false` — светлая, `null` — не выбирали (как в системе). */
    fun savedIsDark(): Boolean?

    fun save(isDark: Boolean)
}

/** Платформенный `Settings` процесса (как у языка приложения), без DI: тема нужна до первой композиции. */
object DefaultAppThemeStore : AppThemeStore {
    private const val KEY = "app_theme_dark"

    override fun savedIsDark(): Boolean? = platformSettings.getBooleanOrNull(KEY)

    override fun save(isDark: Boolean) = platformSettings.putBoolean(KEY, isDark)
}
