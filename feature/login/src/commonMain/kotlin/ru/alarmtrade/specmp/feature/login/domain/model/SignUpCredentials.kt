package ru.alarmtrade.specmp.feature.login.domain.model

data class SignUpCredentials(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val isInstallerConfirmed: Boolean,
) {
    val normalizedFullName: String = fullName.trim()
    val normalizedEmail: String = email.trim()
    val normalizedPhoneNumber: String = phoneNumber.trim()

    fun validationError(): SignUpValidationError? {
        return when {
            normalizedFullName.isBlank() -> SignUpValidationError.EmptyFullName
            normalizedEmail.isBlank() -> SignUpValidationError.EmptyEmail
            normalizedPhoneNumber.isBlank() -> SignUpValidationError.EmptyPhoneNumber
            password.isBlank() -> SignUpValidationError.EmptyPassword
            !isInstallerConfirmed -> SignUpValidationError.InstallerNotConfirmed
            else -> null
        }
    }
}
