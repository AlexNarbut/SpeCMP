package ru.alarmtrade.specmp.core.permissions

import android.bluetooth.BluetoothAdapter

internal actual fun platformDeviceAccessRequirements(): List<DeviceAccessRequirement> =
    listOf(DeviceAccessRequirement.BLUETOOTH, DeviceAccessRequirement.USB)

@Suppress("DEPRECATION")
internal actual fun platformBluetoothEnabled(): Boolean = try {
    BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
} catch (ignored: Throwable) {
    // Нет адаптера / нет доступа к состоянию — не блокируем поток.
    true
}
