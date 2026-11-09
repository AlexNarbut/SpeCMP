@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.core.permissions

enum class DeviceAccessRequirement { BLUETOOTH, USB }

/** Пусто (desktop) — экран-обоснование не показывается. */
internal expect fun platformDeviceAccessRequirements(): List<DeviceAccessRequirement>

internal expect fun platformBluetoothEnabled(): Boolean
