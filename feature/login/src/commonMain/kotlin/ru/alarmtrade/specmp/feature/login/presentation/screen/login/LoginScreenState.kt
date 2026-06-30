package ru.alarmtrade.specmp.feature.login.presentation.screen.login

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.domain.model.LoginResult

@Immutable
data class LoginScreenState(
    val login: String,
    val password: String,
    val isPasswordVisible: Boolean,
    val authState: LCEState<LoginResult>,
    val activationResendState: LCEState<Unit>,
    val isAuthLoading: Boolean,
    val isActivationResendLoading: Boolean,
    val isActivationRequired: Boolean,
    val canSubmit: Boolean,
)
