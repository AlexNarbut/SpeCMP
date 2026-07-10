package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.mapContent
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainLogoutError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.main.presentation.platform.isMainAppUpdateSupported

class MainScreenViewModel(
    private val authStateRepository: AuthStateRepository,
    private val mainRepository: MainRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val logger = Logger.withTag("MainScreenViewModel")

    private val isAppUpdateSupported = isMainAppUpdateSupported()
    private val _state = MutableStateFlow(
        MainScreenState(
            isAppUpdateSupported = isAppUpdateSupported,
            appVersionName = mainRepository.appVersion.name,
        ),
    )
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<MainScreenEffect>()
    val effect: SharedFlow<MainScreenEffect> = _effect.asSharedFlow()

    private var initialRefreshStarted = false
    private var chatAvailabilityJob: Job? = null
    private var chatUnreadJob: Job? = null

    fun onScreenEntered() {
        resetLogoutTransientState()

        if (initialRefreshStarted) return
        initialRefreshStarted = true

        _state.update { it.copy(isInitialLoading = true) }
        logger.d { "Starting initial home data refresh" }

        viewModelScope.launch {
            listOf(
                async { loadUserProfile() },
                async { loadUserAppFeatures() },
                async { loadUserCommercial() },
                async { checkAppUpdate() },
            ).awaitAll()

            _state.update { it.copy(isInitialLoading = false) }
            logger.d { "Initial home data refresh completed" }
        }

        viewModelScope.launch { refreshDatabaseVersion() }

        startChatPolling()
    }

    fun onMenuClicked() {
        _state.update {
            if (it.isInitialLoading) {
                it
            } else {
                it.copy(isMenuOpen = !it.isMenuOpen)
            }
        }
    }

    fun onMenuDismissed() {
        _state.update { it.copy(isMenuOpen = false) }
    }

    fun onDatabaseSyncClicked() {
        if (_state.value.isDatabaseSyncing) return

        viewModelScope.launch {
            refreshDatabaseVersion()
        }
    }

    fun onAppUpdateCheckClicked() {
        val currentState = _state.value
        if (!currentState.isAppUpdateSupported || currentState.isAppUpdateChecking) return

        viewModelScope.launch {
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

        viewModelScope.launch {
            _state.update {
                it.copy(
                    logoutState = LCEState.Loading(),
                    logoutError = null,
                )
            }

            when (withContext(dispatchers.io) { authStateRepository.logout() }) {
                is Response.Success -> {
                    logger.d { "Logout succeeded" }
                    initialRefreshStarted = false
                    _state.update {
                        it.copy(
                            logoutState = LCEState.None(),
                            profileState = LCEState.None(),
                            databaseSyncState = LCEState.None(),
                            appFeaturesState = LCEState.None(),
                            userCommercialState = LCEState.None(),
                            appUpdateState = LCEState.None(),
                            chatAvailabilityState = LCEState.None(),
                            chatUnreadState = LCEState.None(),
                        )
                    }
                }
                is Response.Error -> {
                    logger.e { "Logout failed" }
                    _state.update {
                        it.copy(
                            logoutState = LCEState.None(),
                            logoutError = MainLogoutError.RequestFailed,
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadUserProfile() {
        _state.update { it.copy(profileState = LCEState.Loading()) }

        val response = withContext(dispatchers.io) { mainRepository.currentUserProfile() }
        _state.update {
            it.copy(
                profileState = response.asLCEState { e ->
                    logger.e(e) { "Failed to load user profile" }
                    e.message ?: "Error"
                }.mapContent { profile ->
                    MainProfile(name = profile.name, email = profile.email)
                },
            )
        }
    }

    private suspend fun loadUserAppFeatures() {
        _state.update { it.copy(appFeaturesState = LCEState.Loading()) }

        val response = withContext(dispatchers.io) { mainRepository.currentUserAppFeatures() }
        _state.update {
            it.copy(
                appFeaturesState = response.asLCEState { e ->
                    logger.e(e) { "Failed to load user app features" }
                    e.message ?: "Error"
                },
            )
        }
    }

    private suspend fun loadUserCommercial() {
        _state.update { it.copy(userCommercialState = LCEState.Loading()) }

        val response = withContext(dispatchers.io) { mainRepository.currentUserCommercial() }
        _state.update {
            it.copy(
                userCommercialState = response.asLCEState { e ->
                    logger.e(e) { "Failed to load user commercial" }
                    e.message ?: "Error"
                },
            )
        }
    }

    private suspend fun refreshDatabaseVersion() {
        _state.update { it.copy(databaseSyncState = LCEState.Loading()) }

        val response = withContext(dispatchers.io) { mainRepository.synchronizeSettingsVersion() }
        _state.update {
            it.copy(
                databaseSyncState = response.asLCEState { e ->
                    logger.e(e) { "Failed to synchronize database version" }
                    e.message ?: "Error"
                },
            )
        }

        if (response is Response.Error) {
            logger.d { "Falling back to current database version" }
            loadCurrentDatabaseVersion()
        }
    }

    private suspend fun loadCurrentDatabaseVersion() {
        val response = withContext(dispatchers.io) { mainRepository.currentSettingsVersion() }
        _state.update {
            it.copy(
                databaseSyncState = response.asLCEState { e ->
                    logger.e(e) { "Failed to load current database version" }
                    e.message ?: "Error"
                },
            )
        }
    }

    private suspend fun checkAppUpdate() {
        if (!_state.value.isAppUpdateSupported) return

        _state.update { it.copy(appUpdateState = LCEState.Loading()) }
        logger.d { "Checking for app update" }

        val response = withContext(dispatchers.io) { mainRepository.checkAppUpdate() }
        _state.update {
            it.copy(
                appUpdateState = response.asLCEState { e ->
                    logger.e(e) { "Failed to check app update" }
                    e.message ?: "Error"
                },
            )
        }
    }

    private fun startChatPolling() {
        if (chatAvailabilityJob != null) return

        chatAvailabilityJob = viewModelScope.launch {
            while (isActive) {
                loadChatAvailability()
                delay(CHAT_AVAILABILITY_DELAY_MS)
            }
        }

        chatUnreadJob = viewModelScope.launch {
            while (isActive) {
                loadChatUnreadCount()
                delay(CHAT_UNREAD_DELAY_MS)
            }
        }
    }

    private suspend fun loadChatAvailability() {
        val response = withContext(dispatchers.io) { mainRepository.chatAvailability() }
        _state.update {
            it.copy(
                chatAvailabilityState = response.asLCEState { e ->
                    logger.e(e) { "Failed to load chat availability" }
                    e.message ?: "Error"
                },
            )
        }
    }

    private suspend fun loadChatUnreadCount() {
        val response = withContext(dispatchers.io) { mainRepository.unreadMessagesCount() }
        _state.update {
            it.copy(
                chatUnreadState = response.asLCEState { e ->
                    logger.e(e) { "Failed to load chat unread count" }
                    e.message ?: "Error"
                },
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
                    logoutError = null,
                )
            }
        }
    }

    override fun onCleared() {
        chatAvailabilityJob?.cancel()
        chatUnreadJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val CHAT_AVAILABILITY_DELAY_MS = 10_000L
        private const val CHAT_UNREAD_DELAY_MS = 60_000L
    }
}
