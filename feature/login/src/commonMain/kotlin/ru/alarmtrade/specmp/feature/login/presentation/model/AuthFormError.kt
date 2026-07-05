package ru.alarmtrade.specmp.feature.login.presentation.model

/**
 * Типизированная ошибка submit-флоу auth-форм.
 * UI сам маппит значение в stringResource конкретного экрана (см. docs/patterns.md).
 */
enum class AuthFormError {
    RequiredFields,
    RequestFailed,
}
