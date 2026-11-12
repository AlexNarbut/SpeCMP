package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_btn_blink
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_btn_blinking
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_btn_update
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_format_bar
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_format_celsius
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_format_fuel
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_format_metric
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_hint_blink
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_hint_unavailable
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_hint_update
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_battery
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_engine_temperature
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_event
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_fuel
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_hood
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_lock
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_number
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_pir
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_pressure
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_service
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_signal
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_state
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_temperature
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_triggers
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_label_type
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_msg_live
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_msg_offline
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_blinking
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_blocked
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_closed
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_disabled
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_enabled
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_open
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_pressed
import ru.alarmtrade.specmp.core.localization.resources.peripherals_details_value_unblocked
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_far
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_near
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_offline
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_online
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_paired
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_btn_show
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_format_attention
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_format_firmware
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_format_progress
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_format_summary
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_format_version
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_label_ble
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_label_block
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_loading_poll
import ru.alarmtrade.specmp.core.localization.resources.peripherals_system_msg_all_online
import ru.alarmtrade.specmp.core.localization.resources.peripherals_type_format_name
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralDetails
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.canBlink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.canEnterFirmwareMode
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.BatteryIndicator
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.DeviceIcon
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.IconState
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.PeripheralTone
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.SignalBars
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.StatusPill
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.colors
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.formatDecimal
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.batteryLow
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.batteryPercent
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.needsAttention
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.ui

internal const val SYSTEM_ROW_TAG_PREFIX = "peripherals_device_"
internal const val SYSTEM_SUMMARY_TAG = "peripherals_summary"
internal const val SYSTEM_BLINK_TAG = "peripherals_blink"
internal const val SYSTEM_UPDATE_TAG = "peripherals_update"

private const val FLASH_START_ALPHA = 0.5f
private const val FLASH_MILLIS = 1_200
private val ButtonHeight = 56.dp

internal class LinkUi(val label: StringResource, val tone: PeripheralTone)

internal val PeripheralLink.ui: LinkUi
    get() = when (this) {
        PeripheralLink.NEAR -> LinkUi(Strings.string.peripherals_status_value_near, PeripheralTone.OK)
        PeripheralLink.FAR -> LinkUi(Strings.string.peripherals_status_value_far, PeripheralTone.WARN)
        PeripheralLink.ONLINE -> LinkUi(Strings.string.peripherals_status_value_online, PeripheralTone.OK)
        PeripheralLink.OFFLINE -> LinkUi(Strings.string.peripherals_status_value_offline, PeripheralTone.ERROR)
        PeripheralLink.PAIRED -> LinkUi(Strings.string.peripherals_status_value_paired, PeripheralTone.NEUTRAL)
    }

@Composable
internal fun deviceTitle(type: PeripheralType, number: Int): String =
    stringResource(Strings.string.peripherals_type_format_name, stringResource(type.ui.name), number)

/** Показатель типа: [label] `null` — значение говорит само за себя («Заблокировано»). */
internal class Metric(val label: StringResource?, val value: String)

@Composable
private fun celsius(value: Int): String =
    stringResource(Strings.string.peripherals_details_format_celsius, if (value > 0) "+$value" else value.toString())

@Composable
private fun closedOpen(closed: Boolean): String = stringResource(
    if (closed) Strings.string.peripherals_details_value_closed else Strings.string.peripherals_details_value_open,
)

