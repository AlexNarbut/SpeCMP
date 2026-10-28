package ru.alarmtrade.specmp.feature.login.presentation.screen.login

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

@Immutable
data class LoginScreenState(
    val login: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val authState: LCEState<LoginResult> = LCEState.None(),
    val authError: AuthFormError? = null,
    val activationResendState: LCEState<Unit> = LCEState.None(),
    val activationResendError: AuthFormError? = null
) {
    val isAuthLoading: Boolean get() = authState is LCEState.Loading

    /** Сервер отклонил логин/пароль — подсвечиваем поля; при сбое связи поля не виноваты. */
    val isAuthError: Boolean get() = authError == AuthFormError.Rejected
    val isActivationResendLoading: Boolean get() = activationResendState is LCEState.Loading
    val isActivationRequired: Boolean
        get() = authState is LCEState.Content && authState.content is LoginResult.ActivationRequired
    val canSubmit: Boolean
        get() = login.isNotBlank() && password.isNotBlank() && !isAuthLoading
}
