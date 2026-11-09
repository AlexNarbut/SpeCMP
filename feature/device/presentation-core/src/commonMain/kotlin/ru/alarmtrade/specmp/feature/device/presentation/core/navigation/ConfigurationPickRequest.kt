package ru.alarmtrade.specmp.feature.device.presentation.core.navigation

enum class ConfigurationPickKind { MINE, CLOUD }

/** @property firmwareCanCode облачную конфигурацию с другим CAN-кодом загрузить нельзя. */
data class ConfigurationPickRequest(
    val kind: ConfigurationPickKind,
    val deviceName: String,
    val firmwareCanCode: String?,
)
