package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Маршрут «Архив прошивок» с главного экрана — без подключённого блока (app-level граф `composeApp`): выбор модели
 * блока, затем [FirmwareArchiveEntry] с [FirmwareArchiveRequest.noUnit].
 */
@Serializable
@SerialName("device.firmware.FirmwareArchiveRoute")
data object FirmwareArchiveRoute : NavKey
