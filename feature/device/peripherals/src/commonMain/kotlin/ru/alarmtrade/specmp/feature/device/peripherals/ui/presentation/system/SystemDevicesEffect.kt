package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType

sealed interface SystemDevicesEffect {
    data class Blinking(val type: PeripheralType, val number: Int) : SystemDevicesEffect

    data object BlinkFailed : SystemDevicesEffect

    /** Устройство в режиме прошивки: закрыть сессию и открыть поиск устройств. */
    data object OpenDeviceSearch : SystemDevicesEffect
}
