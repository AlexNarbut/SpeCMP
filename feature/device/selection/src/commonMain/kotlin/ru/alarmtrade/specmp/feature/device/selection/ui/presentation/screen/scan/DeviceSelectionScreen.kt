package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_adapter_subtitle
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_adapter_title
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_a11y_back
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_connect
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_open_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_open_location
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_open_settings
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_error_bond_remove_device
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_error_connection_failed
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_error_pairing_required
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_title_screen
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_connecting_title
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_denied_subtitle
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_denied_title
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_msg_empty
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_empty_item_ble
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_empty_item_both
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_empty_item_usb
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_empty_subtitle
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_empty_title
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_fix_item_android
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_fix_item_ios
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_list_label_count
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_list_loading_searching_short
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_location_subtitle
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_location_title
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_scan_loading_searching
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_scanning_subtitle_ble
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_scanning_subtitle_both
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_scanning_subtitle_usb
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_scanning_title
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.ui.effect.collectSideEffect
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarTone
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem

private enum class DeviceSelectLayout { Compact, Stacked, Split }

private val SplitMaxWidth = 1100.dp
private val DetailMaxWidth = 420.dp
private val StackedMaxWidth = 620.dp
private val DetailTileSize = 128.dp
private val DetailEmptyIconCircle = 72.dp
private val SmallProgressStroke = 2.dp

@Composable
internal fun DeviceSelectionScreen(
    onSessionCreate: (ConnectionTransport) -> Unit,
    onClose: (() -> Unit)?,
    serialEnabled: Boolean,
    alarmsOnly: Boolean,
) {
    val viewModel = koinViewModel<ScanDeviceViewModel>(parameters = { parametersOf(serialEnabled, alarmsOnly) })
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = rememberCmpSnackbarHostState()
    val scope = rememberCoroutineScope()

    val connectionFailedText = stringResource(Strings.string.deviceselect_common_error_connection_failed)
    val boundToAnotherPhoneText = stringResource(Strings.string.deviceselect_common_error_bond_remove_device)
    val pairingRequiredText = stringResource(Strings.string.deviceselect_common_error_pairing_required)

    viewModel.sideEffect.collectSideEffect { effect ->
        when (effect) {
            is ScanDeviceEffect.SessionCreated -> onSessionCreate(effect.transport)

            ScanDeviceEffect.Close -> onClose?.invoke()

            ScanDeviceEffect.ConnectionFailed -> scope.launch {
                snackbarHostState.showSnackbar(connectionFailedText, tone = CmpSnackbarTone.Error)
            }

            ScanDeviceEffect.BoundToAnotherPhone -> scope.launch {
                snackbarHostState.showSnackbar(boundToAnotherPhoneText, tone = CmpSnackbarTone.Error)
            }

            ScanDeviceEffect.PairingRequired -> scope.launch {
                snackbarHostState.showSnackbar(pairingRequiredText, tone = CmpSnackbarTone.Info)
            }
        }
    }

    // «Назад» решает VM: во время подключения — отмена, иначе — выход.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = viewModel::onBack,
    )

    DeviceSelectionContent(
        state = state,
        actions = DeviceSelectionActions(
            onBack = viewModel::onBack,
            onRetry = viewModel::onRetry,
            onOpenSettings = viewModel::onOpenAppSettings,
            onOpenBluetoothSettings = viewModel::onOpenBluetoothSettings,
            onOpenLocationSettings = viewModel::onOpenLocationSettings,
            onSelect = viewModel::onDeviceSelected,
            onConnectSelectedDevice = viewModel::onConnectSelected,
            onCancelConnecting = viewModel::onCancelConnecting,
        ),
        snackbarHost = { CmpSnackbarHost(snackbarHostState) },
        showBack = onClose != null,
    )
}

internal class DeviceSelectionActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onOpenSettings: () -> Unit = {},
    val onOpenBluetoothSettings: () -> Unit = {},
    val onOpenLocationSettings: () -> Unit = {},
    val onSelect: (DeviceUiItem, Boolean) -> Unit = { _, _ -> },
    val onConnectSelectedDevice: () -> Unit = {},
    val onCancelConnecting: () -> Unit = {},
)

