package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_a11y_device_info
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_close
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_btn_connect
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_format_rssi
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_msg_alarms_only
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_alarm
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_immobilizer
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_microbot
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_module
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_navigation
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_other
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_relay
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_remote_control
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_sensor
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_kind_tag
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_mode_boot
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_mode_main
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_mode_normal
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_mode_pairing
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_no
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_transport_ble
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_transport_ble_short
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_transport_com
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_transport_usb
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_unknown
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_common_value_yes
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_device_bond
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_firmware
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_mac
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_manufacturer
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_mode
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_phone_bond
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_port
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_signal
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_system_id
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_type
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_uuid
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_label_vid_pid
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_msg_note_ble
import ru.alarmtrade.specmp.core.localization.resources.deviceselect_detail_msg_note_serial
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
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBreakpoints
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.restricted

private val LargeTileMinSize = 96.dp
private val HeroSize = 96.dp
private val CenterStateMaxWidth = 460.dp

internal enum class HeroTone { Primary, Error, Neutral }

@Composable
internal fun transportLabel(transport: DeviceTransport): String = when (transport) {
    DeviceTransport.BLE -> stringResource(Strings.string.deviceselect_common_value_transport_ble)
    DeviceTransport.USB -> stringResource(Strings.string.deviceselect_common_value_transport_usb)
    DeviceTransport.SERIAL -> stringResource(Strings.string.deviceselect_common_value_transport_com)
}

@Composable
internal fun kindLabel(kind: DeviceKind): String = stringResource(
    when (kind) {
        DeviceKind.ALARM -> Strings.string.deviceselect_common_value_kind_alarm
        DeviceKind.MICROBOT -> Strings.string.deviceselect_common_value_kind_microbot
        DeviceKind.TAG -> Strings.string.deviceselect_common_value_kind_tag
        DeviceKind.RELAY -> Strings.string.deviceselect_common_value_kind_relay
        DeviceKind.IMMOBILIZER -> Strings.string.deviceselect_common_value_kind_immobilizer
        DeviceKind.REMOTE_CONTROL -> Strings.string.deviceselect_common_value_kind_remote_control
        DeviceKind.NAVIGATION -> Strings.string.deviceselect_common_value_kind_navigation
        DeviceKind.MODULE -> Strings.string.deviceselect_common_value_kind_module
        DeviceKind.SENSOR -> Strings.string.deviceselect_common_value_kind_sensor
        DeviceKind.OTHER -> Strings.string.deviceselect_common_value_kind_other
    },
)

@Composable
internal fun modeLabel(mode: ScannedDeviceMode, transport: DeviceTransport): String = stringResource(
    when (mode) {
        ScannedDeviceMode.BASE -> when (transport) {
            DeviceTransport.BLE -> Strings.string.deviceselect_common_value_mode_normal
            DeviceTransport.USB, DeviceTransport.SERIAL -> Strings.string.deviceselect_common_value_mode_main
        }

        ScannedDeviceMode.PAIRING -> Strings.string.deviceselect_common_value_mode_pairing

        ScannedDeviceMode.BOOT -> Strings.string.deviceselect_common_value_mode_boot
    },
)

internal val DeviceUiItem.modeOrNull: ScannedDeviceMode?
    get() = when (this) {
        is DeviceUiItem.Ble -> mode
        is DeviceUiItem.Usb -> isBootMode?.let { if (it) ScannedDeviceMode.BOOT else ScannedDeviceMode.BASE }
        is DeviceUiItem.Serial -> null
    }

internal val DeviceUiItem.notableMode: ScannedDeviceMode?
    get() = modeOrNull?.takeIf { it != ScannedDeviceMode.BASE }

@Composable
internal fun ModeBadge(mode: ScannedDeviceMode) {
    CmpTag(
        text = modeLabel(mode, DeviceTransport.BLE),
        tone = if (mode == ScannedDeviceMode.BOOT) CmpTagTone.Error else CmpTagTone.Warning,
    )
}

