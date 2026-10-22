package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Маршрут экрана поиска устройства (регистрируется в графе `composeApp`). */
@Serializable
data object DeviceSelectionRoute : NavKey
