package ru.alarmtrade.specmp.feature.device.settings.domain.model

/** Блок не вошёл в режим программирования при записи: сохранённый PIN неверный. */
class SettingsWrongPinException(cause: Throwable) : Exception(cause.message, cause)
