package ru.alarmtrade.specmp.feature.login.domain.model

sealed interface AuthValidationError

sealed interface SignUpValidationError : AuthValidationError {
    data object InvalidFullName : SignUpValidationError
    data object InvalidEmail : SignUpValidationError
    data object InvalidPhoneNumber : SignUpValidationError
    data object PasswordTooShort : SignUpValidationError
}

sealed interface ResetPasswordValidationError : AuthValidationError {
    data object EmptyEmail : ResetPasswordValidationError
}