@Composable
internal fun DeviceSelectionContent(
    state: ScanDeviceState,
    actions: DeviceSelectionActions,
    snackbarHost: @Composable () -> Unit = {},
    initialInfoId: String? = null,
    showBack: Boolean = true,
) {
    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = stringResource(Strings.string.deviceselect_common_title_screen),
                navigationIcon = {
                    if (showBack) {
                        CmpIconButton(
                            onClick = actions.onBack,
                            icon = CmpIcons.ArrowBack,
                            contentDescription = stringResource(Strings.string.deviceselect_common_a11y_back),
                        )
                    }
                },
            )
        },
        snackbarHost = snackbarHost,
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val contentHeight = maxHeight - padding.calculateTopPadding() - padding.calculateBottomPadding()
            val layout = when {
                maxWidth < CmpBreakpoints.Medium -> DeviceSelectLayout.Compact
                maxWidth > contentHeight * 1.1f -> DeviceSelectLayout.Split
                else -> DeviceSelectLayout.Stacked
            }
            StepBody(
                state = state,
                layout = layout,
                padding = padding,
                actions = actions,
                onSelect = { item -> actions.onSelect(item, layout == DeviceSelectLayout.Split) },
                initialInfoId = initialInfoId,
            )
        }
    }
}

@Composable
private fun StepBody(
    state: ScanDeviceState,
    layout: DeviceSelectLayout,
    padding: PaddingValues,
    actions: DeviceSelectionActions,
    onSelect: (DeviceUiItem) -> Unit,
    initialInfoId: String?,
) {
    val devices = (state.devices as? LCEState.Content)?.content.orEmpty()
    if (state.step == ScanStep.Scanning && devices.isNotEmpty()) {
        DeviceList(
            devices = devices,
            selectedId = state.selectedId,
            layout = layout,
            padding = padding,
            onSelect = onSelect,
            onConnectSelectedDevice = actions.onConnectSelectedDevice,
            initialInfoId = initialInfoId,
        )
    } else {
        CmpBox(modifier = Modifier.fillMaxSize().padding(padding)) {
            StateBody(state, actions)
        }
    }
}

@Composable
private fun StateBody(state: ScanDeviceState, actions: DeviceSelectionActions) {
    when (val step = state.step) {
        ScanStep.PermissionCheck -> CenterState { CmpCircularProgressIndicator() }

        is ScanStep.Denied -> DeniedBody(
            state.requirements,
            step.canOpenSettings,
            actions.onOpenSettings,
            actions.onRetry,
        )

        ScanStep.AdapterOff -> OffStateBody(
            icon = CmpIcons.BluetoothDisabled,
            title = stringResource(Strings.string.deviceselect_adapter_title),
            subtitle = stringResource(Strings.string.deviceselect_adapter_subtitle),
            primaryText = stringResource(Strings.string.deviceselect_common_btn_open_bluetooth),
            primaryIcon = CmpIcons.Bluetooth,
            onPrimary = actions.onOpenBluetoothSettings,
            onRetry = actions.onRetry,
        )

        ScanStep.LocationOff -> OffStateBody(
            icon = CmpIcons.LocationOn,
            title = stringResource(Strings.string.deviceselect_location_title),
            subtitle = stringResource(Strings.string.deviceselect_location_subtitle),
            primaryText = stringResource(Strings.string.deviceselect_common_btn_open_location),
            primaryIcon = CmpIcons.LocationOn,
            onPrimary = actions.onOpenLocationSettings,
            onRetry = actions.onRetry,
        )

        ScanStep.Scanning -> if (state.searchedLongEnough && state.devices !is LCEState.Error) {
            EmptyBody(state.supportedTransports)
        } else {
            ScanningBody(state.supportedTransports)
        }

        is ScanStep.Connecting -> ConnectingBody(step.device, actions.onCancelConnecting)
    }
}

