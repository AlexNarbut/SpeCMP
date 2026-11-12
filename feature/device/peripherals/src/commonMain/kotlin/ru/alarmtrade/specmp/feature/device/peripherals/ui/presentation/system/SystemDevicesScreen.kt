package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_format_subtitle
import ru.alarmtrade.specmp.core.localization.resources.peripherals_common_value_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_error_blink
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_msg_blinking
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_btn_close
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_btn_update
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_error_no_answer
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_error_refused
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_error_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_loading_close
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_loading_enter
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_msg_close
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_msg_confirm
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_msg_enter
import ru.alarmtrade.specmp.core.localization.resources.peripherals_firmware_title_confirm
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pin_msg
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_a11y_pairing
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_btn_pair_device
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_btn_pairing
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_error_poll
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_loading_type
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_msg_select
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_format_name
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
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
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.sheet.CmpBottomSheet
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.GroupHeader
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.SkeletonRow
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.ui
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialog
import specmp.feature.device.peripherals.generated.resources.Res
import specmp.feature.device.peripherals.generated.resources.peripherals_ic_menu_peripheral_devices

private val WideFrom = 760.dp
private val WidePanelFrom = 1100.dp
private val ListMaxWidth = 640.dp
private val CompactMaxWidth = 720.dp
private val ButtonHeight = 56.dp

internal const val SYSTEM_PAIRING_TAG = "peripherals_open_pairing"
internal const val SYSTEM_FIRMWARE_CONFIRM_TAG = "peripherals_firmware_confirm"

@Immutable
internal class SystemDevicesActions(
    val onBack: () -> Unit = {},
    val onOpenPairing: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSelect: (DeviceKey) -> Unit = {},
    val onDetailsDismiss: () -> Unit = {},
    val onShowProblem: () -> Unit = {},
    val onBlink: (DeviceKey) -> Unit = {},
    val onUpdate: (DeviceKey) -> Unit = {},
    val onFirmwareConfirm: () -> Unit = {},
    val onFirmwareDismiss: () -> Unit = {},
    val onPinSubmit: (String) -> Unit = {},
    val onPinDismiss: () -> Unit = {},
)

/**
 * Только BLE: при USB плитку не показывает расширенный монтаж.
 * [onOpenDeviceSearch] — закрыть сессию и открыть поиск.
 */
@Composable
fun SystemDevicesEntry(onBack: () -> Unit, onOpenPairing: () -> Unit, onOpenDeviceSearch: () -> Unit) {
    val viewModel = koinViewModel<SystemDevicesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()
    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is SystemDevicesEffect.Blinking -> scope.launch {
                val name = getString(
                    Strings.string.peripherals_type_format_name,
                    getString(effect.type.ui.name),
                    effect.number,
                )
                snackbar.showSnackbar(message = getString(Strings.string.peripherals_details_msg_blinking, name))
            }

            SystemDevicesEffect.BlinkFailed -> scope.launch {
                snackbar.showSnackbar(message = getString(Strings.string.peripherals_details_error_blink))
            }

            SystemDevicesEffect.OpenDeviceSearch -> onOpenDeviceSearch()
        }
    }
    val actions = remember(viewModel, onBack, onOpenPairing) {
        SystemDevicesActions(
            onBack = onBack,
            onOpenPairing = onOpenPairing,
            onRetry = viewModel::onRetry,
            onSelect = viewModel::onSelect,
            onDetailsDismiss = viewModel::onDetailsDismiss,
            onShowProblem = viewModel::onShowProblem,
            onBlink = viewModel::onBlink,
            onUpdate = viewModel::onUpdate,
            onFirmwareConfirm = viewModel::onFirmwareConfirm,
            onFirmwareDismiss = viewModel::onFirmwareDismiss,
            onPinSubmit = viewModel::onPinSubmit,
            onPinDismiss = viewModel::onPinDismiss,
        )
    }
    SystemDevicesContent(state = state, actions = actions, snackbar = snackbar)
}

