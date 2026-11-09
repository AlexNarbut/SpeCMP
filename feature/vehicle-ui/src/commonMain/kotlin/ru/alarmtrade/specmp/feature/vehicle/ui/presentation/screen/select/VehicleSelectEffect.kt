package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError

internal sealed interface VehicleSelectEffect {
    data class Picked(val car: CarSummary) : VehicleSelectEffect
    data object Exit : VehicleSelectEffect
    data class CanCopied(val code: String) : VehicleSelectEffect

    data class PickFailed(val error: CatalogError) : VehicleSelectEffect
}
