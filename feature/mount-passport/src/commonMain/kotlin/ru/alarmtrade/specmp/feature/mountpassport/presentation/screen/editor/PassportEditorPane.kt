package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_configuration
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_settings_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_configuration_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_settings_file_saved
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.errorMessage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportCallbacks
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts

/**
 * Предотчёт со своей VM (ключ — паспорт: в широком окне списка их может быть несколько). Отдельным экраном «Назад»
 * дописывает несохранённое и уходит без подтверждения ([onClose]); встроенный ([embedded]) назад не перехватывает.
 */
@Composable
internal fun PassportEditorPane(
    passportId: String,
    callbacks: MountPassportCallbacks,
    ports: MountPassportPorts,
    embedded: Boolean,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<PassportEditorViewModel>(key = "mount-passport-editor-$passportId") {
        parametersOf(PassportEditorArgs(passportId), ports.imagePicker, ports.settingsFileSaver)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val currentClose by rememberUpdatedState(onClose)
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            PassportEditorEffect.Close -> currentClose()
            else -> scope.launch { snackbar.show(effect) }
        }
    }
    if (!embedded) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = viewModel::onBack,
        )
    }
    val actions = remember(viewModel, callbacks, passportId) {
        PassportEditorActions(
            onBack = viewModel::onBack,
            onRetry = viewModel::onRetry,
            onFieldChange = viewModel::onFieldChange,
            onWorkToggle = viewModel::onWorkToggle,
            onReread = { stage -> callbacks.onReread(passportId, stage) },
            onReadUnit = { callbacks.onReadUnit(passportId) },
            onCreatePdf = { callbacks.onCreatePdf(passportId) },
            photos = PassportPhotoActions(
                onAdd = viewModel::onAddPhoto,
                onSource = viewModel::onPhotoSource,
                onSheetDismiss = viewModel::onPhotoSheetDismiss,
                onOpenAppSettings = viewModel::onOpenAppSettings,
                onErrorDismiss = viewModel::onPhotoErrorDismiss,
                onOpen = viewModel::onPhotoOpen,
                onViewerIndex = viewModel::onViewerIndex,
                onViewerClose = viewModel::onViewerClose,
                onCaptionChange = viewModel::onPhotoCaptionChange,
                onDeleteRequest = viewModel::onPhotoDeleteRequest,
                onDeleteConfirm = viewModel::onPhotoDeleteConfirm,
                onDeleteDismiss = viewModel::onPhotoDeleteDismiss,
            ),
            onSaveSettingsFile = viewModel::onSaveSettingsFile,
            onSaveSettingsToConfigurations = viewModel::onSaveSettingsToConfigurations,
        )
    }
    PassportEditorScreen(state = state, actions = actions, snackbar = snackbar, embedded = embedded)
}

private suspend fun CmpSnackbarHostState.show(effect: PassportEditorEffect) {
    when (effect) {
        PassportEditorEffect.Close -> Unit

        is PassportEditorEffect.SettingsFileSaved -> showSnackbar(
            getString(Strings.string.mountpassport_editor_msg_settings_file_saved, effect.name),
            tone = CmpSnackbarTone.Success,
        )

        PassportEditorEffect.SettingsFileFailed -> showSnackbar(
            getString(Strings.string.mountpassport_editor_error_settings_file),
            tone = CmpSnackbarTone.Error,
        )

        PassportEditorEffect.ConfigurationSaved -> showSnackbar(
            getString(Strings.string.mountpassport_editor_msg_configuration_saved),
            tone = CmpSnackbarTone.Success,
        )

        is PassportEditorEffect.ConfigurationFailed -> showSnackbar(
            getString(Strings.string.mountpassport_editor_error_configuration),
            tone = CmpSnackbarTone.Error,
        )

        is PassportEditorEffect.PhotoFailed -> showSnackbar(effect.error.errorMessage(), tone = CmpSnackbarTone.Error)
    }
}
