package ru.alarmtrade.specmp.feature.login.presentation.screen.login

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.dismissError
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.login.domain.model.LoginCredentials
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

class LoginScreenViewModel(private val repository: LoginRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private val _state = MutableStateFlow(LoginScreenState())
    val state: StateFlow<LoginScreenState> = _state.asStateFlow()

    fun onLoginChanged(value: String) {
        _state.update {
            it.copy(
                login = value,
                authState = it.authState.dismissError(),
                activationResendState = LCEState.None
            )
        }
    }

    fun onPasswordChanged(value: String) {
        _state.update {
            it.copy(
                password = value,
                authState = it.authState.dismissError(),
                activationResendState = LCEState.None
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
        // Пустые поля — ошибка формы до запроса, а не «сервер отклонил».
        if (!credentials.isValid()) {
            _state.update { it.copy(authState = LCEState.Error(AuthFormError.RequiredFields)) }
            return
        }

        safeLaunch {
            _state.update {
                it.copy(
                    authState = LCEState.Loading,
                    activationResendState = LCEState.None
                )
            }

            when (val response = withContext(dispatchers.io) { repository.login(credentials) }) {
                is Response.Success -> handleLoginResult(response.value)

                is Response.Error -> _state.update {
                    it.copy(
                        authState = LCEState.Error(AuthFormError.from(response.exception))
                    )
                }
            }
        }
    }

    fun resendActivationEmail() {
        val credentials = _state.value.toCredentials()
        if (!credentials.isValid()) {
            _state.update { it.copy(activationResendState = LCEState.Error(AuthFormError.RequiredFields)) }
            return
        }

        safeLaunch {
            _state.update { it.copy(activationResendState = LCEState.Loading) }
            val response = withContext(dispatchers.io) { repository.resendActivationEmail(credentials) }
            _state.update { it.copy(activationResendState = response.asLCEState(AuthFormError::from)) }
        }
    }

    fun dismissActivationRequest() {
        _state.update {
            it.copy(
                authState = LCEState.None,
                activationResendState = LCEState.None
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

    private fun LoginScreenState.toCredentials(): LoginCredentials = LoginCredentials(
        login = login,
        password = password
    )
}
