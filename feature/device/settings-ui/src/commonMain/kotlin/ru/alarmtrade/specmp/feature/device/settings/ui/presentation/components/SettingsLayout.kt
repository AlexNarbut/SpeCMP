package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Раскладка по ширине окна (M3 Window Size Classes): Compact — переход по разделам с крошками и шторкой;
 * Medium / Expanded — дерево разделов слева (232 / 280 dp) и раздел справа, редактор — диалогом.
 */
internal enum class SettingsLayout(val treeWidth: Dp) {
    Compact(0.dp),
    Medium(232.dp),
    Expanded(280.dp);

    val isWide: Boolean get() = this != Compact
}

private val MediumFrom = 600.dp
private val ExpandedFrom = 840.dp

internal fun settingsLayoutOf(width: Dp): SettingsLayout = when {
    width < MediumFrom -> SettingsLayout.Compact
    width < ExpandedFrom -> SettingsLayout.Medium
    else -> SettingsLayout.Expanded
}
