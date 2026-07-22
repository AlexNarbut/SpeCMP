package ru.alarmtrade.specmp.core.permissions

import org.koin.core.module.Module
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController

/**
 * App-scoped доступ к moko [PermissionsController] и списку BLE-разрешений платформы.
 * Регистрируется в DI через [permissionsModule]; контроллер биндится один раз в корне
 * приложения (`BindEffect`), а фичам инжектится напрямую (ViewModel сам проверяет доступ).
 */
interface PermissionsProvider {
    fun getController(): PermissionsController
    fun getBlePermission(): List<Permission>
}

/** Koin-модуль с платформенной реализацией [PermissionsProvider]. */
expect fun permissionsModule(): Module
