package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpDialog
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceKind
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.domain.model.ScannedDeviceMode
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import specmp.feature.device.selection_ui.generated.resources.Res
import specmp.feature.device.selection_ui.generated.resources.device_select_a11y_device_info
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_close
import specmp.feature.device.selection_ui.generated.resources.device_select_btn_connect
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_device_bond
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_firmware
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_mac
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_manufacturer
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_mode
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_phone_bond
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_signal
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_system_id
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_type
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_uuid
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_label_vid_pid
import specmp.feature.device.selection_ui.generated.resources.device_select_detail_note_ble
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_alarm
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_immobilizer
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_module
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_navigation
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_other
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_relay
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_remote_control
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_sensor
import specmp.feature.device.selection_ui.generated.resources.device_select_kind_tag
import specmp.feature.device.selection_ui.generated.resources.device_select_mode_boot
import specmp.feature.device.selection_ui.generated.resources.device_select_mode_main
import specmp.feature.device.selection_ui.generated.resources.device_select_mode_normal
import specmp.feature.device.selection_ui.generated.resources.device_select_mode_pairing
import specmp.feature.device.selection_ui.generated.resources.device_select_rssi
import specmp.feature.device.selection_ui.generated.resources.device_select_transport_ble
import specmp.feature.device.selection_ui.generated.resources.device_select_transport_ble_short
import specmp.feature.device.selection_ui.generated.resources.device_select_transport_usb
import specmp.feature.device.selection_ui.generated.resources.device_select_value_no
import specmp.feature.device.selection_ui.generated.resources.device_select_value_unknown
import specmp.feature.device.selection_ui.generated.resources.device_select_value_yes

private const val LARGE_TILE_MIN_SIZE_DP = 96

// M3-подобные размеры типографики (CmpText не принимает variant).
internal object DsType {
    val HeadlineSmall = 24.sp
    val TitleMedium = 16.sp
    val TitleSmall = 14.sp
    val BodyLarge = 16.sp
    val BodyMedium = 14.sp
    val LabelLarge = 14.sp
    val LabelSmall = 12.sp
}

internal enum class HeroTone { Primary, Error, Neutral }

@Composable
internal fun transportLabel(transport: DeviceTransport): String = when (transport) {
    DeviceTransport.BLE -> stringResource(Res.string.device_select_transport_ble)
    DeviceTransport.USB -> stringResource(Res.string.device_select_transport_usb)
}

@Composable
internal fun kindLabel(kind: DeviceKind): String = stringResource(
    when (kind) {
        DeviceKind.ALARM -> Res.string.device_select_kind_alarm
        DeviceKind.TAG -> Res.string.device_select_kind_tag
        DeviceKind.RELAY -> Res.string.device_select_kind_relay
        DeviceKind.IMMOBILIZER -> Res.string.device_select_kind_immobilizer
        DeviceKind.REMOTE_CONTROL -> Res.string.device_select_kind_remote_control
        DeviceKind.NAVIGATION -> Res.string.device_select_kind_navigation
        DeviceKind.MODULE -> Res.string.device_select_kind_module
        DeviceKind.SENSOR -> Res.string.device_select_kind_sensor
        DeviceKind.OTHER -> Res.string.device_select_kind_other
    }
)

/** Подпись режима. Обычный режим у BLE — «Обычный», у USB — «Рабочий». */
@Composable
internal fun modeLabel(mode: ScannedDeviceMode, transport: DeviceTransport): String = stringResource(
    when (mode) {
        ScannedDeviceMode.BASE -> when (transport) {
            DeviceTransport.BLE -> Res.string.device_select_mode_normal
            DeviceTransport.USB -> Res.string.device_select_mode_main
        }

        ScannedDeviceMode.PAIRING -> Res.string.device_select_mode_pairing

        ScannedDeviceMode.BOOT -> Res.string.device_select_mode_boot
    }
)

