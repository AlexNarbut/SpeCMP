package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHost
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpTopAppBar
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.scaffold.CmpScaffold
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem

private enum class DeviceSelectLayout { Compact, Stacked, Split }

@Composable
internal fun DeviceSelectionScreen(
    onSessionCreate: () -> Unit,
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
            ScanDeviceEffect.SessionCreated -> onSessionCreate()

            ScanDeviceEffect.Close -> onClose?.invoke()

            ScanDeviceEffect.ConnectionFailed -> scope.launch { snackbarHostState.showSnackbar(connectionFailedText) }

            ScanDeviceEffect.BoundToAnotherPhone -> scope.launch {
                snackbarHostState.showSnackbar(boundToAnotherPhoneText)
            }

            ScanDeviceEffect.PairingRequired -> scope.launch { snackbarHostState.showSnackbar(pairingRequiredText) }
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
                title = { CmpText(text = stringResource(Strings.string.deviceselect_common_title_screen)) },
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
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
            val layout = when {
                maxWidth < 600.dp -> DeviceSelectLayout.Compact
                maxWidth > maxHeight * 1.1f -> DeviceSelectLayout.Split
                else -> DeviceSelectLayout.Stacked
            }
            StepBody(
                state = state,
                layout = layout,
                onRetry = actions.onRetry,
                onOpenSettings = actions.onOpenSettings,
                onOpenBluetoothSettings = actions.onOpenBluetoothSettings,
                onOpenLocationSettings = actions.onOpenLocationSettings,
                onSelect = { item -> actions.onSelect(item, layout == DeviceSelectLayout.Split) },
                onConnectSelectedDevice = actions.onConnectSelectedDevice,
                onCancelConnecting = actions.onCancelConnecting,
                initialInfoId = initialInfoId,
            )
        }
    }
}

