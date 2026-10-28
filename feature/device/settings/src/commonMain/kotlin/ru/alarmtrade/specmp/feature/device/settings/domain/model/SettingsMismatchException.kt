package ru.alarmtrade.specmp.feature.device.settings.domain.model

/** Настройки источника не подходят для приёмника: другой набор настроек (SDK отказал по размеру). */
class SettingsMismatchException(cause: Throwable) : Exception(cause.message, cause)
