package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest

sealed interface FirmwareUpdateEffect {
    data object OpenSupportChat : FirmwareUpdateEffect

    data class OpenArchive(val args: FirmwareArchiveRequest) : FirmwareUpdateEffect

    data object Exit : FirmwareUpdateEffect
}
