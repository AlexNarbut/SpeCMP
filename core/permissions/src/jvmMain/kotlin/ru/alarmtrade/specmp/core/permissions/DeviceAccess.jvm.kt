package ru.alarmtrade.specmp.core.permissions

// Desktop: без экрана-обоснования (сразу сканирование), состояние адаптера не проверяем.
actual fun deviceAccessRequirements(): List<DeviceAccessRequirement> = emptyList()

actual fun isBluetoothEnabled(): Boolean = true
