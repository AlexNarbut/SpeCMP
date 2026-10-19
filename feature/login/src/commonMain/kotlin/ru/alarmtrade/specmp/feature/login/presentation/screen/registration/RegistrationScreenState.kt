package ru.alarmtrade.specmp.feature.login.presentation.screen.registration

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError
import ru.alarmtrade.specmp.feature.login.presentation.model.RegistrationFieldError

@Immutable
data class RegistrationScreenState(
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isInstallerConfirmed: Boolean = false,
    val registrationState: LCEState<Unit> = LCEState.None(),
    val registrationError: AuthFormError? = null,
    val fieldErrors: Set<RegistrationFieldError> = emptySet()
) {
    val isLoading: Boolean get() = registrationState is LCEState.Loading
    val isSuccess: Boolean get() = registrationState is LCEState.Content
    val canSubmit: Boolean
        get() = fullName.isNotBlank() &&
            email.isNotBlank() &&
            phoneNumber.isNotBlank() &&
            password.isNotBlank() &&
            isInstallerConfirmed &&
            !isLoading
}
