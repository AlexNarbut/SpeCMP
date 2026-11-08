package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.mapContent
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.model.MainError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.main.presentation.platform.isMainAppUpdateSupported

class MainScreenViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val mainRepository: MainRepository,
    private val dispatchers: AppDispatchers,
) : BaseViewModel() {

    private val isAppUpdateSupported = isMainAppUpdateSupported()
    private val _state = MutableStateFlow(
        MainScreenState(
            isAppUpdateSupported = isAppUpdateSupported,
            appVersionName = mainRepository.appVersion.name,
        ),
    )

    private val _effect = sideEffectFlow<MainScreenEffect>()
    val effect: SharedFlow<MainScreenEffect> = _effect.asSharedFlow()

    private val chatAvailabilityState: Flow<LCEState<Boolean, MainError>> = mainRepository
        .observeChatAvailability()
        .map { response ->
            response.asLCEState(MainError::from)
        }
        .flowOn(dispatchers.io)
        .onStart { emit(LCEState.None) }

    // Поллинг чата живёт только пока экран подписан: WhileSubscribed останавливает
    // источники при уходе с экрана и перезапускает при возврате.
    val state: StateFlow<MainScreenState> = combine(_state, chatAvailabilityState) { state, chatAvailability ->
        state.copy(chatAvailabilityState = chatAvailability).withActions()
    }
        // Первичная загрузка — один раз за жизнь VM, при первой подписке на state; скелетон — пока она идёт.
        .launchOnFirstStart { initialRefresh() }
        .onStart { resetLogoutTransientState() }
        .flowOn(dispatchers.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT),
            initialValue = _state.value,
        )

    fun onMenuClicked() {
        if (state.value.isInitialLoading) return

        val profile = state.value.profileState
        if (profile !is LCEState.Content && profile !is LCEState.Loading) {
            safeLaunch { loadUserProfile() }
        }

        _state.update { it.copy(isMenuOpen = !it.isMenuOpen) }
    }

    fun onMenuDismissed() {
        _state.update { it.copy(isMenuOpen = false) }
    }

    fun onDatabaseSyncClicked() {
        if (_state.value.isDatabaseSyncing) return

        safeLaunch {
            refreshDatabaseVersion()
        }
    }

    fun onAppUpdateCheckClicked() {
        val currentState = _state.value
        if (!currentState.isAppUpdateSupported || currentState.isAppUpdateChecking) return

        safeLaunch {
            checkAppUpdate()
        }
    }

    fun onAppUpdateBannerClicked() {
        _state.update { it.copy(isMenuOpen = false) }
        _effect.tryEmit(MainScreenEffect.Navigate(MainNavigationTarget.AppUpdate))
    }

    fun onHomeActionClicked(action: MainHomeAction) {
        _effect.tryEmit(MainScreenEffect.Navigate(action.target))
    }

    fun onSettingsClicked() {
        _state.update { it.copy(isMenuOpen = false) }
        _effect.tryEmit(MainScreenEffect.Navigate(MainNavigationTarget.Settings))
    }

    fun logout() {
        if (_state.value.isLogoutInProgress) return

        safeLaunch {
            _state.update {
                it.copy(
                    logoutState = LCEState.Loading,
                )
            }

            when (val response = withContext(dispatchers.io) { logoutUseCase.execute() }) {
                // Остаёмся в Loading: кнопка заблокирована, пока auth-state не сменит зону; эта VM уйдёт вместе с
                // авторизованной зоной (ViewModelScope по сессии) — state сбрасывать не нужно.
                is Response.Success -> logger.d { "Logout succeeded" }

                is Response.Error -> {
                    logger.d { "Logout failed" }
                    _state.update {
                        it.copy(
                            logoutState = LCEState.Error(MainError.from(response.exception)),
                        )
                    }
                }
            }
        }
    }

    private suspend fun initialRefresh() {
        logger.d { "Starting initial home data refresh" }
        // Параллельно, скелетон не гейтят: синхронизация базы и проверка обновления приложения показывают себя
        // своими индикаторами вверху экрана (проверка обновления — сетевой запрос, может идти долго).
        safeLaunch { refreshDatabaseVersion() }
        safeLaunch { checkAppUpdate() }
        try {
            coroutineScope {
                awaitAll(
                    async { loadUserProfile() },
                    async { loadUserAppFeatures() },
                    async { loadUserCommercial() },
                )
            }
            logger.d { "Initial home data refresh completed" }
        } finally {
            // Сбой (необработанное исключение логирует safeLaunch) скелетон тоже убирает.
            _state.update { it.copy(isInitialLoading = false) }
        }
    }

    private suspend fun loadUserProfile() {
        _state.update { it.copy(profileState = LCEState.Loading) }

        val response = loadWithTimeout { mainRepository.currentUserProfile() }
        _state.update {
            it.copy(
                profileState = response.asLCEState(MainError::from)
                    .mapContent { profile ->
                        MainProfile(name = profile.name, email = profile.email)
                    },
            )
        }
    }

    private suspend fun loadUserAppFeatures() {
        _state.update { it.copy(appFeaturesState = LCEState.Loading) }

        val response = loadWithTimeout { mainRepository.currentUserAppFeatures() }
        _state.update {
            it.copy(
                appFeaturesState = response.asLCEState(MainError::from),
            )
        }
    }

    private suspend fun loadUserCommercial() {
        _state.update { it.copy(userCommercialState = LCEState.Loading) }

        val response = loadWithTimeout { mainRepository.currentUserCommercial() }
        _state.update {
            it.copy(
                userCommercialState = response.asLCEState(MainError::from),
            )
        }
    }

    // Синхронизация — процесс: стадии зеркалятся в state по мере эмита,
    // поток всегда завершается одним из MainDatabaseSyncStatus.Finished.
    private suspend fun refreshDatabaseVersion() {
        _state.update { it.copy(databaseSyncState = LCEState.Loading) }

        val lastStatus = mainRepository.synchronizeSettingsVersion()
            .flowOn(dispatchers.io)
            .onEach { response ->
                _state.update {
                    it.copy(databaseSyncState = response.asLCEState(MainError::from))
                }
            }
            .lastOrNull()
            ?.getValueIfSuccess()

        if (lastStatus is MainDatabaseSyncStatus.Finished.Success) {
            _state.update { it.copy(databaseVersion = lastStatus.newVersion) }
        } else {
            logger.d { "Falling back to current database version" }
            loadCurrentDatabaseVersion()
        }
    }

    private suspend fun loadCurrentDatabaseVersion() {
        val response = withContext(dispatchers.io) { mainRepository.currentSettingsVersion() }
        _state.update { current ->
            current.copy(databaseVersion = response.getValueIfSuccess() ?: current.databaseVersion)
        }
    }

    private suspend fun checkAppUpdate() {
        if (!_state.value.isAppUpdateSupported) return

        _state.update { it.copy(appUpdateState = LCEState.Loading) }
        logger.d { "Checking for app update" }

        val response = loadWithTimeout { mainRepository.checkAppUpdate() }
        _state.update {
            it.copy(
                appUpdateState = response.asLCEState(MainError::from),
            )
        }
    }

    /** Ошибка выхода — разовая: при возврате на экран не показываем. Loading (выход идёт или удался) не трогаем. */
    private fun resetLogoutTransientState() {
        _state.update { if (it.logoutState is LCEState.Error) it.copy(logoutState = LCEState.None) else it }
    }

    private suspend fun <T> loadWithTimeout(block: suspend () -> Response<T>): Response<T> =
        withContext(dispatchers.io) {
            withTimeoutOrNull(STARTUP_LOAD_TIMEOUT) { block() }
        } ?: Response.Error(Exception("Request timed out"))

    private companion object {
        val STOP_TIMEOUT = 5.seconds
        val STARTUP_LOAD_TIMEOUT = 10.seconds
    }
}
