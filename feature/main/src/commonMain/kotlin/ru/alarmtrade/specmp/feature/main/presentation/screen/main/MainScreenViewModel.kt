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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
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
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.mapContent
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.main.presentation.platform.isMainAppUpdateSupported

class MainScreenViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val mainRepository: MainRepository,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val isAppUpdateSupported = isMainAppUpdateSupported()
    private val _state = MutableStateFlow(
        MainScreenState(
            isAppUpdateSupported = isAppUpdateSupported,
            appVersionName = mainRepository.appVersion.name
        )
    )

    private val _effect = sideEffectFlow<MainScreenEffect>()
    val effect: SharedFlow<MainScreenEffect> = _effect.asSharedFlow()

    // Первичная загрузка — холодный flow, расшаренный Lazily: стартует при первой
    // подписке на state и выполняется ровно один раз за жизнь VM. Guard-флаг не нужен —
    // «однократность» гарантирует SharingStarted.Lazily (никогда не перезапускается).
    private val initialLoadingState: StateFlow<Boolean> = flow {
        logger.d { "Starting initial home data refresh" }
        safeLaunch { refreshDatabaseVersion() } // параллельно, скелетон не гейтит
        coroutineScope {
            awaitAll(
                async { loadUserProfile() },
                async { loadUserAppFeatures() },
                async { loadUserCommercial() },
                async { checkAppUpdate() }
            )
        }
        logger.d { "Initial home data refresh completed" }
        emit(false)
    }
        .catch { e ->
            logger.e(e) { "Initial home data refresh failed" }
            emit(false)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    private val chatAvailabilityState: Flow<LCEState<Boolean>> = mainRepository
        .observeChatAvailability()
        .map { response ->
            response.asLCEState { e -> e.message ?: "Error" }
        }
        .flowOn(dispatchers.io)
        .onStart { emit(LCEState.None()) }

    private val chatUnreadState: Flow<LCEState<Int>> = mainRepository
        .observeUnreadMessagesCount()
        .map { response ->
            response.asLCEState { e -> e.message ?: "Error" }
        }
        .flowOn(dispatchers.io)
        .onStart { emit(LCEState.None()) }

    // Поллинг чата живёт только пока экран подписан: WhileSubscribed останавливает
    // источники при уходе с экрана и перезапускает при возврате.
    val state: StateFlow<MainScreenState> = combine(
        _state,
        initialLoadingState,
        chatAvailabilityState,
        chatUnreadState
    ) { state, isInitialLoading, chatAvailability, chatUnread ->
        state.copy(
            isInitialLoading = isInitialLoading,
            chatAvailabilityState = chatAvailability,
            chatUnreadState = chatUnread
        )
    }
        .onStart { resetLogoutTransientState() }
        .flowOn(dispatchers.default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT),
            initialValue = _state.value
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
                    logoutState = LCEState.Loading(),
                    logoutError = null
                )
            }

            when (val response = withContext(dispatchers.io) { logoutUseCase.execute() }) {
                is Response.Success -> {
                    logger.d { "Logout succeeded" }
                    _state.update {
                        it.copy(
                            logoutState = LCEState.None(),
                            profileState = LCEState.None(),
                            databaseSyncState = LCEState.None(),
                            databaseVersion = null,
                            appFeaturesState = LCEState.None(),
                            userCommercialState = LCEState.None(),
                            appUpdateState = LCEState.None()
                        )
                    }
                }

                is Response.Error -> {
                    logger.d { "Logout failed" }
                    _state.update {
                        it.copy(
                            logoutState = LCEState.None(),
                            logoutError = RequestError.from(response.exception)
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadUserProfile() {
        _state.update { it.copy(profileState = LCEState.Loading()) }

        val response = loadWithTimeout { mainRepository.currentUserProfile() }
        _state.update {
            it.copy(
                profileState = response.asLCEState { e -> e.message ?: "Error" }
                    .mapContent { profile ->
                        MainProfile(name = profile.name, email = profile.email)
                    }
            )
        }
    }

    private suspend fun loadUserAppFeatures() {
        _state.update { it.copy(appFeaturesState = LCEState.Loading()) }

        val response = loadWithTimeout { mainRepository.currentUserAppFeatures() }
        _state.update {
            it.copy(
                appFeaturesState = response.asLCEState { e -> e.message ?: "Error" }
            )
        }
    }

    private suspend fun loadUserCommercial() {
        _state.update { it.copy(userCommercialState = LCEState.Loading()) }

        val response = loadWithTimeout { mainRepository.currentUserCommercial() }
        _state.update {
            it.copy(
                userCommercialState = response.asLCEState { e -> e.message ?: "Error" }
            )
        }
    }

    // Синхронизация — процесс: стадии зеркалятся в state по мере эмита,
    // поток всегда завершается одним из MainDatabaseSyncStatus.Finished.
    private suspend fun refreshDatabaseVersion() {
        _state.update { it.copy(databaseSyncState = LCEState.Loading()) }

        val lastStatus = mainRepository.synchronizeSettingsVersion()
            .flowOn(dispatchers.io)
            .onEach { response ->
                _state.update {
                    it.copy(databaseSyncState = response.asLCEState { e -> e.message ?: "Error" })
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

        _state.update { it.copy(appUpdateState = LCEState.Loading()) }
        logger.d { "Checking for app update" }

        val response = loadWithTimeout { mainRepository.checkAppUpdate() }
        _state.update {
            it.copy(
                appUpdateState = response.asLCEState { e -> e.message ?: "Error" }
            )
        }
    }

    private fun resetLogoutTransientState() {
        _state.update {
            if (it.logoutState is LCEState.None && it.logoutError == null) {
                it
            } else {
                it.copy(
                    logoutState = LCEState.None(),
                    logoutError = null
                )
            }
        }
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
