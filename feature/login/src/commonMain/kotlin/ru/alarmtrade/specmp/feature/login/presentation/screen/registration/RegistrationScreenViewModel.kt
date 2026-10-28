package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    BaseViewModel() {

    private val _state = MutableStateFlow(RegistrationScreenState())
    val state: StateFlow<RegistrationScreenState> = _state.asStateFlow()

    fun onFullNameChanged(value: String) {
        _state.update { it.withFieldEdited(RegistrationFieldError.FullName) { copy(fullName = value) } }
    }

    fun onEmailChanged(value: String) {
        _state.update { it.withFieldEdited(RegistrationFieldError.Email) { copy(email = value) } }
    }

    fun onPhoneNumberChanged(value: String) {
        _state.update { it.withFieldEdited(RegistrationFieldError.Phone) { copy(phoneNumber = value) } }
    }

    fun onPasswordChanged(value: String) {
        _state.update { it.withFieldEdited(RegistrationFieldError.Password) { copy(password = value) } }
    }

    fun onPasswordVisibilityChanged() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onInstallerConfirmedChanged(value: Boolean) {
        _state.update { it.copy(isInstallerConfirmed = value, registrationState = it.registrationState.dismissError()) }
    }

    fun submit() {
        val credentials = _state.value.toCredentials()

        val fieldErrors = credentials.validationErrors()
        if (fieldErrors.isNotEmpty()) {
            _state.update {
                it.copy(
                    fieldErrors = fieldErrors.mapTo(mutableSetOf()) { error -> error.toFieldError() },
                    registrationState = it.registrationState.dismissError()
                )
            }
            return
        }

        safeLaunch {
            _state.update {
                it.copy(
                    registrationState = LCEState.Loading
                )
            }

            val response = withContext(dispatchers.io) { repository.signUp(credentials) }
            _state.update { it.copy(registrationState = response.asLCEState(AuthFormError::from)) }
        }
    }

    private inline fun RegistrationScreenState.withFieldEdited(
        field: RegistrationFieldError,
        edit: RegistrationScreenState.() -> RegistrationScreenState
    ): RegistrationScreenState = edit().copy(
        registrationState = registrationState.dismissError(),
        fieldErrors = fieldErrors - field
    )

    private fun RegistrationScreenState.toCredentials(): SignUpCredentials = SignUpCredentials(
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        password = password,
        isInstallerConfirmed = isInstallerConfirmed
    )

    private fun SignUpValidationError.toFieldError(): RegistrationFieldError = when (this) {
        SignUpValidationError.InvalidFullName -> RegistrationFieldError.FullName
        SignUpValidationError.InvalidEmail -> RegistrationFieldError.Email
        SignUpValidationError.InvalidPhoneNumber -> RegistrationFieldError.Phone
        SignUpValidationError.PasswordTooShort -> RegistrationFieldError.Password
    }
}
