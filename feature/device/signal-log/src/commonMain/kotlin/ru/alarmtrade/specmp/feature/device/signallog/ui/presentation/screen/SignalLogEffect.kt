package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

sealed interface SignalLogEffect {
    data object Sent : SignalLogEffect

    data object Queued : SignalLogEffect

    data class Saved(val fileName: String) : SignalLogEffect

    data object Cleared : SignalLogEffect

    data object Empty : SignalLogEffect

    data class Failed(val operation: SignalLogOperation) : SignalLogEffect
}

enum class SignalLogOperation { CLEAR, SAVE, OLDER }
