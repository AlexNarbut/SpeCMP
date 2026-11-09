package ru.alarmtrade.specmp.feature.device.firmware.domain.model

enum class FirmwareChannel(val isTest: Boolean) {
    RELEASE(isTest = false),
    BETA(isTest = true),
    ALPHA(isTest = true),
}
