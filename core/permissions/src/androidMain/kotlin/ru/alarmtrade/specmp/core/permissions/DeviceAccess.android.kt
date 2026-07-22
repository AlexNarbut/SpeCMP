package ru.alarmtrade.specmp.core.permissions

import android.bluetooth.BluetoothAdapter

actual fun deviceAccessRequirements(): List<DeviceAccessRequirement> =
    listOf(DeviceAccessRequirement.BLUETOOTH, DeviceAccessRequirement.USB)

@Suppress("DEPRECATION")
actual fun isBluetoothEnabled(): Boolean = try {
    BluetoothAdapter.getDefaultAdapter()?.isEnabled == true
} catch (e: Throwable) {
    // Нет адаптера / нет доступа к состоянию — не блокируем поток.
    true
}