@Composable
internal fun SystemPeripheral.metrics(): List<Metric> = when (val details = details) {
    is PeripheralDetails.Relay -> listOf(
        Metric(
            Strings.string.peripherals_details_label_state,
            stringResource(
                if (details.blocked) {
                    Strings.string.peripherals_details_value_blocked
                } else {
                    Strings.string.peripherals_details_value_unblocked
                },
            ),
        ),
    )

    is PeripheralDetails.HoodLock -> listOf(
        Metric(Strings.string.peripherals_details_label_lock, closedOpen(details.locked)),
        Metric(Strings.string.peripherals_details_label_hood, closedOpen(!details.hoodOpened)),
        Metric(Strings.string.peripherals_details_label_temperature, celsius(details.temperature)),
        Metric(Strings.string.peripherals_details_label_triggers, details.triggerCount.toString()),
    )

    is PeripheralDetails.Tpms -> listOf(
        Metric(
            Strings.string.peripherals_details_label_pressure,
            stringResource(Strings.string.peripherals_details_format_bar, details.pressureBar.formatDecimal(1)),
        ),
        Metric(Strings.string.peripherals_details_label_temperature, celsius(details.temperature)),
    )

    is PeripheralDetails.FuelSensor -> listOf(
        Metric(
            Strings.string.peripherals_details_label_fuel,
            stringResource(Strings.string.peripherals_details_format_fuel, details.liters, details.percent),
        ),
        Metric(Strings.string.peripherals_details_label_temperature, celsius(details.temperature)),
    )

    is PeripheralDetails.Pir -> listOf(
        Metric(Strings.string.peripherals_details_label_pir, details.index.toString()),
        Metric(Strings.string.peripherals_details_label_temperature, celsius(details.temperature)),
    )

    is PeripheralDetails.EngineTemperature ->
        listOf(Metric(Strings.string.peripherals_details_label_engine_temperature, celsius(details.celsius)))

    is PeripheralDetails.Vs -> listOf(
        Metric(
            Strings.string.peripherals_details_label_state,
            stringResource(
                if (details.enabled) {
                    Strings.string.peripherals_details_value_enabled
                } else {
                    Strings.string.peripherals_details_value_disabled
                },
            ),
        ),
    )

    is PeripheralDetails.Tag, null -> emptyList()
}

/** События — чипы акцентом: нажата кнопка, мигает, сервисный режим. */
@Composable
internal fun SystemPeripheral.flags(): List<String> = buildList {
    when (val details = details) {
        is PeripheralDetails.Tag -> {
            if (details.pressed) add(stringResource(Strings.string.peripherals_details_value_pressed))
            if (details.blinking) add(stringResource(Strings.string.peripherals_details_value_blinking))
        }

        is PeripheralDetails.Relay -> if (details.serviceMode) {
            add(stringResource(Strings.string.peripherals_details_label_service))
        }

        is PeripheralDetails.HoodLock -> if (details.serviceMode) {
            add(stringResource(Strings.string.peripherals_details_label_service))
        }

        else -> Unit
    }
}

@Composable
private fun Metric.inline(): String = if (label == null || label == Strings.string.peripherals_details_label_state ||
    label == Strings.string.peripherals_details_label_lock
) {
    value
} else {
    stringResource(Strings.string.peripherals_details_format_metric, stringResource(label), value)
}

@Composable
private fun FlagChip(text: String) {
    val colors = PeripheralTone.ACCENT.colors()
    CmpBox(
        modifier = Modifier
            .heightIn(min = 30.dp)
            .background(colors.container, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(text = text, color = colors.content, fontWeight = FontWeight.Medium, style = CmpTextStyle.Body)
    }
}

@Composable
private fun Indicators(device: SystemPeripheral) {
    val offline = device.link == PeripheralLink.OFFLINE
    if (offline) return
    device.signalDbm?.let { SignalBars(dbm = it) }
    device.voltage?.let { voltage ->
        BatteryIndicator(
            voltage = voltage,
            percent = batteryPercent(device.type, voltage),
            low = device.batteryLow(),
        )
    }
}

/** Строка устройства (вариант A макета): иконка, заголовок + адрес, статус справа; ниже — индикаторы и показатели. */
@Composable
internal fun DeviceRow(
    device: SystemPeripheral,
    selected: Boolean,
    flashSeq: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val link = device.link.ui
    val attention = device.needsAttention()
    val offline = device.link == PeripheralLink.OFFLINE
    val primary = cmpThemeColors().primary
    val shape = RoundedCornerShape(18.dp)
    val rowTone = when {
        !attention -> Color.Transparent
        offline -> PeripheralTone.ERROR.colors().row
        else -> PeripheralTone.WARN.colors().row
    }
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashSeq) {
        if (flashSeq != null) {
            flash.snapTo(FLASH_START_ALPHA)
            flash.animateTo(0f, tween(FLASH_MILLIS))
        }
    }
    val metrics = device.metrics()
    val flags = device.flags()
    CmpRow(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(rowTone, shape)
            .then(if (selected) Modifier.border(2.dp, primary, shape) else Modifier)
            .drawBehind { drawRect(primary.copy(alpha = flash.value)) }
            .clickable(onClick = onClick)
            .heightIn(min = 72.dp)
            .padding(horizontal = 10.dp, vertical = 12.dp)
            .testTag(SYSTEM_ROW_TAG_PREFIX + device.address),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DeviceIcon(type = device.type, state = if (offline) IconState.OFF else IconState.ON, size = 52.dp)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CmpRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    CmpText(
                        text = deviceTitle(device.type, device.number),
                        fontWeight = FontWeight.SemiBold,
                        style = CmpTextStyle.BodyLarge,
                    )
                    AddressText(device.address)
                }
                StatusPill(label = link.label, tone = link.tone)
            }
            val showMetrics = !offline && metrics.isNotEmpty()
            if (!offline || flags.isNotEmpty() || showMetrics) {
                CmpFlowRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Indicators(device)
                    if (showMetrics) metrics.forEach { CmpText(text = it.inline(), style = CmpTextStyle.Body) }
                    flags.forEach { FlagChip(it) }
                }
            }
        }
    }
}

