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

    /**
     * Включены ли службы геолокации, если это требуется для BLE-скана на платформе.
     * Android < 12 (API < 31): BLE-скан работает только при включённой геолокации →
     * реальная проверка. Android 12+ (`BLUETOOTH_SCAN neverForLocation`) и desktop/iOS —
     * геолокация не требуется → всегда `true`.
     */
    fun isLocationEnabled(): Boolean

    /** Открыть системные настройки геолокации (для случая выключенной службы). */
    fun openLocationSettings()

    /** Открыть системные настройки Bluetooth, если платформа это позволяет (иначе no-op). */
    fun openBluetoothSettings()
}

/** Koin-модуль с платформенной реализацией [PermissionsProvider]. */
expect fun permissionsModule(): Module
