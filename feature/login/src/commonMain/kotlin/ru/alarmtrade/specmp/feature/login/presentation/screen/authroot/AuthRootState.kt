package ru.alarmtrade.specmp.feature.login.presentation.screen.authroot

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult

@Immutable
data class AuthRootState(
    val login: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val sessionState: LCEState<Boolean> = LCEState.None(),
    val authState: LCEState<LoginResult> = LCEState.None(),
    val activationResendState: LCEState<Unit> = LCEState.None(),
    val logoutState: LCEState<Unit> = LCEState.None(),
) {
    val isAuthLoading: Boolean get() = authState is LCEState.Loading
    val isActivationResendLoading: Boolean get() = activationResendState is LCEState.Loading
    val isLogoutLoading: Boolean get() = logoutState is LCEState.Loading
    val isActivationRequired: Boolean
        get() = authState is LCEState.Content && authState.content is LoginResult.ActivationRequired
    val isLoggedIn: Boolean
        get() = sessionState is LCEState.Content && sessionState.content

    val canSubmit: Boolean
        get() = login.isNotBlank() && password.isNotBlank() && !isAuthLoading
}
