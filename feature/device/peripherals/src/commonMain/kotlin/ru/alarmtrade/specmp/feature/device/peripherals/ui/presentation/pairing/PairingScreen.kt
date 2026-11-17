package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.draw.clip
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
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
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.ListInsets
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.ui
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.deviceTitle
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog

private val WideFrom = 760.dp
private val ListMaxWidth = 640.dp
private val CompactMaxWidth = 720.dp
private val SkeletonTag = 130.dp
private val SpinnerStroke = 2.dp
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
                snackbar.showSnackbar(message = getString(effect.message.text), tone = effect.message.tone)
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

private val PairingMessage.tone: CmpSnackbarTone
    get() = when (this) {
        PairingMessage.DELETED -> CmpSnackbarTone.Success
        PairingMessage.UPDATE_SENT -> CmpSnackbarTone.Info
        PairingMessage.COMMAND_FAILED -> CmpSnackbarTone.Error
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
        val gutter = if (maxWidth >=
            CmpBreakpoints.Medium
        ) {
            CmpSpacing.screenPaddingHMedium
        } else {
            CmpSpacing.screenPaddingH
        }
        CmpScaffold(
            topBar = { TopBar(state, actions, wide) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
        ) { padding ->
            val insets = ListInsets(top = padding.calculateTopPadding(), horizontal = gutter)
            val modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())
            when (state.phase) {
                PairingPhase.ENTERING, PairingPhase.READING -> Loading(state.phase, modifier.padding(padding))

                PairingPhase.REFUSED, PairingPhase.READ_FAILED ->
                    Failure(state.phase, actions.onRetry, modifier.padding(padding))

                PairingPhase.READY -> if (wide) {
                    CmpRow(modifier = modifier) {
                        SlotList(state, actions, wide = true, insets, Modifier.weight(1f).fillMaxHeight(), ListMaxWidth)
                        CmpVerticalDivider()
                        SidePanel(state, actions, panelWidth, insets.top)
                    }
                } else {
                    SlotList(state, actions, wide = false, insets, modifier, CompactMaxWidth)
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
        title = stringResource(Strings.string.peripherals_pairing_title),
        subtitle = subtitle,
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
                CmpButton(
                    text = stringResource(Strings.string.peripherals_pairing_btn_system),
                    onClick = actions.onOpenSystemDevices,
                    type = CmpButtonType.Secondary,
                    size = CmpButtonSize.S,
                    icon = CmpIcons.Sensors,
                    modifier = Modifier.padding(end = CmpSpacing.space8),
                )
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
            modifier = Modifier
                .widthIn(max = CompactMaxWidth)
                .fillMaxWidth()
                .padding(horizontal = CmpSpacing.screenPaddingH, vertical = CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconS), strokeWidth = SpinnerStroke)
                CmpText(
                    text = stringResource(
                        if (phase == PairingPhase.ENTERING) {
                            Strings.string.peripherals_pairing_loading_program
                        } else {
                            Strings.string.peripherals_pairing_loading_read
                        },
                    ),
                    style = CmpTextStyle.Headline3,
                )
            }
            CmpLinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(CmpSpacing.space4).clip(RoundedCornerShape(CmpRadius.round)),
                trackColor = CmpTheme.colors.backgroundSurfaceHighlight,
            )
            repeat(LOADING_ROWS) {
                CmpRow(
                    modifier = Modifier.heightIn(min = CmpSize.listItem).padding(horizontal = CmpSpacing.space16),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                ) {
                    CmpSkeleton(
                        modifier = Modifier.size(CmpSize.touchMin),
                        shape = RoundedCornerShape(CmpRadius.cardSmall),
                    )
                    CmpColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                    ) {
                        CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE).height(CmpSpacing.space16))
                        CmpSkeleton(
                            modifier = Modifier.width(SkeletonTag).height(CmpSize.tag),
                            shape = RoundedCornerShape(CmpRadius.tags),
                        )
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
    CmpBox(modifier = modifier.padding(CmpSpacing.screenPaddingH), contentAlignment = Alignment.Center) {
        CmpStateBox(
            icon = if (refused) CmpIcons.Lock else CmpIcons.SyncProblem,
            title = stringResource(if (refused) RefusedTitle else ReadTitle),
            text = stringResource(if (refused) RefusedMessage else ReadMessage),
            tone = CmpStateTone.Error,
        ) {
            CmpButton(
                text = stringResource(Strings.string.peripherals_pairing_btn_retry),
                onClick = onRetry,
                icon = CmpIcons.Refresh,
            )
        }
    }
}

