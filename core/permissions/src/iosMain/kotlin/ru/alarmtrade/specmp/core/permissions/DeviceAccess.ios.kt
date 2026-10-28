package ru.alarmtrade.specmp.core.permissions

// iOS: только BLE; состояние адаптера/доступ показывает сам CoreBluetooth при сканировании.
internal actual fun platformDeviceAccessRequirements(): List<DeviceAccessRequirement> =
    listOf(DeviceAccessRequirement.BLUETOOTH)

internal actual fun platformBluetoothEnabled(): Boolean = true
