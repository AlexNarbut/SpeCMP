package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.files.ImagePicker
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.feature.main.presentation.model.MainNavigationTarget
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents.MainHomeBody

@Composable
internal fun MainScreen(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onNavigateToFeature: (MainNavigationTarget) -> Unit,
    imagePicker: ImagePicker,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MainScreenViewModel> { parametersOf(imagePicker) }
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
        appUpdateActions = remember(viewModel) {
            MainAppUpdateActions(
                onOpenFromMenu = viewModel::onAppUpdateClicked,
                onOpenPrompt = viewModel::onAppUpdatePromptClicked,
                onInstall = viewModel::onAppUpdateInstallClicked,
                onDismissBanner = viewModel::onUpdateBannerDismissed,
                onToastAction = viewModel::onUpdateToastAction,
                onToastDismiss = viewModel::onUpdateToastDismissed,
            )
        },
        avatarActions = remember(viewModel) {
            MainAvatarActions(
                onAvatarClick = viewModel::onAvatarClicked,
                onSourceSelect = viewModel::onAvatarSourceSelected,
                onSourceDismiss = viewModel::onAvatarSourceDismissed,
                onErrorDismiss = viewModel::onAvatarErrorDismissed,
                onCropConfirm = viewModel::onAvatarCropConfirmed,
                onCropCancel = viewModel::onAvatarCropCancelled,
            )
        },
        onHomeActionClick = viewModel::onHomeActionClicked,
        onSettingsClick = viewModel::onSettingsClicked,
        onAppearanceClick = viewModel::onAppearanceClicked,
        isDarkTheme = isDarkTheme,
        onThemeChange = onThemeChange,
        onLogout = viewModel::onLogout,
        modifier = modifier,
    )
}
