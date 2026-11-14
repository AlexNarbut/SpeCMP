package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdate
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.presentation.model.MAIN_UNKNOWN_APP_VERSION
import ru.alarmtrade.specmp.feature.main.presentation.model.MainDatabaseSyncBadge
import ru.alarmtrade.specmp.feature.main.presentation.model.MainError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile

@Immutable
data class MainScreenState(
    val isInitialLoading: Boolean = true,
    val isMenuOpen: Boolean = false,
    val appVersionName: String = MAIN_UNKNOWN_APP_VERSION,
    val profileState: LCEState<MainProfile, MainError> = LCEState.None,
    val databaseSyncState: LCEState<MainDatabaseSyncStatus, MainError> = LCEState.None,
    val databaseVersion: Long? = null,
    val appFeaturesState: LCEState<MainUserAppFeatures, MainError> = LCEState.None,
    val userCommercialState: LCEState<MainUserCommercial, MainError> = LCEState.None,
    val appUpdate: MainAppUpdateStatus = MainAppUpdateStatus(installsItself = false, update = MainAppUpdate.None),
    /** Версия, плашку которой закрыли (телефон, планшет). */
    val dismissedUpdateBanner: String? = null,
    /** Уведомление при старте (desktop) — один раз за сессию главного. */
    val updateToastVersion: String? = null,
    val isUpdateToastShown: Boolean = false,
    val chatAvailabilityState: LCEState<Boolean, MainError> = LCEState.None,
    val visibleActions: List<MainHomeAction> = MainHomeAction.visibleActions(),
    val logoutState: LCEState<Unit, MainError> = LCEState.None,
) {
    val logoutError: MainError? get() = (logoutState as? LCEState.Error)?.error

    val profile: MainProfile
        get() = profileState.content ?: MainProfile()

    val isUserDataRefreshing: Boolean
        get() = listOf(profileState, appFeaturesState, userCommercialState).any { it.isRefreshing }

    val userDataError: MainError?
        get() = listOf(profileState, appFeaturesState, userCommercialState).firstNotNullOfOrNull { it.failure }

    val isDatabaseSyncing: Boolean
        get() = databaseSyncState is LCEState.Loading ||
            databaseSyncState.content is MainDatabaseSyncStatus.InProgress

    val databaseSyncBadge: MainDatabaseSyncBadge
        get() {
            if (databaseSyncState is LCEState.Error) {
                return MainDatabaseSyncBadge.UpdateFailed((databaseSyncState.error as? MainError.Common)?.error)
            }

            return when (databaseSyncState.content) {
                MainDatabaseSyncStatus.InProgress.CheckingVersion -> MainDatabaseSyncBadge.CheckingVersion

                MainDatabaseSyncStatus.InProgress.Downloading -> MainDatabaseSyncBadge.Downloading

                MainDatabaseSyncStatus.InProgress.Applying -> MainDatabaseSyncBadge.Applying

                is MainDatabaseSyncStatus.Finished.NotCompatible -> MainDatabaseSyncBadge.AppUpdateRequired

                is MainDatabaseSyncStatus.Finished.Success,
                MainDatabaseSyncStatus.Finished.NoUpdates,
                -> MainDatabaseSyncBadge.Updated

                null -> if (databaseSyncState is LCEState.Loading) {
                    MainDatabaseSyncBadge.CheckingVersion
                } else {
                    MainDatabaseSyncBadge.Updated
                }
            }
        }

    val appFeatures: MainUserAppFeatures
        get() = appFeaturesState.content ?: MainUserAppFeatures()

    val userCommercial: MainUserCommercial
        get() = userCommercialState.content ?: MainUserCommercial()

    /** Плитку чата прячет только явный ответ сервера «недоступен»; пока ответа нет или нет сети — она видна. */
    val chatAvailability: Boolean
        get() = chatAvailabilityState.content ?: true

    val isAppUpdateChecking: Boolean
        get() = appUpdate.update == MainAppUpdate.Checking

    val availableUpdateVersion: String?
        get() = (appUpdate.update as? MainAppUpdate.Available)?.version

    val showUpdateBanner: Boolean
        get() = !appUpdate.installsItself && availableUpdateVersion?.let { it != dismissedUpdateBanner } == true

    val isLogoutInProgress: Boolean
        get() = logoutState is LCEState.Loading
}

private val LCEState<*, MainError>.isRefreshing: Boolean
    get() = this is LCEState.Loading || (this as? LCEState.Content)?.isUpdating == true

private val LCEState<*, MainError>.failure: MainError?
    get() = (this as? LCEState.Error)?.error ?: (this as? LCEState.Content)?.updateError

internal fun MainScreenState.withActions(): MainScreenState = copy(
    visibleActions = MainHomeAction.visibleActions(
        features = appFeatures,
        commercial = userCommercial,
        showChat = chatAvailability,
    ),
)
