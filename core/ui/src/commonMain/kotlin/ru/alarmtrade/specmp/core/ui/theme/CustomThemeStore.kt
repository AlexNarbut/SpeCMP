package ru.alarmtrade.specmp.core.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.di.platformSettings
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeFileCodec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeSpec

/** Пользовательская тема (экран «Оформление»); без хранилища — тема по умолчанию. */
interface CustomThemeStore {
    val theme: StateFlow<ThemeSpec>

    fun save(spec: ThemeSpec)
}

/** JSON файла темы в платформенном `Settings`, как выбор светлой/тёмной: нужна до первой композиции. */
object DefaultCustomThemeStore : CustomThemeStore {
    private const val KEY = "app_theme_custom"

    private val state = MutableStateFlow(
        platformSettings.getStringOrNull(KEY)?.let(ThemeFileCodec::decodeStored) ?: ThemePreset.Default,
    )

    override val theme: StateFlow<ThemeSpec> = state.asStateFlow()

    override fun save(spec: ThemeSpec) {
        when (spec) {
            ThemePreset.Default -> platformSettings.remove(KEY)
            else -> platformSettings.putString(KEY, ThemeFileCodec.encode(spec))
        }
        state.update { spec }
    }
}
