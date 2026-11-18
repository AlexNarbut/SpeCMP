package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_error_clear
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_error_older
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_error_save
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_msg_cleared
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_msg_copied
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_msg_queued
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_msg_saved
import ru.alarmtrade.specmp.core.localization.resources.signallog_common_msg_sent
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpWidthClass
import ru.alarmtrade.specmp.core.ui.view.layout.cmpWindowSize
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpTopBarBlur
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogActionsBar
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogClearDialog
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogFeed
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogHeader
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogShareSheet
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogSidePanel
import ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen.subcomponents.SignalLogStatusBar

@Immutable
internal data class SignalLogActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onLoadOlder: () -> Unit = {},
    val onPauseToggle: () -> Unit = {},
    val onClearRequest: () -> Unit = {},
    val onClearConfirm: () -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onShareOpen: () -> Unit = {},
    val onShareDismiss: () -> Unit = {},
    val onProblemChange: (String) -> Unit = {},
    val onSend: (problemTitle: String) -> Unit = {},
    val onSave: (problemTitle: String) -> Unit = {},
    val onSearchOpen: () -> Unit = {},
    val onSearchClose: () -> Unit = {},
    val onSearchQuery: (String) -> Unit = {},
    val onSearchStep: (older: Boolean) -> Unit = {},
    val onOnlyMatchesToggle: () -> Unit = {},
    val onCopy: (String) -> Unit = {},
)

/** Раскладка по ширине окна: до 1100 — нижняя панель и лист «Поделиться», шире — постоянная панель справа. */
internal class SignalLogLayout(val compact: Boolean, val sidePanel: Boolean) {
    val paddingH: Dp get() = if (compact) CmpSpacing.space16 else CmpSpacing.space24
}

private val SidePanelWidth = 420.dp

/** Экран требует подключения по BLE; без связи остаётся рабочим (хост не перекрывает его, см. `composeApp`). */
@Composable
fun SignalLogEntry(onBack: () -> Unit, fileSaver: TextFileSaver) {
    val viewModel = koinViewModel<SignalLogViewModel> { parametersOf(fileSaver) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        scope.launch { snackbar.showSnackbar(message = effect.message(), tone = effect.tone) }
    }
    @Suppress("DEPRECATION") // LocalClipboard требует платформенный ClipEntry; для текста хватает менеджера
    val clipboard = LocalClipboardManager.current
    val actions = remember(viewModel, onBack, clipboard) {
        SignalLogActions(
            onBack = onBack,
            onRetry = viewModel::onRetry,
            onLoadOlder = viewModel::onLoadOlder,
            onPauseToggle = viewModel::onPauseToggle,
            onClearRequest = viewModel::onClearRequest,
            onClearConfirm = viewModel::onClearConfirm,
            onDialogDismiss = viewModel::onDialogDismiss,
            onShareOpen = viewModel::onShareOpen,
            onShareDismiss = viewModel::onShareDismiss,
            onProblemChange = viewModel::onProblemChange,
            onSend = viewModel::onSend,
            onSave = viewModel::onSave,
            onSearchOpen = viewModel::onSearchOpen,
            onSearchClose = viewModel::onSearchClose,
            onSearchQuery = viewModel::onSearchQuery,
            onSearchStep = viewModel::onSearchStep,
            onOnlyMatchesToggle = viewModel::onOnlyMatchesToggle,
            onCopy = { text ->
                clipboard.setText(AnnotatedString(text))
                scope.launch {
                    snackbar.showSnackbar(
                        message = getString(Strings.string.signallog_common_msg_copied),
                        tone = CmpSnackbarTone.Success,
                    )
                }
            },
        )
    }
    SignalLogContent(state = state, actions = actions, snackbar = snackbar)
}

@Composable
internal fun SignalLogContent(
    state: SignalLogState,
    actions: SignalLogActions,
    snackbar: CmpSnackbarHostState = rememberCmpSnackbarHostState(),
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val size = cmpWindowSize
        val layout = remember(size.widthClass) {
            SignalLogLayout(compact = size.isCompact, sidePanel = size.atLeast(CmpWidthClass.Large))
        }
        CmpScaffold(
            topBar = { SignalLogHeader(state = state, actions = actions, layout = layout) },
            bottomBar = {
                if (!layout.sidePanel) SignalLogActionsBar(state = state, actions = actions, layout = layout)
            },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
            topBarBlur = CmpTopBarBlur.Off,
        ) { padding ->
            CmpRow(modifier = Modifier.fillMaxSize().padding(padding)) {
                CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    SignalLogStatusBar(state = state, onPauseToggle = actions.onPauseToggle, layout = layout)
                    SignalLogFeed(
                        state = state,
                        actions = actions,
                        layout = layout,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (layout.sidePanel) {
                    SignalLogSidePanel(
                        state = state,
                        actions = actions,
                        modifier = Modifier.width(SidePanelWidth).fillMaxHeight(),
                    )
                }
            }
        }
        if (!layout.sidePanel && state.sheetOpen) SignalLogShareSheet(state = state, actions = actions, layout = layout)
        when (val dialog = state.dialog) {
            is SignalLogDialog.ConfirmClear -> SignalLogClearDialog(
                count = dialog.count,
                onConfirm = actions.onClearConfirm,
                onDismiss = actions.onDialogDismiss,
            )

            null -> Unit
        }
    }
}

private suspend fun SignalLogEffect.message(): String = getString(
    when (this) {
        SignalLogEffect.Sent -> Strings.string.signallog_common_msg_sent

        SignalLogEffect.Queued -> Strings.string.signallog_common_msg_queued

        is SignalLogEffect.Saved -> Strings.string.signallog_common_msg_saved

        SignalLogEffect.Cleared -> Strings.string.signallog_common_msg_cleared

        SignalLogEffect.Empty -> Strings.string.signallog_common_msg_empty

        is SignalLogEffect.Failed -> when (operation) {
            SignalLogOperation.CLEAR -> Strings.string.signallog_common_error_clear
            SignalLogOperation.SAVE -> Strings.string.signallog_common_error_save
            SignalLogOperation.OLDER -> Strings.string.signallog_common_error_older
        }
    },
)

private val SignalLogEffect.tone: CmpSnackbarTone
    get() = when (this) {
        SignalLogEffect.Sent, is SignalLogEffect.Saved, SignalLogEffect.Cleared -> CmpSnackbarTone.Success
        SignalLogEffect.Queued, SignalLogEffect.Empty -> CmpSnackbarTone.Info
        is SignalLogEffect.Failed -> CmpSnackbarTone.Error
    }
