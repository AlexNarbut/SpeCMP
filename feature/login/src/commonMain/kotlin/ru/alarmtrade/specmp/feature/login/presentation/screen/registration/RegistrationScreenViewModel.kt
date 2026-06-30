package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.AuthValidationException
import ru.alarmtrade.specmp.feature.login.domain.model.SignUpCredentials
import ru.alarmtrade.specmp.feature.login.domain.usecase.SignUpUseCase

class RegistrationScreenViewModel(
    private val signUpUseCase: SignUpUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationScreenState())
    val state: StateFlow<RegistrationScreenState> = _state.asStateFlow()

    fun onFullNameChanged(value: String) {
        _state.update { it.copy(fullName = value, registrationState = clearError(it.registrationState)) }
    }

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, registrationState = clearError(it.registrationState)) }
    }

    fun onPhoneNumberChanged(value: String) {
        _state.update { it.copy(phoneNumber = value, registrationState = clearError(it.registrationState)) }
    }

    fun onPasswordChanged(value: String) {
        _state.update { it.copy(password = value, registrationState = clearError(it.registrationState)) }
    }

    fun onPasswordVisibilityChanged() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onInstallerConfirmedChanged(value: Boolean) {
        _state.update { it.copy(isInstallerConfirmed = value, registrationState = clearError(it.registrationState)) }
    }

    fun reset() {
        _state.value = RegistrationScreenState()
    }

    fun submit() {
        val credentials = _state.value.toCredentials()

        viewModelScope.launch {
            _state.update { it.copy(registrationState = LCEState.Loading()) }

            when (val response = signUpUseCase(credentials)) {
                is Response.Success -> _state.update {
                    it.copy(registrationState = LCEState.Content(Unit))
                }
                is Response.Error -> _state.update {
                    it.copy(registrationState = response.toRegistrationError())
                }
            }
        }
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

    private fun clearError(state: LCEState<Unit>): LCEState<Unit> {
        return if (state is LCEState.Error) LCEState.None() else state
    }

    private fun Response.Error.toRegistrationError(): LCEState<Unit> {
        val code = if (exception is AuthValidationException) {
            ERROR_REQUIRED_FIELDS
        } else {
            ERROR_REGISTRATION
        }
        return LCEState.Error(code)
    }

    private companion object {
        const val ERROR_REQUIRED_FIELDS = "required_fields"
        const val ERROR_REGISTRATION = "registration_error"
    }
}
