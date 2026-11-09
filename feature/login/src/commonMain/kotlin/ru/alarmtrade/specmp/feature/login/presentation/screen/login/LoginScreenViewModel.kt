package ru.alarmtrade.specmp.feature.login.presentation.screen.login

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
    BaseViewModel<LoginScreenState, Nothing>(LoginScreenState()) {

    fun onLoginChanged(value: String) {
        reduce {
            copy(
                login = value,
                authState = authState.dismissError(),
                activationResendState = LCEState.None,
            )
        }
    }

    fun onPasswordChanged(value: String) {
        reduce {
            copy(
                password = value,
                authState = authState.dismissError(),
                activationResendState = LCEState.None,
            )
        }
    }

    fun onPasswordVisibilityChanged() {
        reduce { copy(isPasswordVisible = !isPasswordVisible) }
    }

    fun onResetForm() {
        reduce { LoginScreenState() }
    }

    fun onSubmit() {
        if (currentState.authState is LCEState.Loading) return
        val credentials = currentState.toCredentials()
        // Пустые поля — ошибка формы до запроса, а не «сервер отклонил».
        if (!credentials.isValid()) {
            reduce { copy(authState = LCEState.Error(AuthFormError.RequiredFields)) }
            return
        }

        intent {
            reduce {
                copy(
                    authState = LCEState.Loading,
                    activationResendState = LCEState.None,
                )
            }

            when (val response = withContext(dispatchers.io) { repository.login(credentials) }) {
                is Response.Success -> handleLoginResult(response.value)

                is Response.Error -> reduce {
                    copy(authState = LCEState.Error(AuthFormError.from(response.exception)))
                }
            }
        }
    }

    fun onResendActivationEmail() {
        if (currentState.activationResendState is LCEState.Loading) return
        val credentials = currentState.toCredentials()
        if (!credentials.isValid()) {
            reduce { copy(activationResendState = LCEState.Error(AuthFormError.RequiredFields)) }
            return
        }

        intent {
            reduce { copy(activationResendState = LCEState.Loading) }
            val response = withContext(dispatchers.io) { repository.resendActivationEmail(credentials) }
            reduce { copy(activationResendState = response.asLCEState(AuthFormError::from)) }
        }
    }

    fun onDismissActivationRequest() {
        reduce {
            copy(
                authState = LCEState.None,
                activationResendState = LCEState.None,
            )
        }
    }

    private fun handleLoginResult(result: LoginResult) {
        when (result) {
            // Переключение на авторизованную зону выполняет feature/auth-state,
            // здесь достаточно вернуть форму в исходное состояние.
            LoginResult.Authorized -> reduce { LoginScreenState() }

            LoginResult.ActivationRequired -> reduce { copy(authState = LCEState.Content(result)) }
        }
    }

    private fun LoginScreenState.toCredentials(): LoginCredentials = LoginCredentials(
        login = login,
        password = password,
    )
}
