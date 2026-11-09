package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

sealed interface DeviceInteractionEffect {
    data object Exit : DeviceInteractionEffect
}
