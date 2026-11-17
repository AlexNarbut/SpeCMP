package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpColors
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpControlStyle
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpPageBackground
import ru.alarmtrade.specmp.core.ui.theme.custom.LocalCmpTextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemePreset
import ru.alarmtrade.specmp.core.ui.theme.custom.effectiveTextScale
import ru.alarmtrade.specmp.core.ui.theme.custom.resolve
import ru.alarmtrade.specmp.core.ui.theme.tokens.LocalCmpRadii

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

/**
 * Тёмная/светлая — сохранённый выбор из [themeStore]; пока не выбирали или без хранилища — как в системе.
 * Цвета, скругления, текст и кнопки — из [customThemeStore]; без него — тема по умолчанию (CSS-токены).
 */
@Composable
fun AppTheme(
    onThemeChange: @Composable (isDark: Boolean) -> Unit,
    themeStore: AppThemeStore? = null,
    customThemeStore: CustomThemeStore? = null,
    content: @Composable () -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()
    val isDarkState = remember(systemIsDark, themeStore) {
        val state = mutableStateOf(themeStore?.savedIsDark() ?: systemIsDark)
        if (themeStore == null) state else SavedThemeState(state, themeStore)
    }
    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
    ) {
        val isDark by isDarkState
        onThemeChange(!isDark)
        val spec = customThemeStore?.theme?.collectAsStateWithLifecycle()?.value ?: ThemePreset.Default
        ThemeLocals(spec.mode(isDark), isDark) { Surface(content = content) }
    }
}

/**
 * Тема одного режима поверх текущей (предпросмотр на экране «Оформление»): [isDark] видят и читатели
 * [LocalThemeIsDark] внутри, переключатель темы приложения здесь не действует.
 */
@Composable
fun CmpThemeProvider(spec: ThemeModeSpec, isDark: Boolean, content: @Composable () -> Unit) {
    val isDarkState = remember(isDark) { mutableStateOf(isDark) }
    CompositionLocalProvider(LocalThemeIsDark provides isDarkState) {
        ThemeLocals(spec, isDark, content)
    }
}

@Composable
private fun ThemeLocals(spec: ThemeModeSpec, isDark: Boolean, content: @Composable () -> Unit) {
    val theme = remember(spec, isDark) { spec.resolve(isDark) }
    val textScale = effectiveTextScale(theme.textScale, LocalDensity.current.fontScale)
    val typography = rememberCmpTypography(textScale)
    CompositionLocalProvider(
        LocalCmpColors provides theme.colors,
        LocalCmpRadii provides theme.radii,
        LocalCmpControlStyle provides theme.controls,
        LocalCmpTextScale provides textScale,
        LocalCmpPageBackground provides theme.background,
    ) {
        MaterialTheme(
            colorScheme = theme.scheme,
            typography = remember(typography) { typography.material() },
            content = content,
        )
    }
}

/** Сохраняет только переключение пользователем (запись в состояние), начальное значение — нет. */
private class SavedThemeState(private val state: MutableState<Boolean>, private val store: AppThemeStore) :
    MutableState<Boolean> by state {
    override var value: Boolean
        get() = state.value
        set(isDark) {
            state.value = isDark
            store.save(isDark)
        }
}
