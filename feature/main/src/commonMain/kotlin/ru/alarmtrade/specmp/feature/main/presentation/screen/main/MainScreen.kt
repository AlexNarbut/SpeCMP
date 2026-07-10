package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents.MainHomeBody

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onNavigateToFeature: (MainNavigationTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onScreenEntered()
    }

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            is MainScreenEffect.Navigate -> onNavigateToFeature(effect.target)
        }
    }

    MainHomeBody(
        state = state,
        onMenuClick = viewModel::onMenuClicked,
        onMenuDismiss = viewModel::onMenuDismissed,
        onDatabaseSyncClick = viewModel::onDatabaseSyncClicked,
        onAppUpdateCheckClick = viewModel::onAppUpdateCheckClicked,
        onAppUpdateOpenClick = viewModel::onAppUpdateBannerClicked,
        onHomeActionClick = viewModel::onHomeActionClicked,
        onSettingsClick = viewModel::onSettingsClicked,
        isDarkTheme = isDarkTheme,
        onThemeChanged = onThemeChanged,
        onLogout = viewModel::logout,
        modifier = modifier,
    )
}
