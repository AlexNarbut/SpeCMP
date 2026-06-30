package ru.alarmtrade.specmp.feature.login.domain.model

data class ResetPasswordCredentials(
    val email: String,
) {
    val normalizedEmail: String = email.trim()

    fun validationError(): ResetPasswordValidationError? {
        return when {
            normalizedEmail.isBlank() -> ResetPasswordValidationError.EmptyEmail
            else -> null
        }
    }
}
