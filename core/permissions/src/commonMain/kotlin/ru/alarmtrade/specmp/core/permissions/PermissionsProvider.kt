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

    suspend fun requestBlePermissions(): PermissionResult {
        val controller = getController()
        getBlePermission().forEach { permission ->
            try {
                if (controller.getPermissionState(permission) != PermissionState.Granted) {
                    controller.providePermission(permission)
                }
            } catch (ignored: DeniedAlwaysException) {
                return PermissionResult.Denied(canOpenSettings = true)
            } catch (ignored: DeniedException) {
                return PermissionResult.Denied(canOpenSettings = false)
            } catch (ignored: RequestCanceledException) {
                return PermissionResult.Denied(canOpenSettings = false)
            }
        }
        return PermissionResult.Granted
    }

    /** Камера для сканера штрих-кода; уже выдано — без системного диалога. */
    suspend fun requestCameraPermission(): PermissionResult {
        val controller = getController()
        return try {
            if (controller.getPermissionState(Permission.CAMERA) != PermissionState.Granted) {
                controller.providePermission(Permission.CAMERA)
            }
            PermissionResult.Granted
        } catch (ignored: DeniedAlwaysException) {
            PermissionResult.Denied(canOpenSettings = true)
        } catch (ignored: DeniedException) {
            PermissionResult.Denied(canOpenSettings = false)
        } catch (ignored: RequestCanceledException) {
            PermissionResult.Denied(canOpenSettings = false)
        }
    }
}

/** Результат запроса разрешения (BLE, камера); [Denied.canOpenSettings] — отклонено навсегда: только в настройках. */
sealed interface PermissionResult {
    data object Granted : PermissionResult

    data class Denied(val canOpenSettings: Boolean) : PermissionResult
}

expect fun permissionsModule(): Module
