package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleLayout

/** Раскладка по ширине окна (Window Size Classes): < 600 — Compact, < 840 — Medium, иначе Expanded. */
internal fun vehicleLayoutOf(width: Dp): VehicleLayout = when {
    width < 600.dp -> VehicleLayout.COMPACT
    width < 840.dp -> VehicleLayout.MEDIUM
    else -> VehicleLayout.EXPANDED
}

/** Третья колонка «Комплектации» на Expanded — от этой ширины окна. */
internal val ThirdColumnMinWidth = 1400.dp
