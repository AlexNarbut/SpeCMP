package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Маршрут экрана поиска устройства (регистрируется в графе `composeApp`). */
@Serializable
@SerialName("device.selection.DeviceSelectionRoute")
data object DeviceSelectionRoute : NavKey