@Composable
internal fun SystemDevicesContent(
    state: SystemDevicesState,
    actions: SystemDevicesActions,
    snackbar: CmpSnackbarHostState = rememberCmpSnackbarHostState(),
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val wide = maxWidth >= WideFrom
        val panelWidth = if (maxWidth >= WidePanelFrom) 460.dp else 380.dp
        CmpScaffold(
            topBar = { TopBar(state, actions, wide) },
            snackbarHost = { CmpSnackbarHost(hostState = snackbar) },
        ) { padding ->
            val contentModifier = Modifier.fillMaxSize().padding(padding)
            if (wide) {
                WideLayout(state, actions, panelWidth, contentModifier)
            } else {
                DeviceList(
                    state = state,
                    actions = actions,
                    wide = false,
                    modifier = contentModifier,
                    maxWidth = CompactMaxWidth,
                )
            }
        }
        val details = state.device(state.selected)
        if (!wide && state.detailsOpen && details != null) {
            CmpBottomSheet(onDismissRequest = actions.onDetailsDismiss) {
                DetailsSheetBody(state, details, actions)
            }
        }
    }
    FirmwareDialogs(state, actions)
}

@Composable
private fun TopBar(state: SystemDevicesState, actions: SystemDevicesActions, wide: Boolean) {
    val subtitle = state.deviceName?.let {
        stringResource(
            Strings.string.peripherals_common_format_subtitle,
            it,
            stringResource(Strings.string.peripherals_common_value_bluetooth),
        )
    }
    val showPairing = state.canOpenPairing && (state.devices.isNotEmpty() || !state.firstPassDone)
    CmpTopAppBar(
        title = { CmpText(text = stringResource(Strings.string.peripherals_system_title)) },
        subtitle = subtitle?.let { { CmpText(text = it, style = CmpTextStyle.Caption) } },
        navigationIcon = {
            CmpIconButton(
                onClick = actions.onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.peripherals_common_a11y_back),
            )
        },
        actions = {
            if (showPairing && wide) {
                CmpOutlinedButton(
                    onClick = actions.onOpenPairing,
                    modifier = Modifier.padding(end = 8.dp).height(48.dp),
                ) {
                    CmpIcon(icon = CmpIcons.AddLink, contentDescription = null)
                    CmpText(
                        text = stringResource(Strings.string.peripherals_system_btn_pairing),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            } else if (showPairing) {
                CmpIconButton(
                    onClick = actions.onOpenPairing,
                    icon = CmpIcons.AddLink,
                    contentDescription = stringResource(Strings.string.peripherals_system_a11y_pairing),
                )
            }
        },
    )
}

@Composable
private fun WideLayout(state: SystemDevicesState, actions: SystemDevicesActions, panelWidth: Dp, modifier: Modifier) {
    val theme = cmpThemeColors()
    val devices = state.devices
    // Широкое окно: без выбора после прохода — первое проблемное (или первое) устройство.
    val selected = state.device(state.selected)
        ?: if (state.firstPassDone) state.device(state.summary.focus) ?: devices.firstOrNull() else null
    CmpRow(modifier = modifier) {
        DeviceList(
            state = state.copy(selected = selected?.key),
            actions = actions,
            wide = true,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            maxWidth = ListMaxWidth,
        )
        if ((state.firstPassDone || selected != null) && devices.isNotEmpty()) {
            CmpBox(
                modifier = Modifier
                    .width(panelWidth)
                    .fillMaxHeight()
                    .background(theme.surfaceContainerLow)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            ) {
                if (selected != null) {
                    DeviceDetails(
                        device = selected,
                        blinking = state.blinking == selected.key,
                        onBlink = { actions.onBlink(selected.key) },
                        onUpdate = { actions.onUpdate(selected.key) },
                    )
                } else {
                    CmpText(
                        text = stringResource(Strings.string.peripherals_system_msg_select),
                        color = theme.onSurfaceVariant,
                        style = CmpTextStyle.BodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsSheetBody(state: SystemDevicesState, device: SystemPeripheral, actions: SystemDevicesActions) {
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 8.dp, bottom = 24.dp),
    ) {
        CmpRow(verticalAlignment = Alignment.CenterVertically) {
            CmpText(
                text = stringResource(Strings.string.peripherals_details_title),
                color = cmpThemeColors().onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                style = CmpTextStyle.Body,
                modifier = Modifier.weight(1f),
            )
            CmpIconButton(
                onClick = actions.onDetailsDismiss,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.peripherals_common_a11y_close),
            )
        }
        CmpBox(modifier = Modifier.padding(end = 12.dp)) {
            DeviceDetails(
                device = device,
                blinking = state.blinking == device.key,
                onBlink = { actions.onBlink(device.key) },
                onUpdate = { actions.onUpdate(device.key) },
            )
        }
    }
}

@Composable
private fun DeviceList(
    state: SystemDevicesState,
    actions: SystemDevicesActions,
    wide: Boolean,
    modifier: Modifier,
    maxWidth: Dp,
) {
    val listState = rememberLazyListState()
    val highlight = state.highlight
    LaunchedEffect(highlight) {
        val target = highlight?.key ?: return@LaunchedEffect
        val index = rowIndex(state, target)
        if (index >= 0) listState.animateScrollToItem(index)
    }
    CmpBox(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            modifier = Modifier.widthIn(max = maxWidth).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "header") {
                CmpColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BlockHeader(state.deviceName, state.firmwareVersion, state.bluetoothVersion, wide)
                    when {
                        !state.firstPassDone && !state.pollFailed -> PollProgress(state.polled, state.types.size)

                        state.firstPassDone && state.devices.isNotEmpty() -> Summary(
                            state.summary,
                            actions.onShowProblem,
                        )
                    }
                }
            }
            groups(state, actions, wide)
            tail(state, actions)
        }
    }
}

/** Индекс строки в LazyColumn: шапка, затем по группе — заголовок и строки. */
private fun rowIndex(state: SystemDevicesState, key: DeviceKey): Int {
    var index = 1
    state.groups.forEach { group ->
        index += 1
        group.devices.forEach { device ->
            if (device.key == key) return index
            index += 1
        }
    }
    return -1
}

private fun LazyListScope.groups(state: SystemDevicesState, actions: SystemDevicesActions, wide: Boolean) {
    state.groups.forEach { group ->
        item(key = "group_${group.type}") {
            GroupHeader(
                title = stringResource(group.type.ui.group),
                trailing = group.devices.size.takeIf { it > 1 }?.toString(),
            )
        }
        items(group.devices, key = { "${it.type}_${it.address}" }) { device ->
            DeviceRow(
                device = device,
                selected = wide && state.selected == device.key,
                flashSeq = state.highlight?.takeIf { it.key == device.key }?.seq,
                onClick = { actions.onSelect(device.key) },
            )
        }
    }
}

private fun LazyListScope.tail(state: SystemDevicesState, actions: SystemDevicesActions) {
    when {
        state.pollFailed -> item(key = "error") {
            CmpStateBox(
                icon = CmpIcons.SyncProblem,
                title = stringResource(Strings.string.peripherals_system_error_poll),
                tone = CmpStateTone.Error,
                modifier = Modifier.padding(top = 32.dp),
            ) {
                CmpButton(text = stringResource(Strings.string.peripherals_system_btn_retry), onClick = actions.onRetry)
            }
        }

        !state.firstPassDone -> item(key = "polling") {
            CmpColumn(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(if (state.groups.isEmpty()) SKELETONS_EMPTY else 1) { SkeletonRow() }
                state.pollingType?.let { type ->
                    CmpText(
                        text = stringResource(
                            Strings.string.peripherals_system_loading_type,
                            stringResource(type.ui.name),
                        ),
                        color = cmpThemeColors().onSurfaceVariant,
                        style = CmpTextStyle.Body,
                    )
                }
            }
        }

        state.devices.isEmpty() -> item(key = "empty") { EmptyState(state.canOpenPairing, actions.onOpenPairing) }
    }
}

private const val SKELETONS_EMPTY = 3

@Composable
private fun EmptyState(canOpenPairing: Boolean, onOpenPairing: () -> Unit) {
    val theme = cmpThemeColors()
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CmpBox(
            modifier = Modifier.size(112.dp).background(theme.surfaceContainerHigh, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                painter = painterResource(Res.drawable.peripherals_ic_menu_peripheral_devices),
                contentDescription = null,
                tint = theme.onSurfaceVariant,
                modifier = Modifier.size(72.dp),
            )
        }
        CmpText(
            text = stringResource(Strings.string.peripherals_system_msg_empty),
            style = CmpTextStyle.Title,
            textAlign = TextAlign.Center,
        )
        if (canOpenPairing) {
            CmpButton(onClick = onOpenPairing, modifier = Modifier.height(ButtonHeight).testTag(SYSTEM_PAIRING_TAG)) {
                CmpIcon(icon = CmpIcons.AddLink, contentDescription = null)
                CmpText(
                    text = stringResource(Strings.string.peripherals_system_btn_pair_device),
                    modifier = Modifier.padding(start = 8.dp),
                    style = CmpTextStyle.BodyLarge,
                )
            }
        }
    }
}

