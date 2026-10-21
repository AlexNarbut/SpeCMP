package ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.login.domain.model.ResetPasswordCredentials
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.model.AuthFormError

class ForgotPasswordScreenViewModel(private val repository: LoginRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordScreenState())
    val state: StateFlow<ForgotPasswordScreenState> = _state.asStateFlow()

    fun onEmailChanged(value: String) {
        _state.update { it.copy(email = value, resetError = null) }
    }

    fun reset() {
        _state.update { ForgotPasswordScreenState() }
    }

    fun submit() {
        val credentials = ResetPasswordCredentials(email = _state.value.email)

        safeLaunch {
            _state.update {
                it.copy(
                    resetState = LCEState.Loading(),
                    resetError = null
                )
            }

            if (credentials.validationError() != null) {
                _state.update {
                    it.copy(
                        resetState = LCEState.None(),
                        resetError = AuthFormError.RequiredFields
                    )
                }
                return@safeLaunch
            }

            val response = withContext(dispatchers.io) {
                repository.resetPassword(credentials)
            }

            when (response) {
                is Response.Success -> _state.update {
                    it.copy(resetState = LCEState.Content(Unit))
                }

                is Response.Error -> _state.update {
                    it.copy(
                        resetState = LCEState.None(),
                        resetError = AuthFormError.RequestFailed
                    )
                }
            }
        }
    }
}