@Composable
internal fun AddressText(address: String, large: Boolean = false) {
    CmpText(
        text = address,
        color = cmpThemeColors().onSurfaceVariant,
        style = if (large) CmpTextStyle.BodyLarge else CmpTextStyle.Caption,
        maxLines = 1,
    )
}

/** «Базовый блок / BLE-модуль»: модель, прошивка, версия BLE — без кнопок (решение макета). */
@Composable
internal fun BlockHeader(deviceName: String?, firmware: String?, bluetoothVersion: String?, wide: Boolean) {
    val theme = cmpThemeColors()
    val shape = RoundedCornerShape(20.dp)
    val cells: @Composable (Modifier) -> Unit = { cellModifier ->
        HeaderCell(
            label = stringResource(Strings.string.peripherals_system_label_block),
            value = deviceName.orEmpty(),
            sub = firmware?.let { stringResource(Strings.string.peripherals_system_format_firmware, it) },
            modifier = cellModifier,
        )
        HeaderCell(
            label = stringResource(Strings.string.peripherals_system_label_ble),
            value = bluetoothVersion
                ?.let { stringResource(Strings.string.peripherals_system_format_version, it) }
                .orEmpty(),
            sub = null,
            modifier = cellModifier,
        )
    }
    val container = Modifier
        .fillMaxWidth()
        .background(theme.surfaceContainerLow, shape)
        .border(1.dp, theme.outlineVariant, shape)
        .padding(14.dp)
    if (wide) {
        CmpRow(modifier = container, horizontalArrangement = Arrangement.spacedBy(16.dp)) { cells(Modifier.weight(1f)) }
    } else {
        CmpRow(modifier = container, horizontalArrangement = Arrangement.spacedBy(16.dp)) { cells(Modifier.weight(1f)) }
    }
}

@Composable
private fun HeaderCell(label: String, value: String, sub: String?, modifier: Modifier) {
    val theme = cmpThemeColors()
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CmpText(text = label, color = theme.onSurfaceVariant, style = CmpTextStyle.Caption)
        CmpText(text = value, fontWeight = FontWeight.SemiBold, style = CmpTextStyle.BodyLarge, maxLines = 2)
        if (sub != null) CmpText(text = sub, color = theme.onSurfaceVariant, style = CmpTextStyle.Body)
    }
}

