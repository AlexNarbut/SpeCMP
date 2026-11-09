package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

sealed interface CloneEffect {
    data object Close : CloneEffect
}
