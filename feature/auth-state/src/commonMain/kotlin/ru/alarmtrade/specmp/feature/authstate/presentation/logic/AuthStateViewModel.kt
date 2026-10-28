package ru.alarmtrade.specmp.feature.authstate.presentation.logic

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.authstate.domain.model.AuthState
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

class AuthStateViewModel(repository: AuthStateRepository, dispatchers: AppDispatchers) : BaseViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AuthStateScreenState> = retryTrigger
        .flatMapLatest { _ ->
            repository.observeIsLoggedIn()
                .map { it.toAuthState() }
                // На время (повторной) проверки показываем загрузку, а не прошлую ошибку.
                .onStart { emit(AuthState.WAITING) }
        }
        .distinctUntilChanged()
        // Каждый вход — новая сессия авторизованной зоны (новое хранилище её ViewModel).
        .scan(AuthStateScreenState()) { previous, state ->
            val newSession = state == AuthState.AUTHED && previous.state != AuthState.AUTHED
            AuthStateScreenState(state, if (newSession) previous.session + 1 else previous.session)
        }
        .flowOn(dispatchers.io)
        // Lazily, не WhileSubscribed: счётчик сессий в scan живёт всю жизнь VM — перезапуск подписки (уход в фон
        // при collectAsStateWithLifecycle) начал бы его заново и очистил бы ViewModelScope авторизованной зоны.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AuthStateScreenState()
        )

    fun retry() {
        retryTrigger.update { it + 1 }
    }

    private fun Response<Boolean>.toAuthState(): AuthState = when (this) {
        is Response.Success -> if (value) AuthState.AUTHED else AuthState.NO_AUTH
        is Response.Error -> AuthState.CHECK_FAILED
    }
}