/** «Опрос блока… N из M» с полосой — только в первом проходе. */
@Composable
internal fun PollProgress(polled: Int, total: Int) {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CmpCircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.5.dp)
            CmpText(
                text = stringResource(Strings.string.peripherals_system_loading_poll),
                fontWeight = FontWeight.SemiBold,
                style = CmpTextStyle.BodyLarge,
                modifier = Modifier.weight(1f),
            )
            CmpText(
                text = stringResource(Strings.string.peripherals_system_format_progress, polled, total),
                color = cmpThemeColors().onSurfaceVariant,
                style = CmpTextStyle.Body,
            )
        }
        CmpLinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(6.dp),
            progress = if (total == 0) 0f else polled.toFloat() / total,
        )
    }
}

/** Сводка после прохода: «Требуют внимания: N» (ошибкой — если кто-то без связи) или «Все устройства на связи». */
@Composable
internal fun Summary(summary: DevicesSummary, onShow: () -> Unit) {
    val hasAttention = summary.attention > 0
    val theme = cmpThemeColors()
    val domain = cmpDomainColors()
    val tone = when {
        !hasAttention -> null
        summary.anyOffline -> PeripheralTone.ERROR
        else -> PeripheralTone.WARN
    }
    val toneColors = tone?.colors()
    val background = toneColors?.container ?: theme.surfaceContainerLow
    val content = toneColors?.content ?: theme.onSurface
    val shape = RoundedCornerShape(18.dp)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background, shape)
            .then(if (!hasAttention) Modifier else Modifier.clickable(onClick = onShow))
            .heightIn(min = 64.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag(SYSTEM_SUMMARY_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpIcon(
            icon = if (!hasAttention) CmpIcons.CheckCircle else CmpIcons.Warning,
            contentDescription = null,
            tint = if (!hasAttention) domain.success else content,
            modifier = Modifier.size(28.dp),
        )
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpText(
                text = if (!hasAttention) {
                    stringResource(Strings.string.peripherals_system_msg_all_online)
                } else {
                    stringResource(Strings.string.peripherals_system_format_attention, summary.attention)
                },
                color = content,
                fontWeight = FontWeight.Bold,
                style = CmpTextStyle.BodyLarge,
            )
            CmpText(
                text = stringResource(Strings.string.peripherals_system_format_summary, summary.total, summary.online),
                color = if (!hasAttention) theme.onSurfaceVariant else content,
                style = CmpTextStyle.Body,
            )
        }
        if (hasAttention) {
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                CmpText(
                    text = stringResource(Strings.string.peripherals_system_btn_show),
                    color = content,
                    fontWeight = FontWeight.Bold,
                    style = CmpTextStyle.Body,
                )
                CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/** «Подробнее»: все поля устройства, «Поморгать» (метки, датчики двери) и «Обновить» (все, кроме датчиков шин). */
@Composable
internal fun DeviceDetails(device: SystemPeripheral, blinking: Boolean, onBlink: () -> Unit, onUpdate: () -> Unit) {
    val theme = cmpThemeColors()
    val offline = device.link == PeripheralLink.OFFLINE
    val link = device.link.ui
    CmpColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DeviceIcon(type = device.type, state = if (offline) IconState.OFF else IconState.ON, size = 72.dp)
            CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpText(text = deviceTitle(device.type, device.number), style = CmpTextStyle.Title)
                StatusPill(label = link.label, tone = link.tone, big = true)
            }
        }
        if (offline) {
            CmpRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(theme.errorContainer, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CmpIcon(icon = CmpIcons.LinkOff, contentDescription = null, tint = theme.onErrorContainer)
                CmpText(
                    text = stringResource(Strings.string.peripherals_details_msg_offline),
                    color = theme.onErrorContainer,
                    style = CmpTextStyle.BodyLarge,
                )
            }
        }
        CmpColumn {
            DetailRow(stringResource(Strings.string.peripherals_details_label_type)) {
                ValueText(stringResource(device.type.ui.name))
            }
            DetailRow(stringResource(Strings.string.peripherals_details_label_number)) {
                ValueText(device.number.toString())
            }
            DetailRow(stringResource(Strings.string.peripherals_details_label_address)) {
                AddressText(device.address, large = true)
            }
            if (!offline) {
                device.signalDbm?.let { dbm ->
                    DetailRow(stringResource(Strings.string.peripherals_details_label_signal)) {
                        SignalBars(dbm = dbm, size = 22.dp, showClass = true)
                    }
                }
                device.voltage?.let { voltage ->
                    DetailRow(stringResource(Strings.string.peripherals_details_label_battery)) {
                        BatteryIndicator(
                            voltage = voltage,
                            percent = batteryPercent(device.type, voltage),
                            low = device.batteryLow(),
                            showPercent = true,
                        )
                    }
                }
            }
            device.metrics().forEach { metric ->
                val label = metric.label?.let { stringResource(it) }.orEmpty()
                DetailRow(label) {
                    ValueText(metric.value)
                }
            }
            device.flags().forEach { flag ->
                DetailRow(stringResource(Strings.string.peripherals_details_label_event)) {
                    ValueText(flag)
                }
            }
        }
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CmpBox(modifier = Modifier.size(8.dp).background(cmpDomainColors().success, CircleShape))
            CmpText(
                text = stringResource(Strings.string.peripherals_details_msg_live),
                color = theme.onSurfaceVariant,
                style = CmpTextStyle.Body,
            )
        }
        if (device.type.canEnterFirmwareMode) {
            DetailActions(device, offline, blinking, onBlink, onUpdate)
        }
    }
}

