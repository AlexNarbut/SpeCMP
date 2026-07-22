package ru.alarmtrade.specmp.core.permissions

import android.os.Build
import ru.alarmtrade.permissions.common.Permission

actual fun blePermissions(): List<Permission> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Permission.BLUETOOTH_SCAN, Permission.BLUETOOTH_CONNECT)
    } else {
        listOf(Permission.LOCATION, Permission.COARSE_LOCATION)
    }
