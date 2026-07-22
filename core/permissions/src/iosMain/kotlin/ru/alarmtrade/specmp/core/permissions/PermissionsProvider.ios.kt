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
}

actual fun permissionsModule(): Module = module {
    single<PermissionsProvider> { IosPermissionsProvider() }
}
