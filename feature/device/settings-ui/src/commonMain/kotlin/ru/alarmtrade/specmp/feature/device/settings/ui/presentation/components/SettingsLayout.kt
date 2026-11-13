package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints

internal enum class SettingsLayout(val treeWidth: Dp) {
    Compact(0.dp),
    Medium(232.dp),
    Expanded(280.dp),
    ;

    val isWide: Boolean get() = this != Compact
}

internal fun settingsLayoutOf(width: Dp): SettingsLayout = when {
    width < CmpBreakpoints.Medium -> SettingsLayout.Compact
    width < CmpBreakpoints.Expanded -> SettingsLayout.Medium
    else -> SettingsLayout.Expanded
}
