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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
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
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionResult
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import specmp.feature.device.selection_ui.generated.resources.Res
import specmp.feature.device.selection_ui.generated.resources.device_select_a11y_back
import specmp.feature.device.selection_ui.generated.resources.device_select_adapter_subtitle
import specmp.feature.device.selection_ui.generated.resources.device_select_adapter_title
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_connect
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_cancel
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_open_bluetooth
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_open_location
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_open_settings
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_retry
import specmp.feature.device.selection_ui.generated.resources.device_select_connecting_title
import specmp.feature.device.selection_ui.generated.resources.device_select_denied_subtitle
import specmp.feature.device.selection_ui.generated.resources.device_select_denied_title
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_empty
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_address
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_link
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_mac
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_ble_1
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_ble_2
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_ble_3
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_both_1
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_both_2
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_both_3
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_subtitle
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_usb_1
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_usb_2
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_usb_3
import specmp.feature.device.selection_ui.generated.resources.device_select_empty_title
import specmp.feature.device.selection_ui.generated.resources.device_select_error_bond_remove_device
import specmp.feature.device.selection_ui.generated.resources.device_select_error_bond_remove_phone
import specmp.feature.device.selection_ui.generated.resources.device_select_error_connection_failed
import specmp.feature.device.selection_ui.generated.resources.device_select_fix_android_1
import specmp.feature.device.selection_ui.generated.resources.device_select_fix_android_2
import specmp.feature.device.selection_ui.generated.resources.device_select_fix_ios_1
import specmp.feature.device.selection_ui.generated.resources.device_select_list_count
import specmp.feature.device.selection_ui.generated.resources.device_select_list_searching_short
import specmp.feature.device.selection_ui.generated.resources.device_select_location_subtitle
import specmp.feature.device.selection_ui.generated.resources.device_select_location_title
import specmp.feature.device.selection_ui.generated.resources.device_select_scanning_subtitle
import specmp.feature.device.selection_ui.generated.resources.device_select_scanning_title
import specmp.feature.device.selection_ui.generated.resources.device_select_searching
import specmp.feature.device.selection_ui.generated.resources.device_select_title_screen

private enum class DeviceSelectLayout { Compact, Stacked, Split }

/**
 * Публичный вход фичи поиска устройства. Компонует ViewModel, permissions-контроллер,
 * one-shot эффекты и адаптивную раскладку (compact / stacked / split).
 */
