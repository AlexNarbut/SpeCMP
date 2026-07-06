package ru.alarmtrade.specmp.feature.authstate.presentation.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.authstate.domain.model.AuthState
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import kotlin.time.Duration.Companion.seconds

class AuthStateViewModel(
    repository: AuthStateRepository,
    dispatchers: AppDispatchers,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AuthState> = retryTrigger
        .flatMapLatest { _ ->
            repository.observeIsLoggedIn()
                .map { it.toAuthState() }
                // На время (повторной) проверки показываем загрузку, а не прошлую ошибку.
                .onStart { emit(AuthState.WAITING) }
        }
        .distinctUntilChanged()
        .flowOn(dispatchers.io)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
            initialValue = AuthState.WAITING,
        )

    fun retry() {
        retryTrigger.update { it + 1 }
    }

    private fun Response<Boolean>.toAuthState(): AuthState {
        return when (this) {
            is Response.Success -> if (value) AuthState.AUTHED else AuthState.NO_AUTH
            is Response.Error -> AuthState.CHECK_FAILED
        }
    }
}
