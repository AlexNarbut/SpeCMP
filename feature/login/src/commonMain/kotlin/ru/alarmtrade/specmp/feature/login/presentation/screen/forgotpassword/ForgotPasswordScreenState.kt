package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

@Immutable
data class ForgotPasswordScreenState(
    val email: String = "",
    val resetState: LCEState<Unit, AuthFormError> = LCEState.None
) {
    val resetError: AuthFormError? get() = (resetState as? LCEState.Error)?.error
    val isLoading: Boolean get() = resetState is LCEState.Loading
    val isSuccess: Boolean get() = resetState is LCEState.Content
    val canSubmit: Boolean get() = email.isNotBlank() && !isLoading
}
