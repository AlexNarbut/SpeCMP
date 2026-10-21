package ru.alarmtrade.specmp.core.permissions

import ru.alarmtrade.permissions.common.Permission

/**
 * Разрешения, необходимые для BLE-сканирования на текущей платформе.
 * iOS/desktop — пусто (runtime-разрешений нет; на iOS доступ спрашивает CoreBluetooth).
 */
expect fun blePermissions(): List<Permission>