@Composable
private fun DeniedBody(
    requirements: List<DeviceAccessRequirement>,
    canOpenSettings: Boolean,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
) {
    val fixItems = if (requirements.contains(DeviceAccessRequirement.USB)) {
        stringArrayResource(Strings.array.deviceselect_fix_item_android)
    } else {
        stringArrayResource(Strings.array.deviceselect_fix_item_ios)
    }
    CenterState {
        Hero(icon = CmpIcons.Lock, tone = HeroTone.Error)
        StateHeadline(
            stringResource(Strings.string.deviceselect_denied_title),
            stringResource(Strings.string.deviceselect_denied_subtitle),
        )
        FixSteps(items = fixItems, done = false)
        if (canOpenSettings) {
            CmpButton(
                text = stringResource(Strings.string.deviceselect_common_btn_open_settings),
                icon = CmpIcons.Settings,
                onClick = onOpenSettings,
                fullWidth = true,
            )
        } else {
            CmpButton(
                text = stringResource(Strings.string.deviceselect_common_btn_retry),
                icon = CmpIcons.Refresh,
                onClick = onRetry,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun OffStateBody(
    icon: CmpIcons,
    title: String,
    subtitle: String,
    primaryText: String,
    primaryIcon: CmpIcons,
    onPrimary: () -> Unit,
    onRetry: () -> Unit,
) {
    CenterState {
        Hero(icon = icon, tone = HeroTone.Neutral)
        StateHeadline(title, subtitle)
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
            CmpButton(text = primaryText, icon = primaryIcon, onClick = onPrimary, fullWidth = true)
            CmpButton(
                text = stringResource(Strings.string.deviceselect_common_btn_retry),
                onClick = onRetry,
                type = CmpButtonType.Secondary,
                fullWidth = true,
            )
        }
    }
}

private fun Set<DeviceTransport>.hasCable(): Boolean = DeviceTransport.USB in this || DeviceTransport.SERIAL in this

@Composable
private fun ScanningBody(supportedTransports: Set<DeviceTransport>) {
    val hasBle = supportedTransports.contains(DeviceTransport.BLE)
    val hasUsb = supportedTransports.hasCable()
    val (icon, subtitle) = when {
        hasBle && !hasUsb -> CmpIcons.BluetoothSearching to Strings.string.deviceselect_scanning_subtitle_ble
        hasUsb && !hasBle -> CmpIcons.Usb to Strings.string.deviceselect_scanning_subtitle_usb
        else -> CmpIcons.Sensors to Strings.string.deviceselect_scanning_subtitle_both
    }
    CenterState {
        Hero(icon = icon)
        StateHeadline(stringResource(Strings.string.deviceselect_scanning_title), stringResource(subtitle))
        CmpCircularProgressIndicator()
    }
}

@Composable
private fun SearchingLabel(text: String) {
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconS), strokeWidth = SmallProgressStroke)
        CmpText(text = text, style = CmpTextStyle.Headline3, color = CmpTheme.colors.foregroundOnDarkSecondaryDefault)
    }
}

@Composable
private fun EmptyBody(supportedTransports: Set<DeviceTransport>) {
    val hasBle = supportedTransports.contains(DeviceTransport.BLE)
    val hasUsb = supportedTransports.hasCable()
    val hints = when {
        hasBle && hasUsb -> stringArrayResource(Strings.array.deviceselect_empty_item_both)
        hasUsb -> stringArrayResource(Strings.array.deviceselect_empty_item_usb)
        else -> stringArrayResource(Strings.array.deviceselect_empty_item_ble)
    }
    CenterState {
        Hero(icon = CmpIcons.SearchOff, tone = HeroTone.Neutral)
        StateHeadline(
            stringResource(Strings.string.deviceselect_empty_title),
            stringResource(Strings.string.deviceselect_empty_subtitle),
        )
        FixSteps(items = hints, done = true)
        SearchingLabel(stringResource(Strings.string.deviceselect_scan_loading_searching))
    }
}

@Composable
private fun ConnectingBody(device: DeviceUiItem, onCancel: () -> Unit) {
    CenterState {
        Hero(icon = device.typeIcon)
        CmpColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        ) {
            CmpText(
                text = stringResource(Strings.string.deviceselect_connecting_title),
                style = CmpTextStyle.Headline1,
                textAlign = TextAlign.Center,
            )
            CmpText(text = device.name, style = CmpTextStyle.Headline2, textAlign = TextAlign.Center)
            CmpText(
                text = device.address,
                style = CmpTextStyle.Paragraph3,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        }
        CmpCircularProgressIndicator()
        CmpButton(
            text = stringResource(Strings.string.deviceselect_common_btn_cancel),
            onClick = onCancel,
            type = CmpButtonType.Secondary,
        )
    }
}

