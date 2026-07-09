package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents.MainHomeBody

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val state = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.onScreenEntered()
    }

    MainHomeBody(
        state = state,
        onMenuClick = viewModel::onMenuClicked,
        onMenuDismiss = viewModel::onMenuDismissed,
        onDatabaseSyncClick = viewModel::onDatabaseSyncClicked,
        onAppUpdateCheckClick = viewModel::onAppUpdateCheckClicked,
        onAppUpdateOpenClick = viewModel::onAppUpdateBannerClicked,
        onHomeActionClick = viewModel::onHomeActionClicked,
        isDarkTheme = isDarkTheme,
        onThemeChanged = onThemeChanged,
        onLogout = viewModel::logout,
        modifier = modifier,
    )
}
