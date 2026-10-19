package ru.alarmtrade.specmp.core.specialist

// iOS: только BLE (USB-host недоступен).
actual fun supportedConnectionTypes(): Set<DeviceConnectionType> = setOf(DeviceConnectionType.BLUETOOTH)
