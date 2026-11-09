package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

sealed interface SerialFirmwareEffect {
    data object Exit : SerialFirmwareEffect
}
