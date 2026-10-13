package ru.alarmtrade.specmp.feature.advancedmount.domain.model

/** Результат проверки блока на сервере: [hasAccess] = false — работа с блоком ограничена, [message] — причина. */
data class DeviceAccess(
    val hasAccess: Boolean,
    val message: String,
)
