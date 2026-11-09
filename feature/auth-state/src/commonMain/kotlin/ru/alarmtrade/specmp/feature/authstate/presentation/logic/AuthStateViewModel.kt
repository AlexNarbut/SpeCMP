package ru.alarmtrade.specmp.feature.authstate.presentation.logic

import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository

class AuthStateViewModel(private val repository: AuthStateRepository, private val dispatchers: AppDispatchers) :
    BaseViewModel<AuthStateScreenState, Nothing>(AuthStateScreenState()) {

    override suspend fun onStart() {
        observeSession()
    }

    fun onRetry() {
        observeSession()
    }

    // Не collectIntoState: проверка живёт всю жизнь VM. Перезапуск подписки (уход в фон) прошёл бы WAITING → AUTHED
    // и открыл бы новую сессию — ViewModelScope авторизованной зоны очистился бы.
    private fun observeSession() {
        launchReplacing(OBSERVE_SESSION_KEY) {
            // На время (повторной) проверки показываем загрузку, а не прошлую ошибку.
            reduce { withAuthState(AuthState.WAITING) }
            repository.observeIsLoggedIn()
                .map { it.toAuthState() }
                .flowOn(dispatchers.io)
                .collect { state -> reduce { withAuthState(state) } }
        }
    }

    // Каждый вход — новая сессия авторизованной зоны (новое хранилище её ViewModel).
    private fun AuthStateScreenState.withAuthState(newState: AuthState): AuthStateScreenState = when {
        newState == state -> this
        newState == AuthState.AUTHED -> AuthStateScreenState(newState, session + 1)
        else -> copy(state = newState)
    }

    private fun Response<Boolean>.toAuthState(): AuthState = when (this) {
        is Response.Success -> if (value) AuthState.AUTHED else AuthState.NO_AUTH
        is Response.Error -> AuthState.CHECK_FAILED
    }

    private companion object {
        const val OBSERVE_SESSION_KEY = "observeSession"
    }
}