/** `hasPhoneBond == null` — платформа не Android: строку не показываем, а BLE-адрес на iOS — это UUID. */
@Composable
internal fun deviceInfoRows(item: DeviceUiItem): List<DeviceInfoRow> {
    val yes = stringResource(Strings.string.deviceselect_common_value_yes)
    val no = stringResource(Strings.string.deviceselect_common_value_no)
    val modeRow = item.modeOrNull?.let { mode ->
        DeviceInfoRow(
            label = stringResource(Strings.string.deviceselect_detail_label_mode),
            value = modeLabel(mode, item.transport),
            strong = mode != ScannedDeviceMode.BASE,
        )
    }
    return when (item) {
        is DeviceUiItem.Ble -> listOfNotNull(
            DeviceInfoRow(
                label = stringResource(
                    if (item.hasPhoneBond ==
                        null
                    ) {
                        Strings.string.deviceselect_detail_label_uuid
                    } else {
                        Strings.string.deviceselect_detail_label_mac
                    },
                ),
                value = item.address,
            ),
            DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_type), kindLabel(item.kind)),
            DeviceInfoRow(
                stringResource(Strings.string.deviceselect_detail_label_signal),
                stringResource(Strings.string.deviceselect_common_format_rssi, item.rssi),
            ),
            modeRow,
            item.hasPhoneBond?.let {
                DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_phone_bond), if (it) yes else no)
            },
            DeviceInfoRow(
                stringResource(Strings.string.deviceselect_detail_label_device_bond),
                if (item.hasDeviceBond) yes else no,
            ),
        )

        is DeviceUiItem.Usb -> listOfNotNull(
            DeviceInfoRow(
                stringResource(Strings.string.deviceselect_detail_label_system_id),
                item.systemId?.toString() ?: stringResource(Strings.string.deviceselect_common_value_unknown),
            ),
            DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_manufacturer), item.manufacturerName),
            DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_vid_pid), item.vidPid),
            item.firmwareVersion?.let {
                DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_firmware), it)
            },
            modeRow,
        )

        is DeviceUiItem.Serial -> listOfNotNull(
            DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_port), item.id),
            item.vidPid?.let { DeviceInfoRow(stringResource(Strings.string.deviceselect_detail_label_vid_pid), it) },
        )
    }
}

@Composable
internal fun deviceInfoNote(item: DeviceUiItem): String? = when (item) {
    is DeviceUiItem.Ble -> stringResource(Strings.string.deviceselect_detail_msg_note_ble)
    is DeviceUiItem.Usb -> null
    is DeviceUiItem.Serial -> stringResource(Strings.string.deviceselect_detail_msg_note_serial)
}

