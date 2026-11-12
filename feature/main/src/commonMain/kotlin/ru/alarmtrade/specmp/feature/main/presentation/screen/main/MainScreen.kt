package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents.MainHomeBody

@Composable
internal fun MainScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToFeature: (MainNavigationTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()

    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is MainScreenEffect.Navigate -> onNavigateToFeature(effect.target)
        }
    }

    MainHomeBody(
        state = state,
        onMenuClick = viewModel::onMenuClicked,
        onMenuDismiss = viewModel::onMenuDismissed,
        onUserDataRefreshClick = viewModel::onUserDataRefreshClicked,
        onDatabaseSyncClick = viewModel::onDatabaseSyncClicked,
        onAppUpdateCheckClick = viewModel::onAppUpdateCheckClicked,
        onAppUpdateOpenClick = viewModel::onAppUpdateBannerClicked,
        onHomeActionClick = viewModel::onHomeActionClicked,
        onSettingsClick = viewModel::onSettingsClicked,
        isDarkTheme = isDarkTheme,
        onThemeChange = onThemeChange,
        onLogout = viewModel::onLogout,
        modifier = modifier,
    )
}
