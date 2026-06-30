package ru.alarmtrade.specmp.feature.login.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.usecase.CheckLoginStatusUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.LoginUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.ResendActivationEmailUseCase

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val resendActivationEmailUseCase: ResendActivationEmailUseCase,
    private val checkLoginStatusUseCase: CheckLoginStatusUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = sideEffectFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        checkLoginStatus()
    }

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

    fun retrySessionCheck() {
        checkLoginStatus()
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
                    it.copy(authState = response.toLCEError(errorCode = ERROR_AUTH))
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
                            errorCode = ERROR_ACTIVATION_RESEND
                        )
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(logoutState = LCEState.Loading()) }

            when (val response = logoutUseCase()) {
                is Response.Success -> _state.update {
                    it.copy(
                        password = "",
                        sessionState = LCEState.Content(false),
                        authState = LCEState.None(),
                        activationResendState = LCEState.None(),
                        logoutState = LCEState.None(),
                    )
                }
                is Response.Error -> _state.update {
                    it.copy(logoutState = response.toLCEError(errorCode = ERROR_LOGOUT))
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

    private fun checkLoginStatus() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    sessionState = LCEState.Loading(),
                    logoutState = LCEState.None(),
                )
            }

            when (val response = checkLoginStatusUseCase()) {
                is Response.Success -> _state.update {
                    if (it.isAuthorized) {
                        it
                    } else {
                        it.copy(sessionState = LCEState.Content(response.value))
                    }
                }
                is Response.Error -> _state.update {
                    it.copy(sessionState = response.toLCEError(errorCode = ERROR_SESSION_CHECK))
                }
            }
        }
    }

    private suspend fun handleLoginResult(result: LoginResult) {
        when (result) {
            LoginResult.Authorized -> {
                _state.update {
                    it.copy(
                        sessionState = LCEState.Content(true),
                        authState = LCEState.Content(result),
                    )
                }
                _events.tryEmit(LoginEvent.Authorized)
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

    private fun <T> Response.Error.toLCEError(errorCode: String): LCEState<T> {
        return LCEState.Error(errorCode)
    }

    private val LoginState.isAuthorized: Boolean
        get() = authState is LCEState.Content && authState.content is LoginResult.Authorized

    private companion object {
        const val ERROR_AUTH = "auth_error"
        const val ERROR_ACTIVATION_RESEND = "activation_resend_error"
        const val ERROR_SESSION_CHECK = "session_check_error"
        const val ERROR_LOGOUT = "logout_error"
    }
}
