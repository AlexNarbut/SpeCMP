package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.main.domain.model.MainAppUpdateCheckResult
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserAppFeatures
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserCommercial
import ru.alarmtrade.specmp.feature.main.presentation.model.MainCardVariant
import ru.alarmtrade.specmp.feature.main.presentation.model.MainLogoutError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile
import ru.alarmtrade.specmp.feature.main.presentation.model.MainUnknownAppVersion

@Immutable
data class MainScreenState(
    val isInitialLoading: Boolean = true,
    val isMenuOpen: Boolean = false,
    val isAppUpdateSupported: Boolean = false,
    val appVersionName: String = MainUnknownAppVersion,
    val profileState: LCEState<MainProfile> = LCEState.None(),
    val databaseSyncState: LCEState<Long?> = LCEState.None(),
    val appFeaturesState: LCEState<MainUserAppFeatures> = LCEState.None(),
    val userCommercialState: LCEState<MainUserCommercial> = LCEState.None(),
    val appUpdateState: LCEState<MainAppUpdateCheckResult> = LCEState.None(),
    val chatAvailabilityState: LCEState<Boolean> = LCEState.None(),
    val chatUnreadState: LCEState<Int> = LCEState.None(),
    val cardVariant: MainCardVariant = MainCardVariant.Centered,
    val logoutState: LCEState<Unit> = LCEState.None(),
    val logoutError: MainLogoutError? = null,
) {
    val profile: MainProfile
        get() = profileState.content ?: MainProfile()

    val databaseVersion: Long?
        get() = databaseSyncState.content

    val isDatabaseSyncing: Boolean
        get() = databaseSyncState is LCEState.Loading

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
