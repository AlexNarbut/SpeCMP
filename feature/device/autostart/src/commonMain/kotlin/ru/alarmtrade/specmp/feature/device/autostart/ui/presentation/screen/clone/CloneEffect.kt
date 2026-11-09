package ru.alarmtrade.specmp.feature.device.autostart.ui.presentation.screen.clone

sealed interface CloneEffect {
    /** Уйти с экрана: «Назад» вне процедуры. */
    data object Close : CloneEffect
}
