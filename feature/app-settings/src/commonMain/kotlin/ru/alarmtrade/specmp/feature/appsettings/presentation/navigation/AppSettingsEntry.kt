package ru.alarmtrade.specmp.feature.appsettings.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings.AppSettingsActions
import ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings.AppSettingsScreen
import ru.alarmtrade.specmp.feature.appsettings.presentation.screen.settings.AppSettingsViewModel

@Composable
fun AppSettingsEntry(onBack: () -> Unit) {
    val viewModel = koinViewModel<AppSettingsViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) {
        AppSettingsActions(
            onBack = onBack,
            onLocaleSelect = viewModel::onLocaleSelect,
            onApply = viewModel::onApply,
            onConfirm = viewModel::onConfirm,
            onDialogDismiss = viewModel::onDialogDismiss,
            onConfigurationLanguageSelect = viewModel::onConfigurationLanguageSelect,
        )
    }
    AppSettingsScreen(state, actions)
}