@Composable
internal fun DeviceInfoRows(rows: List<DeviceInfoRow>) {
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            if (index > 0) CmpDivider()
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = CmpSpacing.space12),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                verticalAlignment = Alignment.Top,
            ) {
                CmpText(
                    text = row.label,
                    style = CmpTextStyle.Paragraph2,
                    color = colors.foregroundOnDarkSecondaryDefault,
                    maxLines = 1,
                )
                CmpText(
                    text = row.value,
                    style = if (row.strong) CmpTextStyle.Additional0 else CmpTextStyle.Paragraph3,
                    modifier = Modifier.weight(1f),
                    color = if (row.strong) colors.brandPrimaryDefault else colors.foregroundOnDarkPrimaryDefault,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
internal fun DeviceInfoDialog(item: DeviceUiItem, onDismiss: () -> Unit, onConnect: () -> Unit) {
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = item.name, style = CmpTextStyle.Headline1) },
        text = {
            CmpColumn(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DeviceInfoRows(deviceInfoRows(item))
                deviceInfoNote(item)?.let {
                    CmpText(
                        text = it,
                        style = CmpTextStyle.Paragraph3,
                        modifier = Modifier.padding(top = CmpSpacing.space8),
                        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
        },
        dismissButton = {
            CmpButton(
                text = stringResource(Strings.string.deviceselect_common_btn_close),
                onClick = onDismiss,
                type = CmpButtonType.Tertiary,
                size = CmpButtonSize.S,
            )
        },
        confirmButton = {
            CmpButton(
                text = stringResource(Strings.string.deviceselect_common_btn_connect),
                onClick = onConnect,
                size = CmpButtonSize.S,
            )
        },
    )
}

internal fun DeviceTransport.icon(): CmpIcons = when (this) {
    DeviceTransport.BLE -> CmpIcons.Bluetooth
    DeviceTransport.USB -> CmpIcons.Usb
    DeviceTransport.SERIAL -> CmpIcons.Cable
}

@Composable
internal fun DeviceTile(icon: CmpIcons, size: Dp) {
    val colors = CmpTheme.colors
    val large = size >= LargeTileMinSize
    val shape = RoundedCornerShape(if (large) CmpRadius.cardMedium else CmpRadius.cardSmall)
    CmpBox(
        modifier = Modifier
            .size(size)
            .background(colors.backgroundSurfaceHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(
            icon = icon,
            contentDescription = null,
            modifier = Modifier.size(if (large) CmpSize.iconXxl else CmpSize.iconM),
            tint = colors.foregroundOnDarkSecondaryDefault,
        )
    }
}

@Composable
internal fun TransportChip(transport: DeviceTransport, short: Boolean = false) {
    CmpTag(
        text = if (short && transport == DeviceTransport.BLE) {
            stringResource(Strings.string.deviceselect_common_value_transport_ble_short)
        } else {
            transportLabel(transport)
        },
        icon = transport.icon(),
    )
}

private const val RESTRICTED_ALPHA = 0.5f

internal const val DEVICE_ITEM_TAG_PREFIX = "device_item_"

@Composable
internal fun DeviceRow(
    item: DeviceUiItem,
    selected: Boolean,
    onClick: () -> Unit,
    tileSize: Dp,
    showMode: Boolean = false,
    onInfo: (() -> Unit)? = null,
) {
    val restricted = item.restricted
    CmpListItem(
        title = item.name,
        modifier = Modifier.fillMaxWidth().testTag(DEVICE_ITEM_TAG_PREFIX + item.id),
        selected = selected,
        onClick = if (restricted) null else onClick,
        leading = {
            CmpBox(modifier = Modifier.alpha(if (restricted) RESTRICTED_ALPHA else 1f)) {
                DeviceTile(icon = item.typeIcon, size = tileSize)
            }
        },
        below = { DeviceRowMeta(item = item, showMode = showMode, restricted = restricted) },
        trailing = if (onInfo != null && !restricted) {
            {
                CmpIconButton(
                    onClick = onInfo,
                    icon = CmpIcons.MoreVert,
                    contentDescription = stringResource(Strings.string.deviceselect_common_a11y_device_info),
                    tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun DeviceRowMeta(item: DeviceUiItem, showMode: Boolean, restricted: Boolean) {
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        TransportChip(transport = item.transport, short = true)
        if (showMode) item.notableMode?.let { ModeBadge(it) }
        if (restricted) {
            CmpTag(
                text = stringResource(Strings.string.deviceselect_common_msg_alarms_only),
                tone = CmpTagTone.Outline,
                icon = CmpIcons.Block,
            )
        } else {
            CmpText(
                text = item.address,
                style = CmpTextStyle.Additional1,
                color = secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item is DeviceUiItem.Ble) {
            CmpText(
                text = stringResource(Strings.string.deviceselect_common_format_rssi, item.rssi),
                style = CmpTextStyle.Additional0,
                color = secondary,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun Hero(icon: CmpIcons, tone: HeroTone = HeroTone.Primary) {
    val colors = CmpTheme.colors
    val (bg, fg) = when (tone) {
        HeroTone.Primary -> colors.brandPrimaryHighlight to colors.brandPrimaryDefault
        HeroTone.Error -> colors.errorHighlight to colors.errorDefault
        HeroTone.Neutral -> colors.backgroundSurfaceHighlight to colors.foregroundOnDarkSecondaryDefault
    }
    CmpBox(
        modifier = Modifier.size(HeroSize).background(bg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(CmpSize.iconXl), tint = fg)
    }
}

@Composable
internal fun CenterState(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        CmpBox(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = screenPaddingH(maxWidth), vertical = CmpSpacing.space32),
            contentAlignment = Alignment.Center,
        ) {
            CmpColumn(
                modifier = Modifier.widthIn(max = CenterStateMaxWidth).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space24),
            ) { content() }
        }
    }
}

internal fun screenPaddingH(width: Dp): Dp =
    if (width >= CmpBreakpoints.Medium) CmpSpacing.screenPaddingHMedium else CmpSpacing.screenPaddingH

@Composable
internal fun FixSteps(items: List<String>, done: Boolean) {
    val colors = CmpTheme.colors
    CmpCard(modifier = Modifier.fillMaxWidth()) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(CmpSpacing.space16),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            items.forEach { text ->
                CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                    CmpIcon(
                        icon = if (done) CmpIcons.CheckCircle else CmpIcons.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(CmpSize.iconM),
                        tint = colors.brandPrimaryDefault,
                    )
                    CmpText(
                        text = text,
                        style = CmpTextStyle.Paragraph2,
                        color = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }
        }
    }
}

@Composable
internal fun StateHeadline(title: String, subtitle: String) {
    CmpColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpText(
            text = title,
            style = CmpTextStyle.Headline1,
            textAlign = TextAlign.Center,
        )
        CmpText(
            text = subtitle,
            style = CmpTextStyle.Paragraph2,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            textAlign = TextAlign.Center,
        )
    }
}
