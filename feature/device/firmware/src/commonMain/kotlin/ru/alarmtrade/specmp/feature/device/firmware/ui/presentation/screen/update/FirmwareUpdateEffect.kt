package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update

import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive.FirmwareArchiveRequest

/** Одноразовые события экрана обновления. */
sealed interface FirmwareUpdateEffect {
    /** Открыть чат техподдержки («Написать в поддержку» на ошибке). */
    data object OpenSupportChat : FirmwareUpdateEffect

    /** Открыть архив прошивок выбранного модуля и канала. */
    data class OpenArchive(val args: FirmwareArchiveRequest) : FirmwareUpdateEffect

    /** Выйти из режима обновления (закрыть сессию и flow). */
    data object Exit : FirmwareUpdateEffect
}
