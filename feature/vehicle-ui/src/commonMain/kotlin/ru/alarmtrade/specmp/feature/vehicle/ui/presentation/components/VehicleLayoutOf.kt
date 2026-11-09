package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleLayout

internal fun vehicleLayoutOf(width: Dp): VehicleLayout = when {
    width < 600.dp -> VehicleLayout.COMPACT
    width < 840.dp -> VehicleLayout.MEDIUM
    else -> VehicleLayout.EXPANDED
}

internal val ThirdColumnMinWidth = 1400.dp
