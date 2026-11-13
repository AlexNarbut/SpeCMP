package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_format_subtitle
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_value_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_value_usb
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_a11y_system
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_delete
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_ok
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_system
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_command
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_read_msg
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_read_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_refused_msg
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_refused_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_unsupported_msg
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_unsupported_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_address_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_delete_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_group_count
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_instruction_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_occupied
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_loading_program
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_loading_read
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_delete
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_deleted
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_empty_hint
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_live
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_pin
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_select
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_update_sent
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_title_slot
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.icon.imageVector
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.GroupHeader
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.ui
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.deviceTitle
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog

private val WideFrom = 760.dp
private val ListMaxWidth = 640.dp
private val CompactMaxWidth = 720.dp
private val BigButton = 56.dp
private const val LOADING_ROWS = 5

@Immutable
internal class PairingActions(
    val onBack: () -> Unit = {},
    val onOpenSystemDevices: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSlotClick: (Int) -> Unit = {},
    val onSheetDismiss: () -> Unit = {},
    val onPair: (Int) -> Unit = {},
    val onInstruction: (Int) -> Unit = {},
    val onInstructionToggle: () -> Unit = {},
    val onAddressChange: (String) -> Unit = {},
    val onAddressSubmit: () -> Unit = {},
    val onPairingCancel: () -> Unit = {},
    val onPairingClose: () -> Unit = {},
    val onPairingRetry: () -> Unit = {},
    val onDelete: (Int) -> Unit = {},
    val onDeleteConfirm: () -> Unit = {},
    val onUpdate: (Int) -> Unit = {},
    val onDialogDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onPinDismiss: () -> Unit = {},
)

/** «Приживление устройств» — BLE и USB; [onOpenSystemDevices] показывается только по Bluetooth. */
@Composable
fun PairingEntry(onBack: () -> Unit, onOpenSystemDevices: () -> Unit) {
    val viewModel = koinViewModel<PairingViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is PairingEffect.Snackbar -> scope.launch {
                snackbar.showSnackbar(message = getString(effect.message.text))
            }
        }
    }
    val actions = remember(viewModel, onBack, onOpenSystemDevices) {
        PairingActions(
            onBack = onBack,
            onOpenSystemDevices = onOpenSystemDevices,
            onRetry = viewModel::onRetry,
            onSlotClick = viewModel::onSlotClick,
            onSheetDismiss = viewModel::onSheetDismiss,
            onPair = viewModel::onPair,
            onInstruction = viewModel::onInstruction,
            onInstructionToggle = viewModel::onInstructionToggle,
            onAddressChange = viewModel::onAddressChange,
            onAddressSubmit = viewModel::onAddressSubmit,
            onPairingCancel = viewModel::onPairingCancel,
            onPairingClose = viewModel::onPairingClose,
            onPairingRetry = viewModel::onPairingRetry,
            onDelete = viewModel::onDelete,
            onDeleteConfirm = viewModel::onDeleteConfirm,
            onUpdate = viewModel::onUpdate,
            onDialogDismiss = viewModel::onDialogDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onPinDismiss = viewModel::onPinDismiss,
        )
    }
    PairingContent(state = state, actions = actions, snackbar = snackbar)
}

private val PairingMessage.text: StringResource
    get() = when (this) {
        PairingMessage.DELETED -> Strings.string.peripherals_pairing_msg_deleted
        PairingMessage.UPDATE_SENT -> Strings.string.peripherals_pairing_msg_update_sent
        PairingMessage.COMMAND_FAILED -> Strings.string.peripherals_pairing_error_command
    }

@Composable
internal fun PairingContent(
    state: PairingState,
    actions: PairingActions,
    snackbar: CmpSnackbarHostState = rememberCmpSnackbarHostState(),
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= WideFrom
        val panelWidth = if (maxWidth >= CmpBreakpoints.Large) 480.dp else 380.dp
        CmpScaffold(
            topBar = { TopBar(state, actions, wide) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
        ) { padding ->
            val modifier = Modifier.fillMaxSize().padding(padding)
            when (state.phase) {
                PairingPhase.ENTERING, PairingPhase.READING -> Loading(state.phase, modifier)

                PairingPhase.REFUSED, PairingPhase.READ_FAILED -> Failure(state.phase, actions.onRetry, modifier)

                PairingPhase.READY -> if (wide) {
                    CmpRow(modifier = modifier) {
                        SlotList(state, actions, wide = true, Modifier.weight(1f).fillMaxHeight(), ListMaxWidth)
                        SidePanel(state, actions, panelWidth)
                    }
                } else {
                    SlotList(state, actions, wide = false, modifier, CompactMaxWidth)
                }
            }
        }
        if (!wide) CompactSheets(state, actions)
    }
    Dialogs(state, actions)
}

