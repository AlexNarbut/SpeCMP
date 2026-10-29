package ru.alarmtrade.specmp.feature.appsettings.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Маршрут настроек приложения для app-level графа (`composeApp`): главная → меню → «Настройки». */
@Serializable
@SerialName("app-settings.AppSettingsRoute")
data object AppSettingsRoute : NavKey
