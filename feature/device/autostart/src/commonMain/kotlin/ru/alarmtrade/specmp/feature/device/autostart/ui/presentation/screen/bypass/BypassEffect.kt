package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

sealed interface BypassEffect {
    data object Close : BypassEffect
}
