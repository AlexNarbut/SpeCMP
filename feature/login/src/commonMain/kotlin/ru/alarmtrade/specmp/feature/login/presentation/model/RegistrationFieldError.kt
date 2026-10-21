package ru.alarmtrade.specmp.feature.login.presentation.model

/**
 * Ошибка валидации конкретного поля формы регистрации.
 * UI маппит значение в supportingText поля (см. docs/patterns.md).
 */
enum class RegistrationFieldError {
    FullName,
    Email,
    Phone,
    Password
}
