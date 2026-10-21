package ru.alarmtrade.specmp.core.permissions

import androidx.compose.runtime.Composable
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController

// iOS: доступ к BLE запрашивает сам CoreBluetooth, привязка не требуется.
@Composable
actual fun BindEffect(permissionsController: PermissionsController) = Unit

actual fun blePermissions(): List<Permission> = emptyList()
