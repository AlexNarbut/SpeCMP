package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion

sealed interface FirmwareArchiveEffect {
    /** Выбрана версия для установки — результат архива. */
    data class Picked(val version: FirmwareVersion) : FirmwareArchiveEffect
}
