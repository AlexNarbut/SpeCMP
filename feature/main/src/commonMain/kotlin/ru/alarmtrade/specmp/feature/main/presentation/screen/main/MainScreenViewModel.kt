package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
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
) : BaseViewModel<MainScreenState, MainScreenEffect>(
    MainScreenState(
        isAppUpdateSupported = isMainAppUpdateSupported(),
        appVersionName = mainRepository.appVersion.name,
    ),
) {

    override suspend fun onStart() {
        // Поллинг чата живёт, пока экран подписан на state: при возврате перезапускается с None.
        mainRepository.observeChatAvailability()
            .map { response -> response.asLCEState(MainError::from) }
            .flowOn(dispatchers.io)
            .onStart { emit(LCEState.None) }
            .collectIntoState { chatAvailability -> copy(chatAvailabilityState = chatAvailability).withActions() }
        safeLaunch { repeatOnSubscription { resetLogoutTransientState() } }
        initialRefresh()
    }

    fun onMenuClicked() {
        if (currentState.isInitialLoading) return

        val profile = currentState.profileState
        if (profile !is LCEState.Content && profile !is LCEState.Loading) {
            intent { loadUserProfile() }
        }

        reduce { copy(isMenuOpen = !isMenuOpen) }
    }

    fun onMenuDismissed() {
        reduce { copy(isMenuOpen = false) }
    }

    fun onDatabaseSyncClicked() {
        if (currentState.isDatabaseSyncing) return

        intent { refreshDatabaseVersion() }
    }

    fun onAppUpdateCheckClicked() {
        if (!currentState.isAppUpdateSupported || currentState.isAppUpdateChecking) return

        intent { checkAppUpdate() }
    }

    fun onAppUpdateBannerClicked() {
        reduce { copy(isMenuOpen = false) }
        postSideEffect(MainScreenEffect.Navigate(MainNavigationTarget.AppUpdate))
    }

    fun onHomeActionClicked(action: MainHomeAction) {
        postSideEffect(MainScreenEffect.Navigate(action.target))
    }

    fun onSettingsClicked() {
        reduce { copy(isMenuOpen = false) }
        postSideEffect(MainScreenEffect.Navigate(MainNavigationTarget.Settings))
    }

    fun onLogout() {
        if (currentState.isLogoutInProgress) return

        intent {
            reduce { copy(logoutState = LCEState.Loading) }

            when (val response = withContext(dispatchers.io) { logoutUseCase.execute() }) {
                // Остаёмся в Loading: кнопка заблокирована, пока auth-state не сменит зону; эта VM уйдёт вместе с
                // авторизованной зоной (ViewModelScope по сессии) — state сбрасывать не нужно.
                is Response.Success -> logger.d { "Logout succeeded" }

                is Response.Error -> {
                    logger.d { "Logout failed" }
                    reduce { copy(logoutState = LCEState.Error(MainError.from(response.exception))) }
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
            // Сбой (необработанное исключение логирует база) скелетон тоже убирает.
            reduce { copy(isInitialLoading = false) }
        }
    }

    private suspend fun loadUserProfile() {
        reduce { copy(profileState = LCEState.Loading) }

        val response = loadWithTimeout { mainRepository.currentUserProfile() }
        reduce {
            copy(
                profileState = response.asLCEState(MainError::from)
                    .mapContent { profile -> MainProfile(name = profile.name, email = profile.email) },
            )
        }
    }

    private suspend fun loadUserAppFeatures() {
        reduce { copy(appFeaturesState = LCEState.Loading).withActions() }

        val response = loadWithTimeout { mainRepository.currentUserAppFeatures() }
        reduce { copy(appFeaturesState = response.asLCEState(MainError::from)).withActions() }
    }

    private suspend fun loadUserCommercial() {
        reduce { copy(userCommercialState = LCEState.Loading).withActions() }

        val response = loadWithTimeout { mainRepository.currentUserCommercial() }
        reduce { copy(userCommercialState = response.asLCEState(MainError::from)).withActions() }
    }

    // Синхронизация — процесс: стадии зеркалятся в state по мере эмита,
    // поток всегда завершается одним из MainDatabaseSyncStatus.Finished.
    private suspend fun refreshDatabaseVersion() {
        reduce { copy(databaseSyncState = LCEState.Loading) }

        val lastStatus = mainRepository.synchronizeSettingsVersion()
            .flowOn(dispatchers.io)
            .onEach { response -> reduce { copy(databaseSyncState = response.asLCEState(MainError::from)) } }
            .lastOrNull()
            ?.getValueIfSuccess()

        if (lastStatus is MainDatabaseSyncStatus.Finished.Success) {
            reduce { copy(databaseVersion = lastStatus.newVersion) }
        } else {
            logger.d { "Falling back to current database version" }
            loadCurrentDatabaseVersion()
        }
    }

    private suspend fun loadCurrentDatabaseVersion() {
        val response = withContext(dispatchers.io) { mainRepository.currentSettingsVersion() }
        reduce { copy(databaseVersion = response.getValueIfSuccess() ?: databaseVersion) }
    }

    private suspend fun checkAppUpdate() {
        if (!currentState.isAppUpdateSupported) return

        reduce { copy(appUpdateState = LCEState.Loading) }
        logger.d { "Checking for app update" }

        val response = loadWithTimeout { mainRepository.checkAppUpdate() }
        reduce { copy(appUpdateState = response.asLCEState(MainError::from)) }
    }

    /** Ошибка выхода — разовая: при возврате на экран не показываем. Loading (выход идёт или удался) не трогаем. */
    private fun resetLogoutTransientState() {
        reduce { if (logoutState is LCEState.Error) copy(logoutState = LCEState.None) else this }
    }

    private suspend fun <T> loadWithTimeout(block: suspend () -> Response<T>): Response<T> =
        withContext(dispatchers.io) {
            withTimeoutOrNull(STARTUP_LOAD_TIMEOUT) { block() }
        } ?: Response.Error(Exception("Request timed out"))

    private companion object {
        val STARTUP_LOAD_TIMEOUT = 10.seconds
    }
}
