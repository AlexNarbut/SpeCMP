package ru.alarmtrade.specmp.core.permissions

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController

internal class AndroidPermissionsProvider(
    applicationContext: Context,
) : PermissionsProvider {
    private val controller = PermissionsController(applicationContext = applicationContext)
    override fun getController(): PermissionsController = controller
    override fun getBlePermission(): List<Permission> = blePermissions()
}

// get() резолвит android.content.Context, зарегистрированный composition root'ом.
actual fun permissionsModule(): Module = module {
    single<PermissionsProvider> { AndroidPermissionsProvider(get()) }
}
