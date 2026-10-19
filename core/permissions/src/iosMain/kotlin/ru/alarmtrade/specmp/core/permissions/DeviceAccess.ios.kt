package ru.alarmtrade.specmp.core.permissions

// iOS: только BLE; состояние адаптера/доступ показывает сам CoreBluetooth при сканировании.
actual fun deviceAccessRequirements(): List<DeviceAccessRequirement> = listOf(DeviceAccessRequirement.BLUETOOTH)

actual fun isBluetoothEnabled(): Boolean = true
