package ru.alarmtrade.specmp.core.permissions

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController

internal class DesktopPermissionsProvider : PermissionsProvider {
    private val controller = PermissionsController()
    override fun getController(): PermissionsController = controller
    override fun getBlePermission(): List<Permission> = blePermissions()
}

actual fun permissionsModule(): Module = module {
    single<PermissionsProvider> { DesktopPermissionsProvider() }
}
