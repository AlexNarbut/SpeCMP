package ru.alarmtrade.specmp.core.ui.theme

import com.russhwolf.settings.Settings

/**
 * Тема, выбранная пользователем (переключатель в боковом меню), между запусками. Интерфейсом — чтобы галереи и
 * тесты рисовали тему без сохранённого выбора ([AppTheme] без хранилища следует за системой).
 */
interface AppThemeStore {
    /** Сохранённый выбор: `true` — тёмная, `false` — светлая, `null` — не выбирали (как в системе). */
    fun savedIsDark(): Boolean?

    fun save(isDark: Boolean)
}

/** Хранилище приложения — свой `Settings` (как у языка приложения), без DI: тема нужна до первой композиции. */
object DefaultAppThemeStore : AppThemeStore {
    private const val KEY = "app_theme_dark"

    private val settings: Settings by lazy { Settings() }

    override fun savedIsDark(): Boolean? = settings.getBooleanOrNull(KEY)

    override fun save(isDark: Boolean) = settings.putBoolean(KEY, isDark)
}
