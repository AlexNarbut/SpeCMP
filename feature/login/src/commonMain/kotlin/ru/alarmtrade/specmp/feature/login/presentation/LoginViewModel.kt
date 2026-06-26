package ru.alarmtrade.specmp.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.usecase.LoginUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.ResendActivationEmailUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val resendActivationEmailUseCase: ResendActivationEmailUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onLoginChanged(value: String) {
        _state.update {
            it.copy(
                login = value,
                authState = clearError(it.authState),
                activationResendState = LCEState.None(),
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _state.update {
            it.copy(
                password = value,
                authState = clearError(it.authState),
                activationResendState = LCEState.None(),
            )
        }
    }

    fun onPasswordVisibilityChanged() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun submit() {
        val credentials = _state.value.toCredentials()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    authState = LCEState.Loading(),
                    activationResendState = LCEState.None(),
                )
            }

            when (val response = loginUseCase(credentials)) {
                is Response.Success -> handleLoginResult(response.value)
                is Response.Error -> _state.update {
                    it.copy(authState = response.toLCEError(defaultMessage = "Не удалось войти"))
                }
            }
        }
    }

    fun resendActivationEmail() {
        val credentials = _state.value.toCredentials()

        viewModelScope.launch {
            _state.update { it.copy(activationResendState = LCEState.Loading()) }

            when (val response = resendActivationEmailUseCase(credentials)) {
                is Response.Success -> _state.update {
                    it.copy(activationResendState = LCEState.Content(Unit))
                }
                is Response.Error -> _state.update {
                    it.copy(
                        activationResendState = response.toLCEError(
                            defaultMessage = "Не удалось отправить письмо подтверждения"
                        )
                    )
                }
            }
        }
    }

    fun dismissActivationRequest() {
        _state.update {
            it.copy(
                authState = LCEState.None(),
                activationResendState = LCEState.None(),
            )
        }
    }

    private suspend fun handleLoginResult(result: LoginResult) {
        when (result) {
            LoginResult.Authorized -> {
                _state.update { it.copy(authState = LCEState.Content(result)) }
                _events.send(LoginEvent.Authorized)
            }
            LoginResult.ActivationRequired -> {
                _state.update { it.copy(authState = LCEState.Content(result)) }
            }
        }
    }

    private fun LoginState.toCredentials(): LoginCredentials {
        return LoginCredentials(
            login = login,
            password = password,
        )
    }

    private fun clearError(state: LCEState<LoginResult>): LCEState<LoginResult> {
        return if (state is LCEState.Error) LCEState.None() else state
    }

    private fun <T> Response.Error.toLCEError(defaultMessage: String): LCEState<T> {
        return LCEState.Error(message ?: exception.message ?: defaultMessage)
    }
}
