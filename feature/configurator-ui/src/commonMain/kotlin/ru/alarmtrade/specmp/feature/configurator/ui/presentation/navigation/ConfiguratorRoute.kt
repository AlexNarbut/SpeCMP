package ru.alarmtrade.specmp.feature.configurator.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Маршрут конфигуратора для app-level графа (`composeApp`): экран — [ConfiguratorEntry]. */
@Serializable
data object ConfiguratorRoute : NavKey
