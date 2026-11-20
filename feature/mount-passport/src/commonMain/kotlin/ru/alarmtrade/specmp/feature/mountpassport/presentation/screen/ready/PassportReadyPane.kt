package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_refresh
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_save
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_error_share
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_ready_msg_saved
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.layout.CmpAdaptiveBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.PassportTransferSheet
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer.rememberPassportTransfer

/** «Паспорт оформлен» отдельным экраном (телефон, узкое окно): [onBack] — назад в предотчёт. */
@Composable
internal fun PassportReadyPane(
    passportId: String,
    ports: MountPassportPorts,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbar = rememberCmpSnackbarHostState()
    val transfer = rememberPassportTransfer("ready-$passportId", ports, snackbar)
    val currentTransfer by rememberUpdatedState(transfer)
    val document = rememberPassportDocument(
        passportId = passportId,
        ports = ports,
        preview = false,
        snackbar = snackbar,
        onExportZip = { currentTransfer.onExport(listOf(passportId)) },
        onBack = onBack,
    )
    CmpAdaptiveBox(modifier = modifier) { window ->
        PassportReadyScreen(state = document.state, actions = document.actions, snackbar = snackbar)
        PassportTransferSheet(transfer.state, transfer.actions, wide = window.atLeast(CmpWidthClass.Medium))
    }
}

/** Документ паспорта: state, действия, сообщения и [onIssue] — «Сформировать» / «Обновить» широкого окна. */
@Immutable
internal class PassportDocument(
    val state: PassportReadyState,
    val actions: PassportReadyActions,
    val snackbar: CmpSnackbarHostState,
    val onIssue: () -> Unit,
)

/**
 * VM документа (ключ — паспорт): [preview] — живой лист справа от предотчёта (статус не меняет до
 * [PassportDocument.onIssue]), иначе экран «Паспорт оформлен». Сообщения — в [snackbar], «Экспорт ZIP» —
 * [onExportZip].
 */
@Composable
internal fun rememberPassportDocument(
    passportId: String,
    ports: MountPassportPorts,
    preview: Boolean,
    snackbar: CmpSnackbarHostState,
    onExportZip: () -> Unit,
    onBack: () -> Unit = {},
): PassportDocument {
    val key = if (preview) "mount-passport-preview-$passportId" else "mount-passport-ready-$passportId"
    val viewModel = koinViewModel<PassportReadyViewModel>(key = key) {
        parametersOf(
            PassportReadyArgs(passportId, preview = preview),
            ports.pdfRenderer,
            ports.pageRasterizer,
            ports.fileExporter,
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val currentBack by rememberUpdatedState(onBack)
    val currentZip by rememberUpdatedState(onExportZip)
    viewModel.sideEffect.collectSideEffect { effect -> scope.launch { snackbar.show(effect) } }
    val actions = remember(viewModel) {
        PassportReadyActions(
            onBack = { currentBack() },
            onRetry = viewModel::onRetry,
            onPageVisible = viewModel::onPageVisible,
            onExportPdf = viewModel::onExportPdf,
            onExportZip = {
                viewModel.onIssueDismiss()
                currentZip()
            },
            onIssueRetry = viewModel::onIssue,
            onIssueDismiss = viewModel::onIssueDismiss,
        )
    }
    return remember(viewModel, state, actions, snackbar) {
        PassportDocument(state, actions, snackbar, viewModel::onIssue)
    }
}

private suspend fun CmpSnackbarHostState.show(effect: PassportReadyEffect) {
    val (text, tone) = when (effect) {
        is PassportReadyEffect.Saved ->
            getString(Strings.string.mountpassport_ready_msg_saved, effect.name) to CmpSnackbarTone.Success

        PassportReadyEffect.SaveFailed -> getString(Strings.string.mountpassport_ready_error_save) to
            CmpSnackbarTone.Error

        PassportReadyEffect.ShareFailed ->
            getString(Strings.string.mountpassport_ready_error_share) to CmpSnackbarTone.Error

        is PassportReadyEffect.RefreshFailed ->
            getString(Strings.string.mountpassport_ready_error_refresh) to CmpSnackbarTone.Error
    }
    showSnackbar(text, tone = tone)
}