@Composable
private fun TopBar(state: PairingState, actions: PairingActions, wide: Boolean) {
    val subtitle = state.deviceName?.let {
        stringResource(
            Strings.string.peripherals_common_format_subtitle,
            it,
            stringResource(
                if (state.bluetooth) {
                    Strings.string.peripherals_common_value_bluetooth
                } else {
                    Strings.string.peripherals_common_value_usb
                },
            ),
        )
    }
    CmpTopAppBar(
        title = { CmpText(text = stringResource(Strings.string.peripherals_pairing_title)) },
        subtitle = subtitle?.let { { CmpText(text = it, style = CmpTextStyle.Caption) } },
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.peripherals_common_a11y_back),
            )
        },
        actions = {
            // По USB «Устройства системы» недоступны: SDK читает периферию только по BLE.
            if (state.bluetooth && wide) {
                CmpOutlinedButton(
                    onClick = actions.onOpenSystemDevices,
                    modifier = Modifier.padding(end = 8.dp).height(48.dp),
                ) {
                    CmpIcon(icon = CmpIcons.Sensors, contentDescription = null)
                    CmpText(
                        text = stringResource(Strings.string.peripherals_pairing_btn_system),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else if (state.bluetooth) {
                CmpIconButton(
                    onClick = actions.onOpenSystemDevices,
                    icon = CmpIcons.Sensors,
                    contentDescription = stringResource(Strings.string.peripherals_pairing_a11y_system),
                )
            }
        },
    )
}

@Composable
private fun Loading(phase: PairingPhase, modifier: Modifier) {
    CmpBox(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        CmpColumn(
            modifier = Modifier.widthIn(max = CompactMaxWidth).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
                CmpText(
                    text = stringResource(
                        if (phase == PairingPhase.ENTERING) {
                            Strings.string.peripherals_pairing_loading_program
                        } else {
                            Strings.string.peripherals_pairing_loading_read
                        },
                    ),
                    fontWeight = FontWeight.SemiBold,
                    style = CmpTextStyle.BodyLarge,
                )
            }
            CmpLinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(6.dp))
            repeat(LOADING_ROWS) {
                CmpRow(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    CmpSkeleton(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(16.dp))
                    CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE).height(18.dp))
                        CmpSkeleton(modifier = Modifier.width(130.dp).height(28.dp), shape = CircleShape)
                    }
                }
            }
        }
    }
}

private const val SKELETON_TITLE = 0.5f
private val RefusedTitle = Strings.string.peripherals_pairing_error_refused_title
private val ReadTitle = Strings.string.peripherals_pairing_error_read_title
private val RefusedMessage = Strings.string.peripherals_pairing_error_refused_msg
private val ReadMessage = Strings.string.peripherals_pairing_error_read_msg

@Composable
private fun Failure(phase: PairingPhase, onRetry: () -> Unit, modifier: Modifier) {
    val refused = phase == PairingPhase.REFUSED
    CmpBox(modifier = modifier.padding(28.dp), contentAlignment = Alignment.Center) {
        CmpStateBox(
            icon = if (refused) CmpIcons.Lock else CmpIcons.SyncProblem,
            title = stringResource(if (refused) RefusedTitle else ReadTitle),
            text = stringResource(if (refused) RefusedMessage else ReadMessage),
            tone = CmpStateTone.Error,
        ) {
            CmpButton(onClick = onRetry, modifier = Modifier.height(BigButton)) {
                CmpIcon(icon = CmpIcons.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_retry),
                    style = CmpTextStyle.BodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SlotList(state: PairingState, actions: PairingActions, wide: Boolean, modifier: Modifier, maxWidth: Dp) {
    val pairingSlot = state.slot(state.pairing?.slot)
    val session = state.pairing
    CmpBox(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = maxWidth).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (!wide && session != null && pairingSlot != null) {
                stickyHeader(key = "pairing") {
                    CmpBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cmpThemeColors().background)
                            .padding(top = 8.dp, bottom = 4.dp),
                    ) {
                        PairingMode(
                            session = session,
                            slot = pairingSlot,
                            panel = false,
                            instructionOpen = state.instructionOpen,
                            onToggleInstruction = actions.onInstructionToggle,
                            onCancel = actions.onPairingCancel,
                            onClose = actions.onPairingClose,
                            onRetry = actions.onPairingRetry,
                            onInstruction = { actions.onInstruction(pairingSlot.slot) },
                        )
                    }
                }
            }
            item(key = "summary") { Summary(state) }
            if (state.occupied == 0 && session == null) item(key = "hint") { EmptyHint() }
            slotGroups(state, actions, wide)
        }
    }
}

