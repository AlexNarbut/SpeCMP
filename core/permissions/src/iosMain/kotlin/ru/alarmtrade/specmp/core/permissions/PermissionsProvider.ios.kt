@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.permissions

import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
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

    // Библиотека разрешений открывает настройки через UIApplication.openURL(_:) — в iOS 18 он ничего не делает.
    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }
}

actual fun permissionsModule(): Module = module {
    single<PermissionsProvider> { IosPermissionsProvider() }
}
