package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.dismissError
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

class ForgotPasswordScreenViewModel(private val repository: LoginRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel<ForgotPasswordScreenState, Nothing>(ForgotPasswordScreenState()) {

    fun onEmailChanged(value: String) {
        reduce { copy(email = value, resetState = resetState.dismissError()) }
    }

    fun onSubmit() {
        if (currentState.resetState is LCEState.Loading) return
        val credentials = ResetPasswordCredentials(email = currentState.email)
        // Валидация — до запроса: без мигания Loading перед ошибкой формы.
        if (credentials.validationError() != null) {
            reduce { copy(resetState = LCEState.Error(AuthFormError.RequiredFields)) }
            return
        }

        intent {
            reduce { copy(resetState = LCEState.Loading) }
            val response = withContext(dispatchers.io) { repository.resetPassword(credentials) }
            reduce { copy(resetState = response.asLCEState(AuthFormError::from)) }
        }
    }
}