@Composable
private fun FirmwareDialogs(state: SystemDevicesState, actions: SystemDevicesActions) {
    state.pin?.let { pin ->
        ServicePinDialog(
            message = stringResource(Strings.string.peripherals_pin_msg),
            state = pin,
            onDismiss = actions.onPinDismiss,
            onSubmit = actions.onPinSubmit,
        )
        return
    }
    val step = state.firmware ?: return
    val device = state.device(step.device)
    val name = device?.let { deviceTitle(it.type, it.number) }.orEmpty()
    when (step) {
        is FirmwareStep.Confirm -> CmpDialog(
            onDismissRequest = actions.onFirmwareDismiss,
            icon = CmpIcons.SystemUpdate.imageVector(),
            title = {
                CmpText(
                    text = stringResource(Strings.string.peripherals_firmware_title_confirm, name),
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                CmpText(
                    text = stringResource(Strings.string.peripherals_firmware_msg_confirm),
                    style = CmpTextStyle.BodyLarge,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.peripherals_firmware_btn_update),
                    onClick = actions.onFirmwareConfirm,
                    modifier = Modifier.height(48.dp).testTag(SYSTEM_FIRMWARE_CONFIRM_TAG),
                )
            },
            dismissButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.peripherals_firmware_btn_cancel),
                    onClick = actions.onFirmwareDismiss,
                )
            },
        )

        is FirmwareStep.Waiting -> BusyDialog(
            title = stringResource(Strings.string.peripherals_firmware_loading_enter),
            text = stringResource(Strings.string.peripherals_firmware_msg_enter, name),
        )

        is FirmwareStep.Closing -> BusyDialog(
            title = stringResource(Strings.string.peripherals_firmware_loading_close),
            text = stringResource(Strings.string.peripherals_firmware_msg_close),
        )

        is FirmwareStep.Failed -> CmpDialog(
            onDismissRequest = actions.onFirmwareDismiss,
            icon = CmpIcons.ErrorOutline.imageVector(),
            title = {
                CmpText(
                    text = stringResource(Strings.string.peripherals_firmware_error_title),
                    textAlign = TextAlign.Center,
                )
            },
            text = {
                CmpText(
                    text = stringResource(
                        when (step.reason) {
                            FirmwareFailure.NO_ANSWER -> Strings.string.peripherals_firmware_error_no_answer
                            FirmwareFailure.REFUSED -> Strings.string.peripherals_firmware_error_refused
                        },
                    ),
                    style = CmpTextStyle.BodyLarge,
                )
            },
            confirmButton = {
                CmpButton(
                    text = stringResource(Strings.string.peripherals_firmware_btn_retry),
                    onClick = actions.onFirmwareConfirm,
                )
            },
            dismissButton = {
                CmpTextButton(
                    text = stringResource(Strings.string.peripherals_firmware_btn_close),
                    onClick = actions.onFirmwareDismiss,
                )
            },
        )
    }
}

/** Блокирующее ожидание: без кнопок, закрыть нельзя. */
@Composable
private fun BusyDialog(title: String, text: String) {
    CmpDialog(
        onDismissRequest = {},
        confirmButton = {},
        title = {
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                CmpCircularProgressIndicator(modifier = Modifier.size(56.dp), strokeWidth = 5.dp)
                CmpText(text = title, textAlign = TextAlign.Center)
            }
        },
        text = { CmpText(text = text, style = CmpTextStyle.BodyLarge, textAlign = TextAlign.Center) },
    )
}
