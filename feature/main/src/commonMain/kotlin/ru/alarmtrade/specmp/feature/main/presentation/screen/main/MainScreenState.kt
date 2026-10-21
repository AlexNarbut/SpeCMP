package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainDatabaseSyncStatus
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.presentation.model.MAIN_UNKNOWN_APP_VERSION
import ru.alarmtrade.specmp.feature.main.presentation.model.MainCardVariant
import ru.alarmtrade.specmp.feature.main.presentation.model.MainDatabaseSyncBadge
import ru.alarmtrade.specmp.feature.main.presentation.model.MainLogoutError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile

@Immutable
data class MainScreenState(
    val isInitialLoading: Boolean = true,
    val isMenuOpen: Boolean = false,
    val isAppUpdateSupported: Boolean = false,
    val appVersionName: String = MAIN_UNKNOWN_APP_VERSION,
    val profileState: LCEState<MainProfile> = LCEState.None(),
    val databaseSyncState: LCEState<MainDatabaseSyncStatus> = LCEState.None(),
    val databaseVersion: Long? = null,
    val appFeaturesState: LCEState<MainUserAppFeatures> = LCEState.None(),
    val userCommercialState: LCEState<MainUserCommercial> = LCEState.None(),
    val appUpdateState: LCEState<MainAppUpdateCheckResult> = LCEState.None(),
    val chatAvailabilityState: LCEState<Boolean> = LCEState.None(),
    val chatUnreadState: LCEState<Int> = LCEState.None(),
    val cardVariant: MainCardVariant = MainCardVariant.Centered,
    val logoutState: LCEState<Unit> = LCEState.None(),
    val logoutError: MainLogoutError? = null
) {
    val profile: MainProfile
        get() = profileState.content ?: MainProfile()

    val isDatabaseSyncing: Boolean
        get() = databaseSyncState is LCEState.Loading ||
            databaseSyncState.content is MainDatabaseSyncStatus.InProgress

    val databaseSyncBadge: MainDatabaseSyncBadge
        get() {
            if (databaseSyncState is LCEState.Error) return MainDatabaseSyncBadge.UpdateFailed

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

    val chatUnreadCount: Int
        get() = chatUnreadState.content ?: 0

    val isAppUpdateChecking: Boolean
        get() = appUpdateState is LCEState.Loading

    val appUpdateCheckResult: MainAppUpdateCheckResult?
        get() = appUpdateState.content

    val isLogoutInProgress: Boolean
        get() = logoutState is LCEState.Loading
}
