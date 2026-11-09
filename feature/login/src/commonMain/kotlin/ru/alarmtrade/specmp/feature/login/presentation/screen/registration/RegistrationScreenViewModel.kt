package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.dismissError
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpValidationError
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import ru.alarmtrade.specmp.feature.login.presentation.model.RegistrationFieldError

class RegistrationScreenViewModel(private val repository: LoginRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel<RegistrationScreenState, Nothing>(RegistrationScreenState()) {

    fun onFullNameChanged(value: String) {
        reduce { withFieldEdited(RegistrationFieldError.FullName) { copy(fullName = value) } }
    }

    fun onEmailChanged(value: String) {
        reduce { withFieldEdited(RegistrationFieldError.Email) { copy(email = value) } }
    }

    fun onPhoneNumberChanged(value: String) {
        reduce { withFieldEdited(RegistrationFieldError.Phone) { copy(phoneNumber = value) } }
    }

    fun onPasswordChanged(value: String) {
        reduce { withFieldEdited(RegistrationFieldError.Password) { copy(password = value) } }
    }

    fun onPasswordVisibilityChanged() {
        reduce { copy(isPasswordVisible = !isPasswordVisible) }
    }

    fun onInstallerConfirmedChanged(value: Boolean) {
        reduce { copy(isInstallerConfirmed = value, registrationState = registrationState.dismissError()) }
    }

    fun onSubmit() {
        if (currentState.registrationState is LCEState.Loading) return
        val credentials = currentState.toCredentials()

        val fieldErrors = credentials.validationErrors()
        if (fieldErrors.isNotEmpty()) {
            reduce {
                copy(
                    fieldErrors = fieldErrors.mapTo(mutableSetOf()) { error -> error.toFieldError() },
                    registrationState = registrationState.dismissError(),
                )
            }
            return
        }

        intent {
            reduce { copy(registrationState = LCEState.Loading) }
            val response = withContext(dispatchers.io) { repository.signUp(credentials) }
            reduce { copy(registrationState = response.asLCEState(AuthFormError::from)) }
        }
    }

    private inline fun RegistrationScreenState.withFieldEdited(
        field: RegistrationFieldError,
        edit: RegistrationScreenState.() -> RegistrationScreenState,
    ): RegistrationScreenState = edit().copy(
        registrationState = registrationState.dismissError(),
        fieldErrors = fieldErrors - field,
    )

    private fun RegistrationScreenState.toCredentials(): SignUpCredentials = SignUpCredentials(
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        password = password,
        isInstallerConfirmed = isInstallerConfirmed,
    )

    private fun SignUpValidationError.toFieldError(): RegistrationFieldError = when (this) {
        SignUpValidationError.InvalidFullName -> RegistrationFieldError.FullName
        SignUpValidationError.InvalidEmail -> RegistrationFieldError.Email
        SignUpValidationError.InvalidPhoneNumber -> RegistrationFieldError.Phone
        SignUpValidationError.PasswordTooShort -> RegistrationFieldError.Password
    }
}
