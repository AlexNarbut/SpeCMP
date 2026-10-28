package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Раскладка по ширине окна (M3 Window Size Classes). */
internal enum class FirmwareLayout { Compact, Medium, Expanded }

private val MediumFrom = 600.dp
private val ExpandedFrom = 840.dp

internal fun firmwareLayoutOf(width: Dp): FirmwareLayout = when {
    width < MediumFrom -> FirmwareLayout.Compact
    width < ExpandedFrom -> FirmwareLayout.Medium
    else -> FirmwareLayout.Expanded
}
