package ru.alarmtrade.specmp.feature.login.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AuthLayoutMode {
    Compact,
    Medium,
    Expanded,
}

fun authLayoutModeForWidth(width: Dp): AuthLayoutMode = when {
    width < 600.dp -> AuthLayoutMode.Compact
    width < 960.dp -> AuthLayoutMode.Medium
    else -> AuthLayoutMode.Expanded
}