private fun LazyListScope.slotGroups(state: PairingState, actions: PairingActions, wide: Boolean) {
    state.groups.forEach { group ->
        item(key = "group_${group.type}") {
            GroupHeader(
                title = stringResource(group.type.ui.group),
                trailing = stringResource(
                    Strings.string.peripherals_pairing_format_group_count,
                    group.slots.count { it.paired },
                    group.slots.size,
                ),
            )
        }
        items(group.slots, key = { "slot_${it.slot}" }) { slot ->
            SlotRow(
                slot = slot,
                waiting = state.waiting && state.pairing?.slot == slot.slot,
                deleting = slot.slot in state.deleting,
                selected = wide && state.selected == slot.slot,
                flashSeq = state.flash?.takeIf { it.slot == slot.slot }?.seq,
                canPair = !state.waiting,
                wide = wide,
                onClick = { actions.onSlotClick(slot.slot) },
                onPair = { actions.onPair(slot.slot) },
            )
        }
    }
}

@Composable
private fun Summary(state: PairingState) {
    val theme = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_format_occupied, state.occupied, state.slots.size),
            fontWeight = FontWeight.Bold,
            style = CmpTextStyle.BodyLarge,
            modifier = Modifier.weight(1f),
        )
        CmpBox(modifier = Modifier.size(8.dp).background(cmpDomainColors().success, CircleShape))
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_msg_live),
            color = theme.onSurfaceVariant,
            style = CmpTextStyle.Body,
        )
    }
}

@Composable
private fun EmptyHint() {
    val theme = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .background(theme.primaryContainer, RoundedCornerShape(18.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(icon = CmpIcons.Info, contentDescription = null, tint = theme.onPrimaryContainer)
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_msg_empty_hint),
            color = theme.onPrimaryContainer,
            style = CmpTextStyle.BodyLarge,
        )
    }
}

/** Широкое окно: режим приживления, форма адреса, действия слота с инструкцией или подсказка. */
@Composable
private fun SidePanel(state: PairingState, actions: PairingActions, width: Dp) {
    val theme = cmpThemeColors()
    val session = state.pairing
    val pairingSlot = state.slot(session?.slot)
    val selected = state.slot(state.selected)
    CmpColumn(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(theme.surfaceContainerLow)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when {
            session != null && pairingSlot != null -> PairingMode(
                session = session,
                slot = pairingSlot,
                panel = true,
                instructionOpen = true,
                onToggleInstruction = {},
                onCancel = actions.onPairingCancel,
                onClose = actions.onPairingClose,
                onRetry = actions.onPairingRetry,
                onInstruction = {},
            )

            selected != null && state.sheet == SlotSheet.ADDRESS -> {
                CmpText(
                    text = stringResource(
                        Strings.string.peripherals_pairing_format_address_title,
                        deviceTitle(selected.type, selected.number),
                    ),
                    style = CmpTextStyle.Title,
                )
                AddressForm(
                    state.address,
                    state.addressError,
                    actions.onAddressChange,
                    actions.onAddressSubmit,
                    actions.onSheetDismiss,
                )
            }

            selected != null -> {
                SlotActions(
                    slot = selected,
                    onPair = { actions.onPair(selected.slot) },
                    onInstruction = { actions.onInstruction(selected.slot) },
                    onUpdate = { actions.onUpdate(selected.slot) },
                    onDelete = { actions.onDelete(selected.slot) },
                )
                if (!selected.paired || state.sheet == SlotSheet.INSTRUCTION) InstructionView(selected.type)
            }

            else -> CmpText(
                text = stringResource(Strings.string.peripherals_pairing_msg_select),
                color = theme.onSurfaceVariant,
                style = CmpTextStyle.BodyLarge,
            )
        }
    }
}

