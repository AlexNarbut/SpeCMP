package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState

@Immutable
data class MainScreenState(
    val logoutState: LCEState<Unit> = LCEState.None(),
) {
    val isLogoutInProgress: Boolean
        get() = logoutState is LCEState.Loading || logoutState is LCEState.Content
    val isLogoutFailed: Boolean get() = logoutState is LCEState.Error
}
