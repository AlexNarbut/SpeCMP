package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

sealed interface ScanDeviceEffect {
    data object SessionCreated : ScanDeviceEffect

    data object ConnectionFailed : ScanDeviceEffect

    data object Close : ScanDeviceEffect

    data object BoundToAnotherPhone : ScanDeviceEffect

    data object PairingRequired : ScanDeviceEffect
}