@Composable
private fun DetailActions(
    device: SystemPeripheral,
    offline: Boolean,
    blinking: Boolean,
    onBlink: () -> Unit,
    onUpdate: () -> Unit,
) {
    val canBlink = device.type.canBlink
    CmpColumn(modifier = Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (canBlink) {
            CmpOutlinedButton(
                onClick = onBlink,
                enabled = !offline && !blinking,
                modifier = Modifier.fillMaxWidth().height(ButtonHeight).testTag(SYSTEM_BLINK_TAG),
            ) {
                CmpIcon(icon = CmpIcons.Flare, contentDescription = null)
                CmpText(
                    text = stringResource(
                        if (blinking) {
                            Strings.string.peripherals_details_btn_blinking
                        } else {
                            Strings.string.peripherals_details_btn_blink
                        },
                    ),
                    modifier = Modifier.padding(start = 8.dp),
                    style = CmpTextStyle.BodyLarge,
                )
            }
        }
        CmpButton(
            onClick = onUpdate,
            enabled = !offline,
            modifier = Modifier.fillMaxWidth().height(ButtonHeight).testTag(SYSTEM_UPDATE_TAG),
        ) {
            CmpIcon(icon = CmpIcons.SystemUpdate, contentDescription = null)
            CmpText(
                text = stringResource(Strings.string.peripherals_details_btn_update),
                modifier = Modifier.padding(start = 8.dp),
                style = CmpTextStyle.BodyLarge,
            )
        }
        val hint = if (offline) {
            stringResource(Strings.string.peripherals_details_hint_unavailable)
        } else {
            listOfNotNull(
                stringResource(Strings.string.peripherals_details_hint_blink).takeIf { canBlink },
                stringResource(Strings.string.peripherals_details_hint_update),
            ).joinToString(" ")
        }
        CmpText(text = hint, color = cmpThemeColors().onSurfaceVariant, style = CmpTextStyle.Body)
    }
}

@Composable
private fun ValueText(text: String) {
    CmpText(text = text, fontWeight = FontWeight.SemiBold, style = CmpTextStyle.BodyLarge, textAlign = TextAlign.End)
}

@Composable
private fun DetailRow(label: String, value: @Composable () -> Unit) {
    val theme = cmpThemeColors()
    CmpColumn {
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CmpText(
                text = label,
                color = theme.onSurfaceVariant,
                style = CmpTextStyle.BodyLarge,
                modifier = Modifier.weight(DETAIL_LABEL_WEIGHT),
            )
            CmpBox(modifier = Modifier.weight(DETAIL_VALUE_WEIGHT), contentAlignment = Alignment.CenterEnd) { value() }
        }
        CmpBox(modifier = Modifier.fillMaxWidth().height(1.dp).background(theme.outlineVariant))
    }
}

private const val DETAIL_LABEL_WEIGHT = 0.4f
private const val DETAIL_VALUE_WEIGHT = 0.6f
