package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.bypass

sealed interface BypassEffect {
    /** Уйти с экрана: «Назад» вне обучения. */
    data object Close : BypassEffect
}