@Composable
private fun CompactSheets(state: PairingState, actions: PairingActions) {
    val instructionType = state.slot(state.selected)?.type
    val selected = state.slot(state.selected)
    when {
        state.sheet == SlotSheet.INSTRUCTION && instructionType != null -> SheetFrame(
            title = stringResource(
                Strings.string.peripherals_pairing_format_instruction_title,
                stringResource(instructionType.ui.name),
            ),
            onDismiss = actions.onSheetDismiss,
        ) { InstructionView(instructionType) }

        state.sheet == SlotSheet.ACTIONS && selected != null -> SheetFrame(
            title = stringResource(Strings.string.peripherals_pairing_title_slot),
            onDismiss = actions.onSheetDismiss,
        ) {
            SlotActions(
                slot = selected,
                onPair = { actions.onPair(selected.slot) },
                onInstruction = { actions.onInstruction(selected.slot) },
                onUpdate = { actions.onUpdate(selected.slot) },
                onDelete = { actions.onDelete(selected.slot) },
            )
        }

        state.sheet == SlotSheet.ADDRESS && selected != null -> SheetFrame(
            title = stringResource(
                Strings.string.peripherals_pairing_format_address_title,
                deviceTitle(selected.type, selected.number),
            ),
            onDismiss = actions.onSheetDismiss,
        ) {
            AddressForm(
                state.address,
                state.addressError,
                actions.onAddressChange,
                actions.onAddressSubmit,
                onCancel = null,
            )
        }
    }
}

@Composable
private fun SheetFrame(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    CmpBottomSheet(onDismissRequest = onDismiss) {
        CmpColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 8.dp, bottom = 24.dp),
        ) {
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                CmpText(
                    text = title,
                    color = cmpThemeColors().onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    style = CmpTextStyle.Body,
                    modifier = Modifier.weight(1f),
                )
                CmpIconButton(
                    onClick = onDismiss,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.peripherals_common_a11y_close),
                )
            }
            CmpBox(modifier = Modifier.padding(end = 12.dp)) { content() }
        }
    }
}

@Composable
private fun Dialogs(state: PairingState, actions: PairingActions) {
    state.pin?.let { pin ->
        ServicePinDialog(
            message = stringResource(Strings.string.peripherals_pairing_msg_pin),
            state = pin,
            onDismiss = actions.onPinDismiss,
            onSubmit = actions.onPinSubmit,
        )
        return
    }
    when (val dialog = state.dialog) {
        is PairingDialog.Delete -> {
            val slot = state.slot(dialog.slot)
            val name = slot?.let { deviceTitle(it.type, it.number) }.orEmpty()
            CmpDialog(
                onDismissRequest = actions.onDialogDismiss,
                icon = CmpIcons.Delete.imageVector(),
                title = {
                    CmpText(
                        text = stringResource(Strings.string.peripherals_pairing_format_delete_title, name),
                        textAlign = TextAlign.Center,
                    )
                },
                text = {
                    CmpText(
                        text = stringResource(Strings.string.peripherals_pairing_msg_delete),
                        style = CmpTextStyle.BodyLarge,
                    )
                },
                confirmButton = {
                    CmpButton(
                        text = stringResource(Strings.string.peripherals_pairing_btn_delete),
                        onClick = actions.onDeleteConfirm,
                    )
                },
                dismissButton = {
                    CmpTextButton(
                        text = stringResource(Strings.string.peripherals_pairing_btn_cancel),
                        onClick = actions.onDialogDismiss,
                    )
                },
            )
        }

        PairingDialog.Unsupported -> CmpDialog(
            onDismissRequest = actions.onDialogDismiss,
            icon = CmpIcons.Block.imageVector(),
            title = {
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_error_unsupported_title),
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_error_unsupported_msg),
                    style = CmpTextStyle.BodyLarge,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.peripherals_pairing_btn_ok),
                    onClick = actions.onDialogDismiss,
                )
            },
        )

        null -> Unit
    }
}
