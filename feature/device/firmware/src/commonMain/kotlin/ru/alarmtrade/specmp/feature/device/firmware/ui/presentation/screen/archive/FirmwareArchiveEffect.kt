package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion

sealed interface FirmwareArchiveEffect {
    data class Picked(val version: FirmwareVersion) : FirmwareArchiveEffect
}
