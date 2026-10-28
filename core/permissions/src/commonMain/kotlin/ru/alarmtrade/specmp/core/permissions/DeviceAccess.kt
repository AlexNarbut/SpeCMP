@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.permissions

/** Тип доступа, который поясняется пользователю на экране-обосновании (зависит от платформы). */
enum class DeviceAccessRequirement { BLUETOOTH, USB }

/**
 * Требования доступа для поиска устройств на текущей платформе.
 * Пусто (desktop) → экран-обоснование не показывается, поток начинается со сканирования.
 */
internal expect fun platformDeviceAccessRequirements(): List<DeviceAccessRequirement>

/** Включён ли Bluetooth-адаптер. На платформах без BLE / со своим системным UI — всегда true. */
internal expect fun platformBluetoothEnabled(): Boolean
