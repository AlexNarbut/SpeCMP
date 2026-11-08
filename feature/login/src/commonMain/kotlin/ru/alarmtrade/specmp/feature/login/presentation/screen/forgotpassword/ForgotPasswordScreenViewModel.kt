package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.dismissError
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

class ForgotPasswordScreenViewModel(private val repository: LoginRepository, private val dispatchers: AppDispatchers) :
    LegacyBaseViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordScreenState())
    val state: StateFlow<ForgotPasswordScreenState> = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, resetState = it.resetState.dismissError()) }
    }

    fun submit() {
        // Запрос уже идёт — повторное нажатие не отправляет его второй раз.
        if (_state.value.resetState is LCEState.Loading) return
        val credentials = ResetPasswordCredentials(email = _state.value.email)
        // Валидация — до запроса (как в регистрации): без мигания Loading перед ошибкой формы.
        if (credentials.validationError() != null) {
            _state.update { it.copy(resetState = LCEState.Error(AuthFormError.RequiredFields)) }
            return
        }

        safeLaunch {
            _state.update { it.copy(resetState = LCEState.Loading) }
            val response = withContext(dispatchers.io) { repository.resetPassword(credentials) }
            _state.update { it.copy(resetState = response.asLCEState(AuthFormError::from)) }
        }
    }
}
