package ru.alarmtrade.specmp.feature.login.domain.model

sealed interface AuthValidationError

sealed interface SignUpValidationError : AuthValidationError {
    data object EmptyFullName : SignUpValidationError
    data object EmptyEmail : SignUpValidationError
    data object EmptyPhoneNumber : SignUpValidationError
    data object EmptyPassword : SignUpValidationError
    data object InstallerNotConfirmed : SignUpValidationError
}

sealed interface ResetPasswordValidationError : AuthValidationError {
    data object EmptyEmail : ResetPasswordValidationError
}

class AuthValidationException(
    val error: AuthValidationError,
) : IllegalArgumentException()