@Composable
internal fun DeviceSelectionScreen(
    onDeviceConnected: (DeviceConnectionResult) -> Unit,
    onClose: () -> Unit,
) {
    val viewModel = koinViewModel<ScanDeviceViewModel>()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = rememberCmpSnackbarHostState()

    val connectionFailedText = stringResource(Res.string.device_select_error_connection_failed)
    val bondRemovePhoneText = stringResource(Res.string.device_select_error_bond_remove_phone)
    val bondRemoveDeviceText = stringResource(Res.string.device_select_error_bond_remove_device)

    viewModel.effect.collectSideEffect { effect ->
        when (effect) {
            is ScanDeviceEffect.Connected -> onDeviceConnected(effect.result)
            ScanDeviceEffect.Close -> onClose()
            ScanDeviceEffect.ConnectionFailed -> snackbarHostState.showSnackbar(connectionFailedText)
            is ScanDeviceEffect.BondConflict -> snackbarHostState.showSnackbar(
                when (effect.kind) {
                    BondConflictKind.REMOVE_FROM_PHONE -> bondRemovePhoneText
                    BondConflictKind.REMOVE_FROM_DEVICE -> bondRemoveDeviceText
                },
            )
        }
    }

    // «Назад» решает VM: во время подключения — отмена, иначе — выход.
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        onBackCompleted = viewModel::onBack,
    )

    CmpScaffold(
        topBar = {
            CmpTopAppBar(
                title = { CmpText(text = stringResource(Res.string.device_select_title_screen)) },
                navigationIcon = {
                    CmpIconButton(
                        onClick = viewModel::onBack,
                        icon = CmpIcons.ArrowBack,
                        contentDescription = stringResource(Res.string.device_select_a11y_back),
                    )
                },
            )
        },
        snackbarHost = { CmpSnackbarHost(snackbarHostState) },
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
                onRetry = viewModel::onRetry,
                onOpenSettings = viewModel::openAppSettings,
                onOpenBluetoothSettings = viewModel::onOpenBluetoothSettings,
                onOpenLocationSettings = viewModel::onOpenLocationSettings,
                onSelect = { item -> viewModel.onDeviceSelected(item, isSplit = layout == DeviceSelectLayout.Split) },
                onConnectSelected = viewModel::onConnectSelected,
                onCancelConnecting = viewModel::onCancelConnecting,
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
    onConnectSelected: () -> Unit,
    onCancelConnecting: () -> Unit,
) {
    when (val step = state.step) {
        ScanStep.PermissionCheck -> CenterState { CmpCircularProgressIndicator() }

        is ScanStep.Denied -> DeniedBody(state.requirements, step.canOpenSettings, onOpenSettings, onRetry)

        ScanStep.AdapterOff -> OffStateBody(
            icon = CmpIcons.BluetoothDisabled,
            title = stringResource(Res.string.device_select_adapter_title),
            subtitle = stringResource(Res.string.device_select_adapter_subtitle),
            primaryText = stringResource(Res.string.device_select_btn_open_bluetooth),
            primaryIcon = CmpIcons.Bluetooth,
            onPrimary = onOpenBluetoothSettings,
            onRetry = onRetry,
        )

        ScanStep.LocationOff -> OffStateBody(
            icon = CmpIcons.LocationOn,
            title = stringResource(Res.string.device_select_location_title),
            subtitle = stringResource(Res.string.device_select_location_subtitle),
            primaryText = stringResource(Res.string.device_select_btn_open_location),
            primaryIcon = CmpIcons.LocationOn,
            onPrimary = onOpenLocationSettings,
            onRetry = onRetry,
        )

        ScanStep.Scanning -> ScanningOrListBody(state, layout, onSelect, onConnectSelected)

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
        listOf(stringResource(Res.string.device_select_fix_android_1), stringResource(Res.string.device_select_fix_android_2))
    } else {
        listOf(stringResource(Res.string.device_select_fix_ios_1))
    }
    CenterState {
        Hero(icon = CmpIcons.Lock, tone = HeroTone.Error)
        StateHeadline(stringResource(Res.string.device_select_denied_title), stringResource(Res.string.device_select_denied_subtitle))
        FixSteps(items = fixItems, done = false)
        if (canOpenSettings) {
            IconedButton(
                text = stringResource(Res.string.device_select_btn_open_settings),
                icon = CmpIcons.Settings,
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            IconedButton(
                text = stringResource(Res.string.device_select_btn_retry),
                icon = CmpIcons.Refresh,
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Состояние «что-то выключено» (Bluetooth / геолокация): открыть настройки + повторить. */
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
            text = stringResource(Res.string.device_select_btn_retry),
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ScanningBody() {
    CenterState {
        Hero(icon = CmpIcons.BluetoothSearching)
        StateHeadline(stringResource(Res.string.device_select_scanning_title), stringResource(Res.string.device_select_scanning_subtitle))
        CmpCircularProgressIndicator()
    }
}

@Composable
private fun EmptyBody(supportedTransports: Set<DeviceTransport>) {
    val colors = cmpThemeColors()
    val hasBle = supportedTransports.contains(DeviceTransport.BLE)
    val hasUsb = supportedTransports.contains(DeviceTransport.USB)
    val hints = when {
        hasBle && hasUsb -> listOf(
            stringResource(Res.string.device_select_empty_both_1),
            stringResource(Res.string.device_select_empty_both_2),
            stringResource(Res.string.device_select_empty_both_3),
        )
        hasUsb -> listOf(
            stringResource(Res.string.device_select_empty_usb_1),
            stringResource(Res.string.device_select_empty_usb_2),
            stringResource(Res.string.device_select_empty_usb_3),
        )
        else -> listOf(
            stringResource(Res.string.device_select_empty_ble_1),
            stringResource(Res.string.device_select_empty_ble_2),
            stringResource(Res.string.device_select_empty_ble_3),
        )
    }
    CenterState {
        Hero(icon = CmpIcons.SearchOff, tone = HeroTone.Neutral)
        StateHeadline(stringResource(Res.string.device_select_empty_title), stringResource(Res.string.device_select_empty_subtitle))
        FixSteps(items = hints, done = true)
        CmpRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpCircularProgressIndicator(modifier = Modifier.size(18.dp))
            CmpText(text = stringResource(Res.string.device_select_searching), fontSize = DsType.LabelLarge, color = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun ConnectingBody(device: DeviceUiItem, onCancel: () -> Unit) {
    val colors = cmpThemeColors()
    CenterState {
        Hero(icon = device.typeIcon)
        CmpColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(text = stringResource(Res.string.device_select_connecting_title), fontSize = DsType.HeadlineSmall, fontWeight = FontWeight.Medium)
            CmpText(text = device.name, fontSize = DsType.TitleMedium, fontWeight = FontWeight.Medium)
            CmpText(text = device.address, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
        }
        CmpCircularProgressIndicator()
        CmpOutlinedButton(text = stringResource(Res.string.device_select_btn_cancel), onClick = onCancel)
    }
}

@Composable
private fun ScanningOrListBody(
    state: ScanDeviceState,
    layout: DeviceSelectLayout,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelected: () -> Unit,
) {
    when (val devices = state.devices) {
        is LCEState.Content ->
            if (devices.content.isEmpty()) {
                if (state.searchedLongEnough) EmptyBody(state.supportedTransports) else ScanningBody()
            } else {
                DeviceList(devices.content, state.selectedId, layout, onSelect, onConnectSelected)
            }

        is LCEState.Error -> ScanningBody()

        else -> if (state.searchedLongEnough) EmptyBody(state.supportedTransports) else ScanningBody()
    }
}

@Composable
private fun DeviceList(
    devices: List<DeviceUiItem>,
    selectedId: String?,
    layout: DeviceSelectLayout,
    onSelect: (DeviceUiItem) -> Unit,
    onConnectSelected: () -> Unit,
) {
    val tileSize = if (layout == DeviceSelectLayout.Compact) 56 else 60

    if (layout == DeviceSelectLayout.Split) {
        val colors = cmpThemeColors()
        val selected = devices.firstOrNull { it.id == selectedId }
        CmpRow(
            modifier = Modifier.fillMaxSize().widthIn(max = 1100.dp).padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ListHeader(devices.size)
                CmpColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    devices.forEach { d -> DeviceRow(d, d.id == selectedId, { onSelect(d) }, tileSize) }
                }
            }
            CmpSurface(modifier = Modifier.width(1.dp).fillMaxHeight(), color = colors.outlineVariant) {}
            CmpBox(modifier = Modifier.weight(1f).widthIn(max = 420.dp).fillMaxHeight()) {
                DetailPanel(selected, onConnectSelected)
            }
        }
        return
    }

    val maxW = if (layout == DeviceSelectLayout.Stacked) 620.dp else Int.MAX_VALUE.dp
    CmpColumn(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CmpColumn(modifier = Modifier.widthIn(max = maxW).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ListHeader(devices.size)
            CmpColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                devices.forEach { d -> DeviceRow(d, selected = false, { onSelect(d) }, tileSize) }
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
        CmpText(text = stringResource(Res.string.device_select_list_count, count), fontSize = DsType.TitleMedium, fontWeight = FontWeight.Medium)
        CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpCircularProgressIndicator(modifier = Modifier.size(16.dp))
            CmpText(text = stringResource(Res.string.device_select_list_searching_short), fontSize = DsType.LabelLarge, color = colors.onSurfaceVariant)
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
                    CmpIcon(icon = CmpIcons.TouchApp, contentDescription = null, tint = colors.onSurfaceVariant)
                }
            }
            CmpText(
                text = stringResource(Res.string.device_select_detail_empty),
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
            CmpColumn(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpText(text = device.name, fontSize = DsType.HeadlineSmall, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                TransportChip(transport = device.transport)
            }
            CmpColumn(modifier = Modifier.fillMaxWidth()) {
                DetailRow(
                    label = stringResource(Res.string.device_select_detail_label_link),
                    value = transportLabel(device.transport),
                )
                DetailRow(
                    label = if (device.transport == DeviceTransport.BLE) {
                        stringResource(Res.string.device_select_detail_label_mac)
                    } else {
                        stringResource(Res.string.device_select_detail_label_address)
                    },
                    value = device.address,
                )
            }
            IconedButton(
                text = stringResource(Res.string.device_select_btn_connect),
                icon = CmpIcons.Link,
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(text = label, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
        CmpText(text = value, fontSize = DsType.BodyMedium, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
    }
}
