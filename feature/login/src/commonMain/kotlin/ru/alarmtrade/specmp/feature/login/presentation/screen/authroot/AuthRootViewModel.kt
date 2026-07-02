package ru.alarmtrade.specmp.feature.login.presentation.screen.authroot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

class AuthRootViewModel(
    private val repository: LoginRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthRootState())
    val state: StateFlow<AuthRootState> = _state.asStateFlow()

    private val _events = sideEffectFlow<AuthRootEvent>()
    val events: SharedFlow<AuthRootEvent> = _events.asSharedFlow()

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

    fun resetLoginForm() {
        _state.update {
            it.copy(
                login = "",
                password = "",
                isPasswordVisible = false,
                authState = LCEState.None(),
                activationResendState = LCEState.None(),
            )
        }
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

            val response = if (credentials.isValid()) {
                withContext(dispatchers.io) { repository.login(credentials) }
            } else {
                Response.Error(IllegalArgumentException())
            }

            when (response) {
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

            val response = if (credentials.isValid()) {
                withContext(dispatchers.io) { repository.resendActivationEmail(credentials) }
            } else {
                Response.Error(IllegalArgumentException())
            }

            when (response) {
                is Response.Success -> _state.update {
                    it.copy(activationResendState = LCEState.Content(Unit))
                }
                is Response.Error -> _state.update {
                    it.copy(
                        activationResendState = response.toLCEError(
                            errorCode = ERROR_ACTIVATION_RESEND,
                        ),
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(logoutState = LCEState.Loading()) }

            when (val response = withContext(dispatchers.io) { repository.logout() }) {
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

            when (val response = withContext(dispatchers.io) { repository.isLoggedIn() }) {
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
                        login = "",
                        password = "",
                        isPasswordVisible = false,
                        sessionState = LCEState.Content(true),
                        authState = LCEState.Content(result),
                        activationResendState = LCEState.None(),
                    )
                }
                _events.tryEmit(AuthRootEvent.Authorized)
            }
            LoginResult.ActivationRequired -> {
                _state.update { it.copy(authState = LCEState.Content(result)) }
            }
        }
    }

    private fun AuthRootState.toCredentials(): LoginCredentials {
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

    private val AuthRootState.isAuthorized: Boolean
        get() = authState is LCEState.Content && authState.content is LoginResult.Authorized

    private companion object {
        const val ERROR_AUTH = "auth_error"
        const val ERROR_ACTIVATION_RESEND = "activation_resend_error"
        const val ERROR_SESSION_CHECK = "session_check_error"
        const val ERROR_LOGOUT = "logout_error"
    }
}