@Composable
private fun SlotList(
    state: PairingState,
    actions: PairingActions,
    wide: Boolean,
    insets: ListInsets,
    modifier: Modifier,
    maxWidth: Dp,
) {
    val pairingSlot = state.slot(state.pairing?.slot)
    val session = state.pairing
    CmpBox(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = maxWidth).fillMaxSize(),
            contentPadding = insets.list(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            if (!wide && session != null && pairingSlot != null) {
                stickyHeader(key = "pairing") {
                    CmpBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CmpTheme.colors.backgroundPagePrimary)
                            .padding(top = CmpSpacing.space8, bottom = CmpSpacing.space4),
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
    val colors = CmpTheme.colors
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(
            start = CmpSpacing.space4,
            end = CmpSpacing.space4,
            top = CmpSpacing.space8,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_format_occupied, state.occupied, state.slots.size),
            style = CmpTextStyle.Headline2,
            modifier = Modifier.weight(1f),
        )
        CmpBox(modifier = Modifier.size(CmpSpacing.space8).background(colors.successDefault, CircleShape))
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_msg_live),
            color = colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Additional0,
        )
    }
}

@Composable
private fun EmptyHint() {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = CmpSpacing.space8)
            .background(colors.brandPrimaryHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.Info, contentDescription = null, tint = colors.brandPrimaryDefault)
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_msg_empty_hint),
            style = CmpTextStyle.Paragraph2,
        )
    }
}

/** Широкое окно: режим приживления, форма адреса, действия слота с инструкцией или подсказка. */
@Composable
private fun SidePanel(state: PairingState, actions: PairingActions, width: Dp, top: Dp) {
    val colors = CmpTheme.colors
    val session = state.pairing
    val pairingSlot = state.slot(session?.slot)
    val selected = state.slot(state.selected)
    CmpColumn(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .background(colors.backgroundSurfaceCard)
            .verticalScroll(rememberScrollState())
            .padding(top = top)
            .padding(horizontal = CmpSpacing.space24, vertical = CmpSpacing.space20),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20),
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
                    style = CmpTextStyle.Headline1,
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
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
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
                .padding(start = CmpSpacing.screenPaddingH, end = CmpSpacing.space8, bottom = CmpSpacing.space24),
        ) {
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                CmpText(
                    text = title,
                    color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    style = CmpTextStyle.Additional0,
                    uppercase = true,
                    modifier = Modifier.weight(1f),
                )
                CmpIconButton(
                    onClick = onDismiss,
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Strings.string.peripherals_common_a11y_close),
                )
            }
            CmpBox(modifier = Modifier.padding(end = CmpSpacing.space16)) { content() }
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
                        style = CmpTextStyle.Headline1,
                        textAlign = TextAlign.Center,
                    )
                },
                text = {
                    CmpText(
                        text = stringResource(Strings.string.peripherals_pairing_msg_delete),
                        style = CmpTextStyle.Paragraph2,
                    )
                },
                confirmButton = {
                    CmpButton(
                        text = stringResource(Strings.string.peripherals_pairing_btn_delete),
                        onClick = actions.onDeleteConfirm,
                        type = CmpButtonType.Danger,
                    )
                },
                dismissButton = {
                    CmpButton(
                        type = CmpButtonType.Tertiary,
                        size = CmpButtonSize.S,
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
                    style = CmpTextStyle.Headline1,
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_error_unsupported_msg),
                    style = CmpTextStyle.Paragraph2,
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
