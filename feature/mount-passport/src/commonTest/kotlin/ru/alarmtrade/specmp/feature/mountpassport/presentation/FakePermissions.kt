package ru.alarmtrade.specmp.feature.mountpassport.presentation

import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.specmp.core.permissions.PermissionResult
import ru.alarmtrade.specmp.core.permissions.PermissionsProvider

internal class FakePermissions : PermissionsProvider {
    var camera: PermissionResult = PermissionResult.Granted
    var settingsOpened = 0

    override fun getController(): PermissionsController = error("not used")

    override fun getBlePermission(): List<Permission> = emptyList()

    override fun isLocationEnabled(): Boolean = true

    override fun openLocationSettings() = Unit

    override fun openBluetoothSettings() = Unit

    override fun openAppSettings() {
        settingsOpened++
    }

    override suspend fun requestCameraPermission(): PermissionResult = camera
}
