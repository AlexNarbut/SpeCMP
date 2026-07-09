package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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
import ru.alarmtrade.specmp.feature.main.presentation.model.MainAppUpdateState
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainLogoutError
import ru.alarmtrade.specmp.feature.main.presentation.platform.isMainAppUpdateSupported

class MainScreenViewModel(
    private val authStateRepository: AuthStateRepository,
    private val dispatchers: AppDispatchers,
) : ViewModel() {

    private val isAppUpdateSupported = isMainAppUpdateSupported()
    private val _state = MutableStateFlow(
        MainScreenState(
            isAppUpdateSupported = isAppUpdateSupported,
            appUpdateState = if (isAppUpdateSupported) {
                MainAppUpdateState.Available
            } else {
                MainAppUpdateState.Idle
            },
        ),
    )
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private var initialLoadStarted = false

    fun onScreenEntered() {
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

        if (initialLoadStarted) return
        initialLoadStarted = true

        viewModelScope.launch {
            delay(INITIAL_LOAD_MS)
            _state.update { it.copy(isInitialLoading = false) }
        }
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
            _state.update { it.copy(isDatabaseSyncing = true) }
            delay(DATABASE_SYNC_MS)
            _state.update { it.copy(isDatabaseSyncing = false) }
        }
    }

    fun onAppUpdateCheckClicked() {
        val currentState = _state.value
        if (!currentState.isAppUpdateSupported || currentState.isAppUpdateChecking) return

        viewModelScope.launch {
            _state.update { it.copy(appUpdateState = MainAppUpdateState.Checking) }
            delay(APP_UPDATE_CHECK_MS)
            _state.update { it.copy(appUpdateState = MainAppUpdateState.Available) }
        }
    }

    fun onAppUpdateBannerClicked() {
        _state.update { it.copy(isMenuOpen = false) }
    }

    fun onHomeActionClicked(action: MainHomeAction) {
        if (action == MainHomeAction.Settings) {
            _state.update { it.copy(isMenuOpen = false) }
        }
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
                is Response.Success -> _state.update {
                    it.copy(logoutState = LCEState.None())
                }
                is Response.Error -> _state.update {
                    it.copy(
                        logoutState = LCEState.None(),
                        logoutError = MainLogoutError.RequestFailed,
                    )
                }
            }
        }
    }

    private companion object {
        const val INITIAL_LOAD_MS = 1_500L
        const val DATABASE_SYNC_MS = 1_700L
        const val APP_UPDATE_CHECK_MS = 1_400L
    }
}
