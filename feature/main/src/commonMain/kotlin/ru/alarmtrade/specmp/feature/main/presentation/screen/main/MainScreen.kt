package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents.MainPlaceholderBody

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainScreenViewModel>()
    val state = viewModel.state.collectAsState()

    MainContent(
        state = state,
        onLogout = viewModel::logout,
        modifier = modifier,
    )
}

@Composable
private fun MainContent(
    state: State<MainScreenState>,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MainPlaceholderBody(
        state = state.value,
        onLogout = onLogout,
        modifier = modifier,
    )
}
