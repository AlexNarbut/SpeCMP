package ru.alarmtrade.specmp.core.specialist

// Desktop: только USB (BLE не используется).
actual fun supportedConnectionTypes(): Set<DeviceConnectionType> =
    setOf(DeviceConnectionType.USB)
