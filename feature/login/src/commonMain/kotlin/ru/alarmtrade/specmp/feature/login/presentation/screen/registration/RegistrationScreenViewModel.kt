package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpValidationError
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import ru.alarmtrade.specmp.feature.login.presentation.model.RegistrationFieldError

class RegistrationScreenViewModel(
    private val repository: LoginRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

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
        _state.update { it.copy(isInstallerConfirmed = value, registrationError = null) }
    }

    fun reset() {
        _state.update { RegistrationScreenState() }
    }

    fun submit() {
        val credentials = _state.value.toCredentials()

        val fieldErrors = credentials.validationErrors()
        if (fieldErrors.isNotEmpty()) {
            _state.update {
                it.copy(
                    fieldErrors = fieldErrors.mapTo(mutableSetOf()) { error -> error.toFieldError() },
                    registrationError = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    registrationState = LCEState.Loading(),
                    registrationError = null,
                )
            }

            val response = withContext(dispatchers.io) {
                repository.signUp(credentials)
            }

            when (response) {
                is Response.Success -> _state.update {
                    it.copy(registrationState = LCEState.Content(Unit))
                }
                is Response.Error -> _state.update {
                    it.copy(
                        registrationState = LCEState.None(),
                        registrationError = AuthFormError.RequestFailed,
                    )
                }
            }
        }
    }

    private inline fun RegistrationScreenState.withFieldEdited(
        field: RegistrationFieldError,
        edit: RegistrationScreenState.() -> RegistrationScreenState,
    ): RegistrationScreenState {
        return edit().copy(
            registrationError = null,
            fieldErrors = fieldErrors - field,
        )
    }

    private fun RegistrationScreenState.toCredentials(): SignUpCredentials {
        return SignUpCredentials(
            fullName = fullName,
            email = email,
            phoneNumber = phoneNumber,
            password = password,
            isInstallerConfirmed = isInstallerConfirmed,
        )
    }

    private fun SignUpValidationError.toFieldError(): RegistrationFieldError {
        return when (this) {
            SignUpValidationError.InvalidFullName -> RegistrationFieldError.FullName
            SignUpValidationError.InvalidEmail -> RegistrationFieldError.Email
            SignUpValidationError.InvalidPhoneNumber -> RegistrationFieldError.Phone
            SignUpValidationError.PasswordTooShort -> RegistrationFieldError.Password
        }
    }
}
