package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

sealed interface ScanDeviceEffect {
    data class SessionCreated(val transport: ConnectionTransport) : ScanDeviceEffect

    data object ConnectionFailed : ScanDeviceEffect

    data object Close : ScanDeviceEffect

    data object BoundToAnotherPhone : ScanDeviceEffect

    data object PairingRequired : ScanDeviceEffect
}
