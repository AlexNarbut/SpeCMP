package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.ui.unit.Dp
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints

internal enum class FirmwareLayout { Compact, Medium, Expanded }

internal fun firmwareLayoutOf(width: Dp): FirmwareLayout = when {
    width < CmpBreakpoints.Medium -> FirmwareLayout.Compact
    width < CmpBreakpoints.Expanded -> FirmwareLayout.Medium
    else -> FirmwareLayout.Expanded
}