@Composable
private fun StepBody(
    state: ScanDeviceState,
    layout: DeviceSelectLayout,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBluetoothSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelectedDevice: () -> Unit,
    onCancelConnecting: () -> Unit,
    initialInfoId: String?,
) {
    when (val step = state.step) {
        ScanStep.PermissionCheck -> CenterState { CmpCircularProgressIndicator() }

        is ScanStep.Denied -> DeniedBody(state.requirements, step.canOpenSettings, onOpenSettings, onRetry)

        ScanStep.AdapterOff -> OffStateBody(
            icon = CmpIcons.BluetoothDisabled,
            title = stringResource(Strings.string.deviceselect_adapter_title),
            subtitle = stringResource(Strings.string.deviceselect_adapter_subtitle),
            primaryText = stringResource(Strings.string.deviceselect_common_btn_open_bluetooth),
            primaryIcon = CmpIcons.Bluetooth,
            onPrimary = onOpenBluetoothSettings,
            onRetry = onRetry,
        )

        ScanStep.LocationOff -> OffStateBody(
            icon = CmpIcons.LocationOn,
            title = stringResource(Strings.string.deviceselect_location_title),
            subtitle = stringResource(Strings.string.deviceselect_location_subtitle),
            primaryText = stringResource(Strings.string.deviceselect_common_btn_open_location),
            primaryIcon = CmpIcons.LocationOn,
            onPrimary = onOpenLocationSettings,
            onRetry = onRetry,
        )

        ScanStep.Scanning -> ScanningOrListBody(state, layout, onSelect, onConnectSelectedDevice, initialInfoId)

        is ScanStep.Connecting -> ConnectingBody(step.device, onCancelConnecting)
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
            IconedButton(
                text = stringResource(Strings.string.deviceselect_common_btn_open_settings),
                icon = CmpIcons.Settings,
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            IconedButton(
                text = stringResource(Strings.string.deviceselect_common_btn_retry),
                icon = CmpIcons.Refresh,
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
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
        IconedButton(
            text = primaryText,
            icon = primaryIcon,
            onClick = onPrimary,
            modifier = Modifier.fillMaxWidth(),
        )
        CmpOutlinedButton(
            text = stringResource(Strings.string.deviceselect_common_btn_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
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
private fun EmptyBody(supportedTransports: Set<DeviceTransport>) {
    val colors = cmpThemeColors()
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
        CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpCircularProgressIndicator(modifier = Modifier.size(18.dp))
            CmpText(
                text = stringResource(Strings.string.deviceselect_scan_loading_searching),
                fontSize = DsType.LabelLarge,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConnectingBody(device: DeviceUiItem, onCancel: () -> Unit) {
    val colors = cmpThemeColors()
    CenterState {
        Hero(icon = device.typeIcon)
        CmpColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CmpText(
                text = stringResource(Strings.string.deviceselect_connecting_title),
                fontSize = DsType.HeadlineSmall,
                fontWeight = FontWeight.Medium,
            )
            CmpText(text = device.name, fontSize = DsType.TitleMedium, fontWeight = FontWeight.Medium)
            CmpText(text = device.address, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
        }
        CmpCircularProgressIndicator()
        CmpOutlinedButton(text = stringResource(Strings.string.deviceselect_common_btn_cancel), onClick = onCancel)
    }
}

@Composable
private fun ScanningOrListBody(
    state: ScanDeviceState,
    layout: DeviceSelectLayout,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelectedDevice: () -> Unit,
    initialInfoId: String?,
) {
    when (val devices = state.devices) {
        is LCEState.Content ->
            if (devices.content.isEmpty()) {
                if (state.searchedLongEnough) {
                    EmptyBody(
                        state.supportedTransports,
                    )
                } else {
                    ScanningBody(state.supportedTransports)
                }
            } else {
                DeviceList(devices.content, state.selectedId, layout, onSelect, onConnectSelectedDevice, initialInfoId)
            }

        is LCEState.Error -> ScanningBody(state.supportedTransports)

        else -> if (state.searchedLongEnough) {
            EmptyBody(
                state.supportedTransports,
            )
        } else {
            ScanningBody(state.supportedTransports)
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<DeviceUiItem>,
    selectedId: String?,
    layout: DeviceSelectLayout,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelectedDevice: () -> Unit,
    initialInfoId: String?,
) {
    val tileSize = if (layout == DeviceSelectLayout.Compact) 44 else 48

    if (layout == DeviceSelectLayout.Split) {
        val colors = cmpThemeColors()
        val selected = devices.firstOrNull { it.id == selectedId }
        CmpRow(
            modifier = Modifier.fillMaxSize().widthIn(max = 1100.dp).padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CmpColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                ListHeader(devices.size)
                CmpColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Порядок приходит из скана и меняется между эмитами — строка привязана к устройству, не к позиции.
                    devices.forEach { d ->
                        key(d.id) { DeviceRow(d, d.id == selectedId, { onSelect(d) }, tileSize, showMode = true) }
                    }
                }
            }
            CmpSurface(modifier = Modifier.width(1.dp).fillMaxHeight(), color = colors.outlineVariant) {}
            CmpBox(modifier = Modifier.weight(1f).widthIn(max = 420.dp).fillMaxHeight()) {
                DetailPanel(selected, onConnectSelectedDevice)
            }
        }
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

    val maxW = if (layout == DeviceSelectLayout.Stacked) 620.dp else Int.MAX_VALUE.dp
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(
            rememberScrollState(),
        ).padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = maxW).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ListHeader(devices.size)
            CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
private fun ListHeader(count: Int) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = stringResource(Strings.string.deviceselect_list_label_count, count),
            fontSize = DsType.TitleMedium,
            fontWeight = FontWeight.Medium,
        )
        CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpCircularProgressIndicator(modifier = Modifier.size(16.dp))
            CmpText(
                text = stringResource(Strings.string.deviceselect_list_loading_searching_short),
                fontSize = DsType.LabelLarge,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DetailPanel(device: DeviceUiItem?, onConnect: () -> Unit) {
    val colors = cmpThemeColors()
    if (device == null) {
        CmpColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CmpSurface(modifier = Modifier.size(72.dp), shape = CircleShape, color = colors.surfaceVariant) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpIcon(
                        icon = CmpIcons.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
            CmpText(
                text = stringResource(Strings.string.deviceselect_detail_msg_empty),
                fontSize = DsType.TitleMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
        return
    }
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DeviceTile(icon = device.typeIcon, sizeDp = 128)
            CmpColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CmpText(
                    text = device.name,
                    fontSize = DsType.HeadlineSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                )
                TransportChip(transport = device.transport)
            }
            DeviceInfoRows(deviceInfoRows(device))
            deviceInfoNote(device)?.let {
                CmpText(
                    text = it,
                    fontSize = DsType.LabelSmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            IconedButton(
                text = stringResource(Strings.string.deviceselect_common_btn_connect),
                icon = CmpIcons.Link,
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
