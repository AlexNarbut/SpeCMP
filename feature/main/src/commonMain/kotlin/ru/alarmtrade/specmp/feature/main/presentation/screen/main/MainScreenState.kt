package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.main.presentation.model.MainAppUpdateState
import ru.alarmtrade.specmp.feature.main.presentation.model.MainCardVariant
import ru.alarmtrade.specmp.feature.main.presentation.model.MainLogoutError
import ru.alarmtrade.specmp.feature.main.presentation.model.MainProfile

@Immutable
data class MainScreenState(
    val isInitialLoading: Boolean = true,
    val isMenuOpen: Boolean = false,
    val isDatabaseSyncing: Boolean = false,
    val isAppUpdateSupported: Boolean = false,
    val appUpdateState: MainAppUpdateState = MainAppUpdateState.Idle,
    val cardVariant: MainCardVariant = MainCardVariant.Centered,
    val profile: MainProfile = MainProfile(email = "installer@pandora.pro"),
    val logoutState: LCEState<Unit> = LCEState.None(),
    val logoutError: MainLogoutError? = null,
) {
    val isLogoutInProgress: Boolean
        get() = logoutState is LCEState.Loading

    val isAppUpdateChecking: Boolean
        get() = appUpdateState == MainAppUpdateState.Checking
}
