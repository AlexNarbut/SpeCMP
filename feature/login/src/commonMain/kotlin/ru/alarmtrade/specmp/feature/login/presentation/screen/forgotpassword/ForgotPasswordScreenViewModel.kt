package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

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
import ru.alarmtrade.specmp.feature.login.domain.model.AuthValidationException
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class ForgotPasswordScreenViewModel(
    private val repository: LoginRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordScreenState())
    val state: StateFlow<ForgotPasswordScreenState> = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, resetState = clearError(it.resetState)) }
    }

    fun reset() {
        _state.value = ForgotPasswordScreenState()
    }

    fun submit() {
        val credentials = ResetPasswordCredentials(email = _state.value.email)

        viewModelScope.launch {
            _state.update { it.copy(resetState = LCEState.Loading()) }

            val response = credentials.validationError()?.let { error ->
                Response.Error(AuthValidationException(error))
            } ?: withContext(dispatchers.io) {
                repository.resetPassword(credentials)
            }

            when (response) {
                is Response.Success -> _state.update {
                    it.copy(resetState = LCEState.Content(Unit))
                }
                is Response.Error -> _state.update {
                    it.copy(resetState = response.toResetError())
                }
            }
        }
    }

    private fun clearError(state: LCEState<Unit>): LCEState<Unit> {
        return if (state is LCEState.Error) LCEState.None() else state
    }

    private fun Response.Error.toResetError(): LCEState<Unit> {
        val code = if (exception is AuthValidationException) {
            ERROR_REQUIRED_FIELDS
        } else {
            ERROR_RESET_PASSWORD
        }
        return LCEState.Error(code)
    }

    private companion object {
        const val ERROR_REQUIRED_FIELDS = "required_fields"
        const val ERROR_RESET_PASSWORD = "reset_password_error"
    }
}
