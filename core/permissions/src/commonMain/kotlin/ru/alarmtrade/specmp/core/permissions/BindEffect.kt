package ru.alarmtrade.specmp.core.permissions

import androidx.compose.runtime.Composable
import ru.alarmtrade.permissions.common.PermissionsController

/** На Android обязателен до запроса разрешений; на iOS/desktop — no-op. */
@Composable
expect fun BindEffect(permissionsController: PermissionsController)
