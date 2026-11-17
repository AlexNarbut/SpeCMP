@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing

internal enum class MainHomeMode {
    Compact,
    Medium,
    Expanded,
    Large,
}

private const val LARGE_MIN_WIDTH_DP = 1_200f
private const val EXPANDED_MIN_WIDTH_DP = 840f
private const val MEDIUM_MIN_WIDTH_DP = 600f

internal fun mainHomeMode(widthDp: Float): MainHomeMode = when {
    widthDp >= LARGE_MIN_WIDTH_DP -> MainHomeMode.Large
    widthDp >= EXPANDED_MIN_WIDTH_DP -> MainHomeMode.Expanded
    widthDp >= MEDIUM_MIN_WIDTH_DP -> MainHomeMode.Medium
    else -> MainHomeMode.Compact
}

internal val MainHomeMode.contentPadding: Dp
    get() = when (this) {
        MainHomeMode.Compact -> CmpSpacing.screenPaddingH

        MainHomeMode.Medium,
        MainHomeMode.Expanded,
        MainHomeMode.Large,
        -> CmpSpacing.screenPaddingHMedium
    }

internal val MainHomeMode.contentMaxWidth: Dp
    get() = when (this) {
        MainHomeMode.Compact,
        MainHomeMode.Medium,
        -> Dp.Unspecified

        MainHomeMode.Expanded,
        MainHomeMode.Large,
        -> 1_040.dp
    }
