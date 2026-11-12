package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

enum class PairingMessage { CANCELLED, DELETED, UPDATE_SENT, COMMAND_FAILED }

sealed interface PairingEffect {
    data class Snackbar(val message: PairingMessage) : PairingEffect
}
