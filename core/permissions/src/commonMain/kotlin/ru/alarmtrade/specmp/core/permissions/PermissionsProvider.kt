package ru.alarmtrade.specmp.core.permissions

import org.koin.core.module.Module
import ru.alarmtrade.permissions.common.DeniedAlwaysException
import ru.alarmtrade.permissions.common.DeniedException
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionState
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.permissions.common.RequestCanceledException

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

    /** Требования доступа для поиска устройств на платформе (пусто — desktop: сразу скан). */
    fun deviceAccessRequirements(): List<DeviceAccessRequirement> = platformDeviceAccessRequirements()

    /** Включён ли Bluetooth-адаптер (без BLE / со своим системным UI — всегда true). */
    fun isBluetoothEnabled(): Boolean = platformBluetoothEnabled()

    /** Открыть системные настройки приложения (разрешение отклонено навсегда). */
    fun openAppSettings() = getController().openAppSettings()

    /**
     * Запросить недостающие BLE-разрешения. Результат — без типов библиотеки разрешений: фичам они не нужны.
     * Отмена корутины пробрасывается.
     */
    suspend fun requestBlePermissions(): BlePermissionResult {
        val controller = getController()
        getBlePermission().forEach { permission ->
            try {
                if (controller.getPermissionState(permission) != PermissionState.Granted) {
                    controller.providePermission(permission)
                }
            } catch (ignored: DeniedAlwaysException) {
                return BlePermissionResult.Denied(canOpenSettings = true)
            } catch (ignored: DeniedException) {
                return BlePermissionResult.Denied(canOpenSettings = false)
            } catch (ignored: RequestCanceledException) {
                return BlePermissionResult.Denied(canOpenSettings = false)
            }
        }
        return BlePermissionResult.Granted
    }
}

/** Итог запроса BLE-разрешений. [Denied.canOpenSettings] — отклонено навсегда: исправить можно только в настройках. */
sealed interface BlePermissionResult {
    data object Granted : BlePermissionResult

    data class Denied(val canOpenSettings: Boolean) : BlePermissionResult
}

/** Koin-модуль с платформенной реализацией [PermissionsProvider]. */
expect fun permissionsModule(): Module
