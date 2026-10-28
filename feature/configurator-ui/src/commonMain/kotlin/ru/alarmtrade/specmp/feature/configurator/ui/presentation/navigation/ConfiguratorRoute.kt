package ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Маршрут конфигуратора для app-level графа (`composeApp`): экран — [ConfiguratorEntry]. */
@Serializable
@SerialName("configurator-ui.ConfiguratorRoute")
data object ConfiguratorRoute : NavKey
