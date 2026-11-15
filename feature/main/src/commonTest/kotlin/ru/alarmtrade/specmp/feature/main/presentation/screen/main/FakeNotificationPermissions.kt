package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider

class FakeNotificationPermissions(var result: PermissionResult = PermissionResult.Granted) : PermissionsProvider {
    var notificationRequests = 0

    override suspend fun requestNotificationPermission(): PermissionResult {
        notificationRequests++
        return result
    }

    override fun getController(): PermissionsController = error("not used")
    override fun getBlePermission(): List<Permission> = error("not used")
    override fun isLocationEnabled(): Boolean = error("not used")
    override fun openLocationSettings() = error("not used")
    override fun openBluetoothSettings() = error("not used")
}
