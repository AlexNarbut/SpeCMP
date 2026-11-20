package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.barcode.isBarcodeScanAvailable
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountpassport_lookup_error_create
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState

/** Экран «Блок» паспорта без блока; [onCreate] — паспорт создан, открыть предотчёт вместо этого экрана. */
@Composable
internal fun PassportLookupPane(onBack: () -> Unit, onCreate: (passportId: String) -> Unit) {
    val viewModel = koinViewModel<PassportLookupViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val currentCreated by rememberUpdatedState(onCreate)
    val currentBack by rememberUpdatedState(onBack)
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is PassportLookupEffect.Created -> currentCreated(effect.passportId)

            is PassportLookupEffect.CreateFailed -> scope.launch {
                snackbar.showSnackbar(
                    getString(Strings.string.mountpassport_lookup_error_create),
                    tone = CmpSnackbarTone.Error,
                )
            }

            PassportLookupEffect.FocusId -> focusRequester.requestFocus()
        }
    }
    val actions = remember(viewModel) {
        PassportLookupActions(
            onBack = { currentBack() },
            onIdChange = viewModel::onIdChange,
            onFind = viewModel::onFind,
            onModelSelect = viewModel::onModelSelect,
            onOtherModel = viewModel::onOtherModel,
            onCustomModelChange = viewModel::onCustomModelChange,
            onModelsRetry = viewModel::onModelsRetry,
            onNext = viewModel::onNext,
            scanner = PassportLookupScanActions(
                onScan = viewModel::onScanClick,
                onAccessRetry = viewModel::onScanAccessRetry,
                onOpenSettings = viewModel::onScanOpenSettings,
                onScanned = viewModel::onScanned,
                onClose = viewModel::onScanClose,
                onManual = viewModel::onScanManual,
            ),
        )
    }
    PassportLookupScreen(
        state = state,
        actions = actions,
        snackbar = snackbar,
        scanAvailable = isBarcodeScanAvailable,
        focusRequester = focusRequester,
    )
}
