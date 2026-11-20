package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_reread
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_reread_lost
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_reread_wrong_unit
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_error_settings_file
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_configuration_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_reread
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_editor_msg_settings_file_saved
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.errorMessage
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportCallbacks
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.PassportPreviewPane
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.rememberPassportDocument
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready.subcomponents.IssuedDialog
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferSheet
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.rememberPassportTransfer

/**
 * Предотчёт со своей VM (ключ — паспорт: в широком окне списка их может быть несколько). Отдельным экраном «Назад»
 * дописывает несохранённое и уходит без подтверждения ([onClose]); встроенный ([embedded]) назад не перехватывает.
 * «Сформировать» / «Обновить» (после проверки пропавших фото): узкое окно и встроенный — экран «Паспорт оформлен»
 * ([onCreatePdf]); отдельным экраном в широком окне (≥ 840) — поля слева, живой лист справа и диалог
 * «Паспорт оформлен» поверх. «Экспорт PDF» / «Экспорт ZIP» — свой перенос предотчёта.
 */
@Composable
internal fun PassportEditorPane(
    passportId: String,
    callbacks: MountPassportCallbacks,
    ports: MountPassportPorts,
    embedded: Boolean,
    onClose: () -> Unit,
    onCreatePdf: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        EditorPane(
            passportId = passportId,
            callbacks = callbacks,
            ports = ports,
            layout = when {
                embedded -> EditorLayout.EMBEDDED
                maxWidth < WorkspaceMinWidth -> EditorLayout.SCREEN
                else -> EditorLayout.WORKSPACE
            },
            sheetWide = maxWidth >= CmpBreakpoints.Medium,
            onClose = onClose,
            onCreatePdf = onCreatePdf,
        )
    }
}

private enum class EditorLayout { EMBEDDED, SCREEN, WORKSPACE }

@Suppress("LongParameterList")
@Composable
private fun EditorPane(
    passportId: String,
    callbacks: MountPassportCallbacks,
    ports: MountPassportPorts,
    layout: EditorLayout,
    sheetWide: Boolean,
    onClose: () -> Unit,
    onCreatePdf: () -> Unit,
) {
    val viewModel = koinViewModel<PassportEditorViewModel>(key = "mount-passport-editor-$passportId") {
        parametersOf(PassportEditorArgs(passportId), ports.imagePicker, ports.settingsFileSaver)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val transfer = rememberPassportTransfer("editor-$passportId", ports, snackbar)
    val currentTransfer by rememberUpdatedState(transfer)
    val document = if (layout == EditorLayout.WORKSPACE) {
        rememberPassportDocument(
            passportId = passportId,
            ports = ports,
            preview = true,
            snackbar = rememberCmpSnackbarHostState(),
            onExportZip = { currentTransfer.onExport(listOf(passportId)) },
        )
    } else {
        null
    }
    val scope = rememberCoroutineScope()
    val currentClose by rememberUpdatedState(onClose)
    val currentPdf by rememberUpdatedState(onCreatePdf)
    val currentDocument by rememberUpdatedState(document)
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            PassportEditorEffect.Close -> currentClose()
            PassportEditorEffect.OpenDocument -> currentDocument?.onIssue?.invoke() ?: currentPdf()
            else -> scope.launch { snackbar.show(effect) }
        }
    }
    if (layout != EditorLayout.EMBEDDED) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = viewModel::onBack,
        )
    }
    val actions = remember(viewModel, callbacks, passportId, ports) {
        PassportEditorActions(
            onBack = viewModel::onBack,
            onRetry = viewModel::onRetry,
            onFieldChange = viewModel::onFieldChange,
            onPickVehicle = viewModel::onVehiclePickStart.takeIf { ports.vehiclePicker != null },
            onWorkToggle = viewModel::onWorkToggle,
            onReread = viewModel::onReread,
            onReadUnit = { callbacks.onReadUnit(passportId) },
            onCreatePdf = viewModel::onCreatePdf,
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
                onMissingRemove = viewModel::onMissingPhotosRemove,
                onMissingContinue = viewModel::onMissingPhotosContinue,
                onMissingDismiss = viewModel::onMissingPhotosDismiss,
            ),
            onSaveSettingsFile = viewModel::onSaveSettingsFile,
            onSaveSettingsToConfigurations = viewModel::onSaveSettingsToConfigurations,
            onSectionToggle = viewModel::onSectionToggle,
            onExportPdf = { currentTransfer.onExportPdf(passportId) },
            onExportZip = { currentTransfer.onExport(listOf(passportId)) },
        )
    }
    val picker = ports.vehiclePicker
    if (state.vehiclePicking && picker != null) {
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = viewModel::onVehiclePickCancel,
        )
        picker(state.passport.content?.unit?.systemName, viewModel::onVehiclePicked, viewModel::onVehiclePickCancel)
        return
    }
    val exporting = transfer.state.pdfExporting == passportId
    when (layout) {
        EditorLayout.EMBEDDED ->
            PassportEditorScreen(state, actions, snackbar, embedded = true, exporting = exporting)

        EditorLayout.SCREEN -> PassportEditorScreen(state, actions, snackbar, exporting = exporting)

        EditorLayout.WORKSPACE -> if (document != null) {
            PassportWorkspace(
                state = state,
                actions = actions,
                snackbar = snackbar,
                exporting = exporting,
            ) { modifier ->
                PassportPreviewPane(document.state, document.actions, document.snackbar, modifier)
            }
            IssuedDialog(document.state, document.actions)
        }
    }
    PassportTransferSheet(transfer.state, transfer.actions, wide = sheetWide)
}

private val WorkspaceMinWidth = 840.dp

private suspend fun CmpSnackbarHostState.show(effect: PassportEditorEffect) {
    when (effect) {
        PassportEditorEffect.Close, PassportEditorEffect.OpenDocument -> Unit

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

        PassportEditorEffect.Reread -> showSnackbar(
            getString(Strings.string.mountpassport_editor_msg_reread),
            tone = CmpSnackbarTone.Success,
        )

        is PassportEditorEffect.RereadFailed -> showSnackbar(effect.error.message(), tone = CmpSnackbarTone.Error)
    }
}

private suspend fun RereadError.message(): String = when (this) {
    is RereadError.WrongUnit -> getString(Strings.string.mountpassport_editor_error_reread_wrong_unit, actualSystemId)

    is RereadError.NotRead -> getString(
        if (connectionLost) {
            Strings.string.mountpassport_editor_error_reread_lost
        } else {
            Strings.string.mountpassport_editor_error_reread
        },
    )

    is RereadError.Common -> error.errorMessage()
}
