package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.presentation.model.MAIN_UNKNOWN_APP_VERSION
import ru.alarmtrade.specmp.feature.main.presentation.model.MainDatabaseSyncBadge
import ru.alarmtrade.specmp.feature.main.presentation.model.MainError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile

@Immutable
data class MainScreenState(
    val isInitialLoading: Boolean = true,
    val isMenuOpen: Boolean = false,
    val isAppUpdateSupported: Boolean = false,
    val appVersionName: String = MAIN_UNKNOWN_APP_VERSION,
    val profileState: LCEState<MainProfile, MainError> = LCEState.None,
    val databaseSyncState: LCEState<MainDatabaseSyncStatus, MainError> = LCEState.None,
    val databaseVersion: Long? = null,
    val appFeaturesState: LCEState<MainUserAppFeatures, MainError> = LCEState.None,
    val userCommercialState: LCEState<MainUserCommercial, MainError> = LCEState.None,
    val appUpdateState: LCEState<MainAppUpdateCheckResult, MainError> = LCEState.None,
    val chatAvailabilityState: LCEState<Boolean, MainError> = LCEState.None,
    val logoutState: LCEState<Unit, MainError> = LCEState.None
) {
    val logoutError: MainError? get() = (logoutState as? LCEState.Error)?.error

    val profile: MainProfile
        get() = profileState.content ?: MainProfile()

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
                MainDatabaseSyncStatus.Finished.NoUpdates
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

    val chatAvailability: Boolean
        get() = chatAvailabilityState.content ?: false

    val isAppUpdateChecking: Boolean
        get() = appUpdateState is LCEState.Loading

    val appUpdateCheckResult: MainAppUpdateCheckResult?
        get() = appUpdateState.content

    val isLogoutInProgress: Boolean
        get() = logoutState is LCEState.Loading
}
