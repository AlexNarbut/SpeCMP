package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

class MainScreenViewModel(
    private val authStateRepository: AuthStateRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(logoutState = LCEState.Loading()) }

            when (withContext(dispatchers.io) { authStateRepository.logout() }) {
                is Response.Success -> _state.update {
                    it.copy(logoutState = LCEState.Content(Unit))
                }
                is Response.Error -> _state.update {
                    it.copy(logoutState = LCEState.Error(ERROR_LOGOUT))
                }
            }
        }
    }

    private companion object {
        const val ERROR_LOGOUT = "logout_error"
    }
}
