package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal enum class MainHomeMode {
    Compact,
    Medium,
    Expanded,
    Large
}

internal fun mainHomeMode(widthDp: Float): MainHomeMode = when {
    widthDp >= 1_200f -> MainHomeMode.Large
    widthDp >= 840f -> MainHomeMode.Expanded
    widthDp >= 600f -> MainHomeMode.Medium
    else -> MainHomeMode.Compact
}

internal val MainHomeMode.contentPadding: Dp
    get() = when (this) {
        MainHomeMode.Compact -> 16.dp

        MainHomeMode.Medium -> 24.dp

        MainHomeMode.Expanded,
        MainHomeMode.Large -> 32.dp
    }

internal val MainHomeMode.contentMaxWidth: Dp
    get() = when (this) {
        MainHomeMode.Compact,
        MainHomeMode.Medium -> Dp.Unspecified

        MainHomeMode.Expanded,
        MainHomeMode.Large -> 1_040.dp
    }
