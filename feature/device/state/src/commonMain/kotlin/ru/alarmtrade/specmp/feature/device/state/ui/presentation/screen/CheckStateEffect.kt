package ru.alarmtrade.specmp.feature.device.state.ui.presentation.screen

import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand

sealed interface CheckStateEffect {
    data class CommandFinished(val command: StateCommand, val outcome: CommandOutcome) : CheckStateEffect

    data object Close : CheckStateEffect

    data object Next : CheckStateEffect
}
