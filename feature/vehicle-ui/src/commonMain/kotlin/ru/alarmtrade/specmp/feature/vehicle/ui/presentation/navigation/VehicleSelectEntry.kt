package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_select_msg_can_copied
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.vehicleLayoutOf
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.messageText
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectContent
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectEffect
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectViewModel

/**
 * [onPick] — только в [VehicleSelectMode.PICK], закрыть экран — дело вызывающего; `null` у [onOpenFirmwareArchive] и
 * [onOpenUserManual] — без кнопки и перехода.
 */
@Composable
fun VehicleSelectEntry(
    request: VehicleSelectRequest,
    onPick: (CarSummary) -> Unit,
    onBack: () -> Unit,
    onOpenFirmwareArchive: ((canCode: String) -> Unit)? = null,
    onOpenUserManual: ((manualId: Int) -> Unit)? = null,
) {
    // Ключ — запрос: каталог для другого режима в том же владельце (справочник и выбор для архива) — своя ViewModel.
    val viewModel = koinViewModel<VehicleSelectViewModel>(
        key = "VehicleSelect:$request",
        parameters = { parametersOf(request) },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = rememberCmpSnackbarHostState()
    val uriHandler = LocalUriHandler.current

    @Suppress("DEPRECATION") // LocalClipboard требует платформенный ClipEntry; для текста хватает менеджера
    val clipboard = LocalClipboardManager.current
    val currentOnPick by rememberUpdatedState(onPick)
    val currentOnBack by rememberUpdatedState(onBack)

    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is VehicleSelectEffect.Picked -> currentOnPick(effect.car)

            VehicleSelectEffect.Exit -> currentOnBack()

            is VehicleSelectEffect.CanCopied -> {
                clipboard.setText(AnnotatedString(effect.code))
                scope.launch {
                    snackbarHostState.showSnackbar(getString(Strings.string.vehicle_select_msg_can_copied, effect.code))
                }
            }

            is VehicleSelectEffect.PickFailed -> scope.launch {
                snackbarHostState.showSnackbar(effect.error.messageText())
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val layout = vehicleLayoutOf(maxWidth)
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            onBackCompleted = { viewModel.onBack(layout) },
        )
        val actions = remember(viewModel, layout, onOpenFirmwareArchive, onOpenUserManual) {
            VehicleSelectActions(
                onBack = { viewModel.onBack(layout) },
                onTypePick = viewModel::onTypePick,
                onSearchModeChange = viewModel::onSearchModeChange,
                onQueryChange = viewModel::onQueryChange,
                onSearchByCan = viewModel::onSearchByCan,
                onCanCodeChange = viewModel::onCanCodeChange,
                onBrandPick = viewModel::onBrandPick,
                onModelPick = { viewModel.onModelPick(it, wide = layout.isWide) },
                onTrimOpen = viewModel::onTrimOpen,
                onCanResultOpen = viewModel::onCanResultOpen,
                onRecentOpen = viewModel::onRecentOpen,
                onRecentRemove = viewModel::onRecentRemove,
                onCrumb = viewModel::onCrumb,
                onRetry = viewModel::onRetry,
                onFilterOpen = viewModel::onFilterOpen,
                onFilterToggle = viewModel::onFilterToggle,
                onFilterDraftReset = viewModel::onFilterDraftReset,
                onFilterApply = viewModel::onFilterApply,
                onFilterDismiss = viewModel::onFilterDismiss,
                onFilterClear = viewModel::onFilterClear,
                onPick = viewModel::onPick,
                onPaneWidthChange = viewModel::onPaneWidthChange,
                card = CardActions(
                    onTabSelect = viewModel::onTabSelect,
                    onPointOpen = viewModel::onPointOpen,
                    onPointClose = viewModel::onPointClose,
                    onPhotoChange = viewModel::onPhotoChange,
                    onViewerOpen = viewModel::onViewerOpen,
                    onViewerClose = viewModel::onViewerClose,
                    onNotesToggle = viewModel::onNotesToggle,
                    onGroupToggle = viewModel::onGroupToggle,
                    onCapabilityNote = viewModel::onCapabilityNote,
                    onCopyCanCode = viewModel::onCopyCanCode,
                    onOpenDocument = { url -> safeRun { uriHandler.openUri(url) } },
                    onOpenFirmwareArchive = onOpenFirmwareArchive,
                    onOpenUserManual = onOpenUserManual,
                    onDialogDismiss = viewModel::onDialogDismiss,
                    onMismatchConfirm = viewModel::onMismatchConfirm,
                ),
            )
        }
        VehicleSelectContent(
            state = state,
            layout = layout,
            windowWidth = maxWidth,
            actions = actions,
            snackbarHostState = snackbarHostState,
        )
    }
}
