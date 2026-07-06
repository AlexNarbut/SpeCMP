package ru.alarmtrade.specmp.feature.login.domain.model

data class LoginCredentials(
    val login: String,
    val password: String,
) {
    val normalizedLogin: String = login.trim()

    fun isValid(): Boolean = normalizedLogin.isNotBlank() && password.isNotBlank()
}
