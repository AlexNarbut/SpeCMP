package ru.alarmtrade.specmp.core.permissions

// Desktop: без экрана-обоснования (сразу сканирование), состояние адаптера не проверяем.
internal actual fun platformDeviceAccessRequirements(): List<DeviceAccessRequirement> = emptyList()

internal actual fun platformBluetoothEnabled(): Boolean = true
