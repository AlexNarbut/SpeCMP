package ru.alarmtrade.specmp.core.permissions

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.permissions.common.ios.PermissionsController as IosPermissionsController

internal class IosPermissionsProvider : PermissionsProvider {
    private val controller: PermissionsController = IosPermissionsController()
    override fun getController(): PermissionsController = controller
    override fun getBlePermission(): List<Permission> = blePermissions()
    override fun isLocationEnabled(): Boolean = true
    override fun openLocationSettings() = Unit
    // iOS: программно открыть системные настройки Bluetooth нельзя — состояние адаптера
    // показывает сам CoreBluetooth. No-op.
    override fun openBluetoothSettings() = Unit
}

actual fun permissionsModule(): Module = module {
    single<PermissionsProvider> { IosPermissionsProvider() }
}
