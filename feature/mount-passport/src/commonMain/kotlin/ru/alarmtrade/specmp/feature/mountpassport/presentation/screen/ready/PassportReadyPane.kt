package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_configuration
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_settings_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_configuration_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_settings_file_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_no_app
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_open
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_refresh
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_save
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_share
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_msg_saved
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts

/**
 * Документ паспорта со своей VM (ключ — паспорт): [preview] — живой лист справа от предотчёта в широком окне, иначе
 * экран «Паспорт готов» ([onEdit] — назад в предотчёт).
 */
@Composable
internal fun PassportReadyPane(
    passportId: String,
    ports: MountPassportPorts,
    preview: Boolean,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    share: Boolean = false,
) {
    val viewModel = koinViewModel<PassportReadyViewModel>(key = "mount-passport-ready-$passportId") {
        parametersOf(
            PassportReadyArgs(passportId, shareOnReady = share),
            ports.pdfRenderer,
            ports.pageRasterizer,
            ports.fileExporter,
            ports.settingsFileSaver,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val currentEdit by rememberUpdatedState(onEdit)
    viewModel.sideEffect.collectSideEffect { effect ->
        if (effect == PassportReadyEffect.Edit) currentEdit() else scope.launch { snackbar.show(effect) }
    }
    val actions = remember(viewModel) {
        PassportReadyActions(
            onEdit = viewModel::onEdit,
            onRetry = viewModel::onRetry,
            onPageVisible = viewModel::onPageVisible,
            onShare = viewModel::onShare,
            onSave = viewModel::onSave,
            onOpen = viewModel::onOpen,
            onSaveSettingsFile = viewModel::onSaveSettingsFile,
            onSaveSettingsToConfigurations = viewModel::onSaveSettingsToConfigurations,
        )
    }
    if (preview) {
        PassportPreviewPane(state = state, actions = actions, snackbar = snackbar, modifier = modifier)
    } else {
        PassportReadyScreen(state = state, actions = actions, snackbar = snackbar, modifier = modifier)
    }
}

private suspend fun CmpSnackbarHostState.show(effect: PassportReadyEffect) {
    val (text, tone) = when (effect) {
        PassportReadyEffect.Edit -> return

        is PassportReadyEffect.Saved ->
            getString(Strings.string.mountpassport_ready_msg_saved, effect.name) to CmpSnackbarTone.Success

        PassportReadyEffect.SaveFailed -> getString(Strings.string.mountpassport_ready_error_save) to
            CmpSnackbarTone.Error

        PassportReadyEffect.ShareFailed ->
            getString(Strings.string.mountpassport_ready_error_share) to CmpSnackbarTone.Error

        PassportReadyEffect.NoPdfApp -> getString(Strings.string.mountpassport_ready_error_no_app) to
            CmpSnackbarTone.Error

        PassportReadyEffect.OpenFailed -> getString(Strings.string.mountpassport_ready_error_open) to
            CmpSnackbarTone.Error

        is PassportReadyEffect.RefreshFailed ->
            getString(Strings.string.mountpassport_ready_error_refresh) to CmpSnackbarTone.Error

        is PassportReadyEffect.SettingsFileSaved ->
            getString(Strings.string.mountpassport_editor_msg_settings_file_saved, effect.name) to
                CmpSnackbarTone.Success

        PassportReadyEffect.SettingsFileFailed ->
            getString(Strings.string.mountpassport_editor_error_settings_file) to CmpSnackbarTone.Error

        PassportReadyEffect.ConfigurationSaved ->
            getString(Strings.string.mountpassport_editor_msg_configuration_saved) to CmpSnackbarTone.Success

        is PassportReadyEffect.ConfigurationFailed ->
            getString(Strings.string.mountpassport_editor_error_configuration) to CmpSnackbarTone.Error
    }
    showSnackbar(text, tone = tone)
}
