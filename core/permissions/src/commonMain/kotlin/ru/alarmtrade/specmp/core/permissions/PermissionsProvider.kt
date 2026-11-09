package ru.alarmtrade.specmp.core.permissions

import org.koin.core.module.Module
import ru.alarmtrade.permissions.common.DeniedAlwaysException
import ru.alarmtrade.permissions.common.DeniedException
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionState
import ru.alarmtrade.permissions.common.PermissionsController
import ru.alarmtrade.permissions.common.RequestCanceledException

/** Контроллер биндится один раз в корне приложения (`BindEffect`), фичам инжектится напрямую. */
interface PermissionsProvider {
    fun getController(): PermissionsController
    fun getBlePermission(): List<Permission>

    /** Android < 12: BLE-скан работает только с включённой геолокацией; на остальных платформах — всегда `true`. */
    fun isLocationEnabled(): Boolean

    fun openLocationSettings()

    /** Открыть системные настройки Bluetooth, если платформа это позволяет (иначе no-op). */
    fun openBluetoothSettings()

    /** Требования доступа для поиска устройств на платформе (пусто — desktop: сразу скан). */
    fun deviceAccessRequirements(): List<DeviceAccessRequirement> = platformDeviceAccessRequirements()

    /** Включён ли Bluetooth-адаптер (без BLE / со своим системным UI — всегда true). */
    fun isBluetoothEnabled(): Boolean = platformBluetoothEnabled()

    fun openAppSettings() = getController().openAppSettings()

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

/** [Denied.canOpenSettings] — отклонено навсегда: исправить можно только в настройках. */
sealed interface BlePermissionResult {
    data object Granted : BlePermissionResult

    data class Denied(val canOpenSettings: Boolean) : BlePermissionResult
}

expect fun permissionsModule(): Module
