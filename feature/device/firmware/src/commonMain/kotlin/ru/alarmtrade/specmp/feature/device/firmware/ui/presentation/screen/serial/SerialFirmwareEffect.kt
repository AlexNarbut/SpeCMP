package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.serial

/** Одноразовые события экрана прошивки по COM. */
sealed interface SerialFirmwareEffect {
    /** Выйти из flow устройства (сессию закрывает хост). */
    data object Exit : SerialFirmwareEffect
}
