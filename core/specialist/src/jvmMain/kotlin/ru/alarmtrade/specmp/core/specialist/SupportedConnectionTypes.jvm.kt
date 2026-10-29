package ru.alarmtrade.specmp.core.specialist

// Desktop: USB и COM-порт (BLE не используется).
actual fun supportedConnectionTypes(): Set<DeviceConnectionType> =
    setOf(DeviceConnectionType.USB, DeviceConnectionType.SERIAL)
