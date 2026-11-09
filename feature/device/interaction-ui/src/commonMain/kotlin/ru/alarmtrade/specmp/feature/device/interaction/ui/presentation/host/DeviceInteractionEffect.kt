package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

sealed interface DeviceInteractionEffect {
    /** Сессия закрыта — выйти из flow. */
    data object Exit : DeviceInteractionEffect
}
