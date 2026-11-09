package ru.alarmtrade.specmp.core.permissions

import ru.alarmtrade.permissions.common.Permission

/** iOS/desktop — пусто: runtime-разрешений нет. */
expect fun blePermissions(): List<Permission>
