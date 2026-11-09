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

    fun validationErrors(): Set<SignUpValidationError> = buildSet {
        if (normalizedFullName.isBlank()) add(SignUpValidationError.InvalidFullName)
        if (!EMAIL_REGEX.matches(normalizedEmail)) add(SignUpValidationError.InvalidEmail)
        if (!PHONE_REGEX.matches(normalizedPhoneNumber)) add(SignUpValidationError.InvalidPhoneNumber)
        if (password.length < MIN_PASSWORD_LENGTH) add(SignUpValidationError.PasswordTooShort)
    }

    private companion object {
        val EMAIL_REGEX = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
        val PHONE_REGEX = Regex("""^[+\d][\d\s()-]{5,}$""")
        const val MIN_PASSWORD_LENGTH = 6
    }
}