@Composable
private fun DeviceList(
    devices: List<DeviceUiItem>,
    selectedId: String?,
    layout: DeviceSelectLayout,
    padding: PaddingValues,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelectedDevice: () -> Unit,
    initialInfoId: String?,
) {
    val tileSize = if (layout == DeviceSelectLayout.Compact) CmpSize.iconXl else CmpSize.touchMin

    if (layout == DeviceSelectLayout.Split) {
        SplitList(devices, selectedId, padding, tileSize, onSelect, onConnectSelectedDevice)
        return
    }

    // Диалог «⋮» — состояние экрана; устройство пропало из списка — диалог закрывается сам.
    var infoId by rememberSaveable { mutableStateOf(initialInfoId) }
    devices.firstOrNull { it.id == infoId }?.let { info ->
        DeviceInfoDialog(
            item = info,
            onDismiss = { infoId = null },
            onConnect = {
                infoId = null
                onSelect(info)
            },
        )
    }

    val layoutDirection = LocalLayoutDirection.current
    val stacked = layout == DeviceSelectLayout.Stacked
    val padH = if (stacked) CmpSpacing.screenPaddingHMedium else CmpSpacing.screenPaddingH
    CmpColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection),
            )
            .verticalScroll(rememberScrollState())
            .padding(
                start = padH,
                end = padH,
                top = padding.calculateTopPadding() + CmpSpacing.screenPaddingTop,
                bottom = padding.calculateBottomPadding() + CmpSpacing.screenPaddingBottom,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = if (stacked) StackedMaxWidth else Dp.Infinity).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            ListHeader(devices.size)
            CmpColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                devices.forEach { d ->
                    key(d.id) {
                        DeviceRow(d, selected = false, onClick = { onSelect(d) }, tileSize = tileSize, onInfo = {
                            infoId = d.id
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitList(
    devices: List<DeviceUiItem>,
    selectedId: String?,
    padding: PaddingValues,
    tileSize: Dp,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelectedDevice: () -> Unit,
) {
    val selected = devices.firstOrNull { it.id == selectedId }
    CmpRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .widthIn(max = SplitMaxWidth)
            .padding(horizontal = CmpSpacing.screenPaddingHMedium, vertical = CmpSpacing.space24),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
    ) {
        CmpColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            ListHeader(devices.size)
            CmpColumn(
                modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                // Порядок приходит из скана и меняется между эмитами — строка привязана к устройству, не к позиции.
                devices.forEach { d ->
                    key(d.id) { DeviceRow(d, d.id == selectedId, { onSelect(d) }, tileSize, showMode = true) }
                }
            }
        }
        CmpVerticalDivider(modifier = Modifier.fillMaxHeight())
        CmpBox(modifier = Modifier.weight(1f).widthIn(max = DetailMaxWidth).fillMaxHeight()) {
            DetailPanel(selected, onConnectSelectedDevice)
        }
    }
}

@Composable
private fun ListHeader(count: Int) {
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.deviceselect_list_label_count, count),
            style = CmpTextStyle.Headline2,
            modifier = Modifier.weight(1f),
        )
        SearchingLabel(stringResource(Strings.string.deviceselect_list_loading_searching_short))
    }
}

@Composable
private fun DetailPanel(device: DeviceUiItem?, onConnect: () -> Unit) {
    val colors = CmpTheme.colors
    if (device == null) {
        CmpColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16, Alignment.CenterVertically),
        ) {
            CmpBox(
                modifier = Modifier
                    .size(DetailEmptyIconCircle)
                    .background(colors.backgroundSurfaceHighlight, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = CmpIcons.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(CmpSize.iconL),
                    tint = colors.foregroundOnDarkSecondaryDefault,
                )
            }
            CmpText(
                text = stringResource(Strings.string.deviceselect_detail_msg_empty),
                style = CmpTextStyle.Headline2,
                color = colors.foregroundOnDarkSecondaryDefault,
                textAlign = TextAlign.Center,
            )
        }
        return
    }
    CmpCard(modifier = Modifier.fillMaxWidth()) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(CmpSpacing.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            DeviceTile(icon = device.typeIcon, size = DetailTileSize)
            CmpColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
            ) {
                CmpText(
                    text = device.name,
                    style = CmpTextStyle.Headline1,
                    textAlign = TextAlign.Center,
                )
                TransportChip(transport = device.transport)
            }
            DeviceInfoRows(deviceInfoRows(device))
            deviceInfoNote(device)?.let {
                CmpText(
                    text = it,
                    style = CmpTextStyle.Paragraph3,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    textAlign = TextAlign.Center,
                )
            }
            CmpButton(
                text = stringResource(Strings.string.deviceselect_common_btn_connect),
                icon = CmpIcons.Link,
                onClick = onConnect,
                fullWidth = true,
            )
        }
    }
}
