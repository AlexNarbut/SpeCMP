package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_save
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_share
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_msg_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_title_error
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_error_pick
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_error_save
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_error_share
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_saved
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_transfer_msg_shared
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts

/** Перенос для экрана [owner] (список, предотчёт, «Паспорт оформлен»): лист, экспорт ZIP / PDF, импорт. */
@Immutable
internal class PassportTransfer(
    val state: PassportTransferState,
    val actions: PassportTransferActions,
    val onExport: (ids: List<String>) -> Unit,
    val onExportAll: () -> Unit,
    val onImport: () -> Unit,
    val onExportPdf: (id: String) -> Unit,
)

/**
 * VM переноса экрана [owner] (порты файлов — из корня), сообщения о результате — в [snackbar];
 * [onArchiveBuild] — архив собран (`true` — копия всех). Лист рисует [PassportTransferSheet].
 */
@Composable
internal fun rememberPassportTransfer(
    owner: String,
    ports: MountPassportPorts,
    snackbar: CmpSnackbarHostState,
    onArchiveBuild: (fullBackup: Boolean) -> Unit = {},
): PassportTransfer {
    val viewModel = koinViewModel<PassportTransferViewModel>(key = "mount-passport-transfer-$owner") {
        parametersOf(ports.pdfRenderer, ports.fileExporter, ports.filePicker, ports.imagePicker)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val currentBuild by rememberUpdatedState(onArchiveBuild)
    viewModel.sideEffect.collectSideEffect { effect ->
        if (effect is PassportTransferEffect.ArchiveBuilt) {
            currentBuild(effect.fullBackup)
        } else {
            scope.launch { snackbar.show(effect) }
        }
    }
    val actions = remember(viewModel) {
        PassportTransferActions(
            onClose = viewModel::onClose,
            onIncludeSettingsChange = viewModel::onIncludeSettingsChange,
            onIncludePhotosChange = viewModel::onIncludePhotosChange,
            onShare = viewModel::onShare,
            onSave = viewModel::onSave,
            onRetry = viewModel::onRetry,
            onPickFile = viewModel::onPickFile,
            onPickAnother = viewModel::onPickAnother,
            onReplaceChange = viewModel::onReplaceChange,
            onImportConfirm = viewModel::onImportConfirm,
        )
    }
    return remember(viewModel, state, actions) {
        PassportTransfer(
            state = state,
            actions = actions,
            onExport = viewModel::onExport,
            onExportAll = viewModel::onExportAll,
            onImport = viewModel::onImport,
            onExportPdf = viewModel::onExportPdf,
        )
    }
}

private suspend fun CmpSnackbarHostState.show(effect: PassportTransferEffect) {
    val (text, tone) = when (effect) {
        is PassportTransferEffect.ArchiveBuilt -> return

        PassportTransferEffect.Shared -> getString(Strings.string.mountpassport_transfer_msg_shared) to
            CmpSnackbarTone.Success

        is PassportTransferEffect.Saved ->
            getString(Strings.string.mountpassport_transfer_msg_saved, effect.name) to CmpSnackbarTone.Success

        PassportTransferEffect.SaveFailed -> getString(Strings.string.mountpassport_transfer_error_save) to
            CmpSnackbarTone.Error

        PassportTransferEffect.ShareFailed -> getString(Strings.string.mountpassport_transfer_error_share) to
            CmpSnackbarTone.Error

        PassportTransferEffect.PickFailed -> getString(Strings.string.mountpassport_transfer_error_pick) to
            CmpSnackbarTone.Error

        is PassportTransferEffect.PdfSaved ->
            getString(Strings.string.mountpassport_ready_msg_saved, effect.name) to CmpSnackbarTone.Success

        PassportTransferEffect.PdfSaveFailed -> getString(Strings.string.mountpassport_ready_error_save) to
            CmpSnackbarTone.Error

        PassportTransferEffect.PdfShareFailed -> getString(Strings.string.mountpassport_ready_error_share) to
            CmpSnackbarTone.Error

        is PassportTransferEffect.PdfFailed -> getString(Strings.string.mountpassport_ready_title_error) to
            CmpSnackbarTone.Error
    }
    showSnackbar(text, tone = tone)
}
