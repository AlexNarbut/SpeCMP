package ru.alarmtrade.specmp.feature.login.presentation.screen.login

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
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

class LoginScreenViewModel(
    private val repository: LoginRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    fun onLoginChanged(value: String) {
        _state.update {
            it.copy(
                login = value,
                authError = null,
                activationResendState = LCEState.None(),
                activationResendError = null,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _state.update {
            it.copy(
                password = value,
                authError = null,
                activationResendState = LCEState.None(),
                activationResendError = null,
            )
        }
    }

    fun onPasswordVisibilityChanged() {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun resetLoginForm() {
        _state.update { LoginScreenState() }
    }

    fun submit() {
        val credentials = _state.value.toCredentials()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    authState = LCEState.Loading(),
                    authError = null,
                    activationResendState = LCEState.None(),
                    activationResendError = null,
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
                    it.copy(
                        authState = LCEState.None(),
                        authError = AuthFormError.RequestFailed,
                    )
                }
            }
        }
    }

    fun resendActivationEmail() {
        val credentials = _state.value.toCredentials()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    activationResendState = LCEState.Loading(),
                    activationResendError = null,
                )
            }

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
                        activationResendState = LCEState.None(),
                        activationResendError = AuthFormError.RequestFailed,
                    )
                }
            }
        }
    }

    fun dismissActivationRequest() {
        _state.update {
            it.copy(
                authState = LCEState.None(),
                authError = null,
                activationResendState = LCEState.None(),
                activationResendError = null,
            )
        }
    }

    private fun handleLoginResult(result: LoginResult) {
        when (result) {
            // Переключение на авторизованную зону выполняет feature/auth-state,
            // здесь достаточно вернуть форму в исходное состояние.
            LoginResult.Authorized -> _state.update { LoginScreenState() }
            LoginResult.ActivationRequired -> {
                _state.update { it.copy(authState = LCEState.Content(result)) }
            }
        }
    }

    private fun LoginScreenState.toCredentials(): LoginCredentials {
        return LoginCredentials(
            login = login,
            password = password,
        )
    }
}
