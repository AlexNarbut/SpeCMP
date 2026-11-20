package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import androidx.compose.runtime.Composable
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
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_error_delete
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_list_msg_deleted
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportCallbacks
import ru.alarmtrade.specmp.feature.mountpassport.presentation.navigation.MountPassportPorts
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.PassportEditorPane

/** Список со своей VM; широкое окно показывает справа предотчёт выбранного паспорта. */
@Composable
internal fun PassportListPane(
    mode: PassportListMode,
    callbacks: MountPassportCallbacks,
    ports: MountPassportPorts,
    onBack: () -> Unit,
    onOpen: (passportId: String) -> Unit,
    onNewWithoutUnit: () -> Unit,
    onCreatePdf: (passportId: String) -> Unit,
) {
    val viewModel = koinViewModel<PassportListViewModel> { parametersOf(mode) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val currentBack by rememberUpdatedState(onBack)
    val currentOpen by rememberUpdatedState(onOpen)
    val currentNewWithoutUnit by rememberUpdatedState(onNewWithoutUnit)
    val currentPdf by rememberUpdatedState(onCreatePdf)
    viewModel.sideEffect.collectSideEffect { effect ->
        scope.launch {
            when (effect) {
                PassportListEffect.Deleted -> snackbar.showSnackbar(
                    getString(Strings.string.mountpassport_list_msg_deleted),
                    tone = CmpSnackbarTone.Info,
                )

                is PassportListEffect.DeleteFailed -> snackbar.showSnackbar(
                    getString(Strings.string.mountpassport_list_error_delete),
                    tone = CmpSnackbarTone.Error,
                )
            }
        }
    }
    val actions = remember(viewModel, callbacks, mode) {
        PassportListActions(
            onBack = { currentBack() },
            onQueryChange = viewModel::onQueryChange,
            onRetry = viewModel::onRetry,
            onOpen = { currentOpen(it.id) },
            onSelect = { viewModel.onSelect(it.id) },
            onFinish = { callbacks.onFinish(it.id) },
            onNew = { (mode as? PassportListMode.Unit)?.let { callbacks.onNewPassport(it.systemId) } },
            onNewWithoutUnit = { currentNewWithoutUnit() },
            onDeleteRequest = viewModel::onDeleteRequest,
            onDeleteDismiss = viewModel::onDeleteDismiss,
            onDeleteConfirm = viewModel::onDeleteConfirm,
        )
    }
    PassportListScreen(
        state = state,
        actions = actions,
        snackbar = snackbar,
        detail = { id ->
            PassportEditorPane(id, callbacks, ports, embedded = true, onClose = {}, onCreatePdf = { currentPdf(id) })
        },
    )
}
