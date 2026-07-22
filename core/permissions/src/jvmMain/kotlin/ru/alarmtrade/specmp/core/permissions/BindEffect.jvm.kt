package ru.alarmtrade.specmp.core.permissions

import androidx.compose.runtime.Composable
import ru.alarmtrade.permissions.common.Permission
import ru.alarmtrade.permissions.common.PermissionsController

@Composable
actual fun BindEffect(permissionsController: PermissionsController) = Unit

// Desktop: BLE-скан не используется, runtime-разрешений нет.
actual fun blePermissions(): List<Permission> = emptyList()
