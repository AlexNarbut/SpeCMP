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
import ru.alarmtrade.specmp.core.ui.theme.tokens.DarkCmpColorScheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.LightCmpColorScheme

val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }

/** Тёмная/светлая — сохранённый выбор из [themeStore]; пока не выбирали или без хранилища — как в системе. */
@Composable
fun AppTheme(
    onThemeChange: @Composable (isDark: Boolean) -> Unit,
    themeStore: AppThemeStore? = null,
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
        val typography = rememberCmpTypography()
        MaterialTheme(
            colorScheme = if (isDark) DarkCmpColorScheme else LightCmpColorScheme,
            typography = remember(typography) { typography.material() },
            content = { Surface(content = content) },
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
