package ru.alarmtrade.specmp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Theme colors as plain [Color] values — no Material3 types exposed.
 * Feature modules use [cmpThemeColors] instead of importing [MaterialTheme] directly.
 */
data class CmpThemeColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val error: Color,
    val onError: Color,
    val outline: Color,
    val outlineVariant: Color,
)

@Composable
fun cmpThemeColors(): CmpThemeColors {
    val s = MaterialTheme.colorScheme
    return CmpThemeColors(
        background = s.background,
        onBackground = s.onBackground,
        surface = s.surface,
        onSurface = s.onSurface,
        surfaceVariant = s.surfaceVariant,
        onSurfaceVariant = s.onSurfaceVariant,
        primary = s.primary,
        onPrimary = s.onPrimary,
        primaryContainer = s.primaryContainer,
        onPrimaryContainer = s.onPrimaryContainer,
        secondary = s.secondary,
        onSecondary = s.onSecondary,
        secondaryContainer = s.secondaryContainer,
        onSecondaryContainer = s.onSecondaryContainer,
        tertiary = s.tertiary,
        onTertiary = s.onTertiary,
        error = s.error,
        onError = s.onError,
        outline = s.outline,
        outlineVariant = s.outlineVariant,
    )
}
