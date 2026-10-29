package ru.alarmtrade.specmp.core.specialist

actual fun supportedConnectionTypes(): Set<DeviceConnectionType> =
    setOf(DeviceConnectionType.BLUETOOTH, DeviceConnectionType.USB, DeviceConnectionType.SERIAL)
