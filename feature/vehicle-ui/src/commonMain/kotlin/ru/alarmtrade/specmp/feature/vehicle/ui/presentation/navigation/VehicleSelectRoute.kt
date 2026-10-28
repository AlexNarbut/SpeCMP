package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Маршрут выбора автомобиля для app-level графа (`composeApp`): экран — [VehicleSelectEntry] с [request]. */
@Serializable
data class VehicleSelectRoute(val request: VehicleSelectRequest) : NavKey
