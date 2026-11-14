package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.AppVersionProvider
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.refreshedWith
import ru.alarmtrade.specmp.core.ui.lce.refreshing
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile
import ru.alarmtrade.specmp.feature.main.domain.repository.MainAppUpdateRepository
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.model.MainError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile

class MainScreenViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val mainRepository: MainRepository,
    private val appUpdateRepository: MainAppUpdateRepository,
    appVersionProvider: AppVersionProvider,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<MainScreenState, MainScreenEffect>(
    MainScreenState(appVersionName = appVersionProvider.current().name),
) {

    override suspend fun onStart() {
        // Поллинг чата живёт, пока экран подписан на state: при возврате перезапускается с None.
        mainRepository.observeChatAvailability()
            .map { response -> response.asLCEState(MainError::from) }
            .flowOn(dispatchers.io)
            .onStart { emit(LCEState.None) }
            .collectIntoState { chatAvailability ->
                // Ошибка опроса (нет сети) не меняет известный ответ сервера.
                val keepKnown = chatAvailability is LCEState.Error && chatAvailabilityState is LCEState.Content
                copy(chatAvailabilityState = if (keepKnown) chatAvailabilityState else chatAvailability).withActions()
            }
        safeLaunch { repeatOnSubscription { resetLogoutTransientState() } }
        appUpdateRepository.status().collectIntoState(onError = { this }) { status -> withAppUpdate(status) }
        safeLaunch {
            select { updateToastVersion }.collectLatest { version ->
                if (version != null) {
                    delay(UPDATE_TOAST_DURATION)
                    reduce { copy(updateToastVersion = null) }
                }
            }
        }
        applyCachedUserData()
        initialRefresh()
    }

    fun onMenuClicked() {
        if (currentState.isInitialLoading) return

        if (!currentState.isMenuOpen) refreshUserData()
        reduce { copy(isMenuOpen = !isMenuOpen) }
    }

    fun onUserDataRefreshClicked() {
        refreshUserData()
    }

    fun onMenuDismissed() {
        reduce { copy(isMenuOpen = false) }
    }

    fun onDatabaseSyncClicked() {
        if (currentState.isDatabaseSyncing) return

        intent { refreshDatabaseVersion() }
    }

    fun onAppUpdateClicked() {
        openAppUpdate(MainNavigationTarget.AppUpdate)
    }

    fun onAppUpdatePromptClicked() {
        openAppUpdate(MainNavigationTarget.AppUpdatePrompt)
    }

    fun onAppUpdateInstallClicked() {
        openAppUpdate(MainNavigationTarget.AppUpdateInstall)
    }

    fun onUpdateBannerDismissed() {
        val version = currentState.availableUpdateVersion ?: return
        intent {
            reduce { copy(dismissedUpdateBanner = version) }
            withContext(dispatchers.io) { appUpdateRepository.dismissBanner(version) }
                .onError { logger.w(it) { "Update banner dismissal is not saved" } }
        }
    }

    /** «Обновить» в уведомлении при старте: загрузка и экран обновления. */
    fun onUpdateToastAction() {
        intent {
            reduce { copy(updateToastVersion = null) }
            withContext(dispatchers.io) { appUpdateRepository.download() }
                .onError { logger.w(it) { "Update download is not started" } }
            openAppUpdate(MainNavigationTarget.AppUpdatePrompt)
        }
    }

    fun onUpdateToastDismissed() {
        reduce { copy(updateToastVersion = null) }
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

            when (val response = withContext(dispatchers.io) { logoutUseCase() }) {
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
        safeLaunch { loadDismissedUpdateBanner() }
        try {
            loadUserData()
            logger.d { "Initial home data refresh completed" }
        } finally {
            // Сбой (необработанное исключение логирует база) скелетон тоже убирает.
            reduce { copy(isInitialLoading = false) }
        }
    }

    private suspend fun applyCachedUserData() {
        val profile = withContext(dispatchers.io) { mainRepository.cachedUserProfile().getValueIfSuccess() }
        val features = withContext(dispatchers.io) { mainRepository.cachedUserAppFeatures().getValueIfSuccess() }
        val commercial = withContext(dispatchers.io) { mainRepository.cachedUserCommercial().getValueIfSuccess() }
        reduce {
            copy(
                isInitialLoading = profile == null,
                profileState = profile?.let { LCEState.Content(it.toMainProfile()) } ?: profileState,
                appFeaturesState = features?.let { LCEState.Content(it) } ?: appFeaturesState,
                userCommercialState = commercial?.let { LCEState.Content(it) } ?: userCommercialState,
            ).withActions()
        }
    }

    private fun refreshUserData() {
        if (currentState.isUserDataRefreshing) return

        intent { loadUserData() }
    }

    private suspend fun loadUserData() = coroutineScope {
        awaitAll(
            async { loadUserProfile() },
            async { loadUserAppFeatures() },
            async { loadUserCommercial() },
        )
    }

    private suspend fun loadUserProfile() {
        reduce { copy(profileState = profileState.refreshing()) }

        val response = loadWithTimeout { mainRepository.currentUserProfile() }.map { it.toMainProfile() }
        reduce { copy(profileState = profileState.refreshedWith(response, MainError::from)) }
    }

    private suspend fun loadUserAppFeatures() {
        reduce { copy(appFeaturesState = appFeaturesState.refreshing()).withActions() }

        val response = loadWithTimeout { mainRepository.currentUserAppFeatures() }
        reduce { copy(appFeaturesState = appFeaturesState.refreshedWith(response, MainError::from)).withActions() }
    }

    private suspend fun loadUserCommercial() {
        reduce { copy(userCommercialState = userCommercialState.refreshing()).withActions() }

        val response = loadWithTimeout { mainRepository.currentUserCommercial() }
        reduce {
            copy(userCommercialState = userCommercialState.refreshedWith(response, MainError::from)).withActions()
        }
    }

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
        logger.d { "Checking for app update" }
        withContext(dispatchers.io) { appUpdateRepository.check() }
            .onError { logger.w(it) { "App update check is not started" } }
    }

    private suspend fun loadDismissedUpdateBanner() {
        val dismissed = withContext(dispatchers.io) { appUpdateRepository.dismissedBannerVersion() }
        reduce { copy(dismissedUpdateBanner = dismissed.getValueIfSuccess()) }
    }

    private fun openAppUpdate(target: MainNavigationTarget) {
        reduce { copy(isMenuOpen = false) }
        postSideEffect(MainScreenEffect.Navigate(target))
    }

    /** Desktop: при первой найденной версии — уведомление, само скрывается. */
    private fun MainScreenState.withAppUpdate(status: MainAppUpdateStatus): MainScreenState {
        val version = (status.update as? MainAppUpdate.Available)?.version
        val showToast = status.installsItself && version != null && !isUpdateToastShown
        return copy(
            appUpdate = status,
            updateToastVersion = if (showToast) version else updateToastVersion,
            isUpdateToastShown = isUpdateToastShown || showToast,
        )
    }

    /** Ошибка выхода — разовая: при возврате на экран не показываем. Loading (выход идёт или удался) не трогаем. */
    private fun resetLogoutTransientState() {
        reduce { if (logoutState is LCEState.Error) copy(logoutState = LCEState.None) else this }
    }

    private fun MainUserProfile.toMainProfile() = MainProfile(name = name, email = email)

    private suspend fun <T> loadWithTimeout(block: suspend () -> Response<T>): Response<T> =
        withContext(dispatchers.io) {
            withTimeoutOrNull(STARTUP_LOAD_TIMEOUT) { block() }
        } ?: Response.Error(Exception("Request timed out"))

    private companion object {
        val STARTUP_LOAD_TIMEOUT = 10.seconds
        val UPDATE_TOAST_DURATION = 8.seconds
    }
}