/** Режим устройства, если известен (у USB — только после опроса блока). */
internal val DeviceUiItem.modeOrNull: ScannedDeviceMode?
    get() = when (this) {
        is DeviceUiItem.Ble -> mode
        is DeviceUiItem.Usb -> isBootMode?.let { if (it) ScannedDeviceMode.BOOT else ScannedDeviceMode.BASE }
    }

/** Режим, который стоит заметить в списке: сопряжение или загрузчик (рабочий — без метки). */
internal val DeviceUiItem.notableMode: ScannedDeviceMode?
    get() = modeOrNull?.takeIf { it != ScannedDeviceMode.BASE }

/** Метка режима блока (сопряжение / загрузчик). */
@Composable
internal fun ModeBadge(mode: ScannedDeviceMode) {
    val colors = cmpThemeColors()
    val (bg, fg) = if (mode == ScannedDeviceMode.BOOT) {
        colors.errorContainer to colors.onErrorContainer
    } else {
        colors.tertiaryContainer to colors.onTertiaryContainer
    }
    CmpSurface(shape = CircleShape, color = bg, contentColor = fg) {
        CmpText(
            text = modeLabel(mode, DeviceTransport.BLE),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = DsType.LabelSmall,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

/** Строка деталей «подпись — значение»; [strong] — выделенное значение (режим сопряжения / загрузчика). */
@Immutable
internal data class DeviceInfoRow(val label: String, val value: String, val strong: Boolean = false)

/**
 * Поля устройства «до подключения» — общие для панели деталей (split) и диалога «⋮» (compact / stacked).
 * `hasPhoneBond == null` — платформа не Android: строку не показываем, а BLE-адрес на iOS — это UUID.
 */
@Composable
internal fun deviceInfoRows(item: DeviceUiItem): List<DeviceInfoRow> {
    val yes = stringResource(Res.string.device_select_value_yes)
    val no = stringResource(Res.string.device_select_value_no)
    val modeRow = item.modeOrNull?.let { mode ->
        DeviceInfoRow(
            label = stringResource(Res.string.device_select_detail_label_mode),
            value = modeLabel(mode, item.transport),
            strong = mode != ScannedDeviceMode.BASE
        )
    }
    return when (item) {
        is DeviceUiItem.Ble -> listOfNotNull(
            DeviceInfoRow(
                label = stringResource(
                    if (item.hasPhoneBond ==
                        null
                    ) {
                        Res.string.device_select_detail_label_uuid
                    } else {
                        Res.string.device_select_detail_label_mac
                    }
                ),
                value = item.address
            ),
            DeviceInfoRow(stringResource(Res.string.device_select_detail_label_type), kindLabel(item.kind)),
            DeviceInfoRow(
                stringResource(Res.string.device_select_detail_label_signal),
                stringResource(Res.string.device_select_rssi, item.rssi)
            ),
            modeRow,
            item.hasPhoneBond?.let {
                DeviceInfoRow(stringResource(Res.string.device_select_detail_label_phone_bond), if (it) yes else no)
            },
            DeviceInfoRow(
                stringResource(Res.string.device_select_detail_label_device_bond),
                if (item.hasDeviceBond) yes else no
            )
        )

        is DeviceUiItem.Usb -> listOfNotNull(
            DeviceInfoRow(
                stringResource(Res.string.device_select_detail_label_system_id),
                item.systemId?.toString() ?: stringResource(Res.string.device_select_value_unknown)
            ),
            DeviceInfoRow(stringResource(Res.string.device_select_detail_label_manufacturer), item.manufacturerName),
            DeviceInfoRow(stringResource(Res.string.device_select_detail_label_vid_pid), item.vidPid),
            item.firmwareVersion?.let {
                DeviceInfoRow(stringResource(Res.string.device_select_detail_label_firmware), it)
            },
            modeRow
        )
    }
}

/** Сноска под полями: у BLE прошивка и ID блока появятся только после подключения. */
@Composable
internal fun deviceInfoNote(item: DeviceUiItem): String? = when (item) {
    is DeviceUiItem.Ble -> stringResource(Res.string.device_select_detail_note_ble)
    is DeviceUiItem.Usb -> null
}

/** Поля устройства с разделителями: подпись всегда целиком в одну строку, значение занимает остаток и переносится. */
@Composable
internal fun DeviceInfoRows(rows: List<DeviceInfoRow>) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            if (index > 0) CmpDivider()
            CmpRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                CmpText(text = row.label, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant, maxLines = 1)
                CmpText(
                    text = row.value,
                    modifier = Modifier.weight(1f),
                    fontSize = DsType.BodyMedium,
                    fontWeight = if (row.strong) FontWeight.Bold else FontWeight.Medium,
                    color = if (row.strong) colors.primary else colors.onSurface,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/** Диалог «⋮» в compact / stacked: поля устройства и подключение. */
@Composable
internal fun DeviceInfoDialog(item: DeviceUiItem, onDismiss: () -> Unit, onConnect: () -> Unit) {
    val colors = cmpThemeColors()
    CmpDialog(
        onDismissRequest = onDismiss,
        title = { CmpText(text = item.name) },
        text = {
            CmpColumn(modifier = Modifier.verticalScroll(rememberScrollState())) {
                DeviceInfoRows(deviceInfoRows(item))
                deviceInfoNote(item)?.let {
                    CmpText(
                        text = it,
                        modifier = Modifier.padding(top = 8.dp),
                        fontSize = DsType.BodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }
            }
        },
        dismissButton = {
            CmpTextButton(text = stringResource(Res.string.device_select_btn_close), onClick = onDismiss)
        },
        confirmButton = {
            CmpTextButton(text = stringResource(Res.string.device_select_btn_connect), onClick = onConnect)
        }
    )
}

internal fun DeviceTransport.icon(): CmpIcons = when (this) {
    DeviceTransport.BLE -> CmpIcons.Bluetooth
    DeviceTransport.USB -> CmpIcons.Usb
}

/** Плейсхолдер-слот фото устройства: нейтральный фон + иконка типа (реального фото пока нет). */
@Composable
internal fun DeviceTile(icon: CmpIcons, sizeDp: Int) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.size(sizeDp.dp),
        shape = RoundedCornerShape(if (sizeDp >= LARGE_TILE_MIN_SIZE_DP) 20.dp else 12.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, tint = colors.onSurfaceVariant)
        }
    }
}

/** Чип транспорта: в карточке списка короткий («BLE» / «USB»), в деталях полный («Bluetooth»). */
@Composable
internal fun TransportChip(transport: DeviceTransport, short: Boolean = false) {
    val colors = cmpThemeColors()
    CmpSurface(
        shape = CircleShape,
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpIcon(
                icon = transport.icon(),
                contentDescription = null,
                modifier = Modifier.size(if (short) 14.dp else 18.dp),
                tint = colors.onSecondaryContainer
            )
            CmpText(
                text = if (short && transport == DeviceTransport.BLE) {
                    stringResource(Res.string.device_select_transport_ble_short)
                } else {
                    transportLabel(transport)
                },
                fontSize = if (short) DsType.LabelSmall else DsType.BodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/** testTag строки устройства — префикс, дальше id устройства (BLE — MAC). */
internal const val DEVICE_ITEM_TAG_PREFIX = "device_item_"

/**
 * Строка устройства в списке: имя (+ RSSI у BLE справа), чип транспорта и адрес.
 * [showMode] — метка «Сопряжение» / «Загрузчик» (только в широкой раскладке);
 * [onInfo] — кнопка «⋮» с диалогом полей (compact / stacked).
 */
@Composable
internal fun DeviceRow(
    item: DeviceUiItem,
    selected: Boolean,
    onClick: () -> Unit,
    tileSize: Int,
    showMode: Boolean = false,
    onInfo: (() -> Unit)? = null
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth().testTag(DEVICE_ITEM_TAG_PREFIX + item.id)
            .clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = if (selected) null else BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(
                start = 14.dp,
                end = if (onInfo !=
                    null
                ) {
                    8.dp
                } else {
                    14.dp
                },
                top = 10.dp,
                bottom = 10.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceTile(icon = item.typeIcon, sizeDp = tileSize)
            CmpColumn(modifier = Modifier.weight(1f)) {
                val secondary = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                CmpRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // У BLE имя занимает всю ширину, чтобы RSSI прижался вправо.
                    CmpText(
                        text = item.name,
                        modifier = Modifier.weight(1f, fill = item is DeviceUiItem.Ble),
                        fontSize = DsType.TitleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showMode) item.notableMode?.let { ModeBadge(it) }
                    if (item is DeviceUiItem.Ble) {
                        CmpText(
                            text = stringResource(Res.string.device_select_rssi, item.rssi),
                            fontSize = DsType.LabelLarge,
                            fontWeight = FontWeight.Medium,
                            color = secondary,
                            maxLines = 1
                        )
                    }
                }
                CmpRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransportChip(transport = item.transport, short = true)
                    CmpText(
                        text = item.address,
                        fontSize = DsType.LabelSmall,
                        color = secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                onInfo?.let {
                    CmpIconButton(
                        onClick = it,
                        icon = CmpIcons.MoreVert,
                        contentDescription = stringResource(Res.string.device_select_a11y_device_info),
                        tint = colors.onSurfaceVariant
                    )
                }
                CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant
                )
            }
        }
    }
}

/** Круглый герой-значок состояний. */
@Composable
internal fun Hero(icon: CmpIcons, tone: HeroTone = HeroTone.Primary) {
    val colors = cmpThemeColors()
    val bg = when (tone) {
        HeroTone.Primary -> colors.primaryContainer
        HeroTone.Error -> colors.errorContainer
        HeroTone.Neutral -> colors.surfaceVariant
    }
    val fg = when (tone) {
        HeroTone.Primary -> colors.onPrimaryContainer
        HeroTone.Error -> colors.onErrorContainer
        HeroTone.Neutral -> colors.onSurfaceVariant
    }
    CmpSurface(modifier = Modifier.size(96.dp), shape = CircleShape, color = bg) {
        CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = fg)
        }
    }
}

/** Обёртка центрированных состояний (прокручиваемая). */
@Composable
internal fun CenterState(content: @Composable () -> Unit) {
    CmpBox(
        modifier = Modifier.fillMaxSize().verticalScroll(
            rememberScrollState()
        ).padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        CmpColumn(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) { content() }
    }
}

/** Строка разрешения в экране-обосновании. */
@Composable
internal fun PermissionRow(icon: CmpIcons, label: String, desc: String) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpRow(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpSurface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer
            ) {
                CmpBox(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CmpIcon(icon = icon, contentDescription = null, tint = colors.onSecondaryContainer)
                }
            }
            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(text = label, fontSize = DsType.TitleSmall, fontWeight = FontWeight.Medium)
                CmpText(text = desc, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** Список подсказок «как починить» / «почему пусто». */
@Composable
internal fun FixSteps(items: List<String>, done: Boolean) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant)
    ) {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.forEach { text ->
                CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CmpIcon(
                        icon = if (done) CmpIcons.CheckCircle else CmpIcons.ChevronRight,
                        contentDescription = null,
                        tint = colors.primary
                    )
                    CmpText(text = text, fontSize = DsType.BodyMedium, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

/** Кнопка с иконкой (CmpButton не принимает icon — используем content-overload). */
@Composable
internal fun IconedButton(text: String, icon: CmpIcons, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpButton(onClick = onClick, modifier = modifier) {
        CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CmpIcon(icon = icon, contentDescription = null)
            CmpText(text = text, fontSize = DsType.LabelLarge, fontWeight = FontWeight.Medium)
        }
    }
}

/** Заголовок + подзаголовок для центрированных состояний. */
@Composable
internal fun StateHeadline(title: String, subtitle: String) {
    val colors = cmpThemeColors()
    CmpColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CmpText(
            text = title,
            fontSize = DsType.HeadlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        CmpText(
            text = subtitle,
            fontSize = DsType.BodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
