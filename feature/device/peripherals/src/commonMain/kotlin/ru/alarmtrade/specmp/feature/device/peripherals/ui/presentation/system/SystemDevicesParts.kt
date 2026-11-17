package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpLinearProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpInfoRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItemStatus
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
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
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.StatusTag
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
private val SpinnerStroke = 2.dp

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

private fun SystemPeripheral.rowStatus(): CmpListItemStatus? = when {
    link == PeripheralLink.OFFLINE -> CmpListItemStatus.Error
    needsAttention() -> CmpListItemStatus.Warn
    else -> null
}

@Composable
private fun DeviceRowDetails(device: SystemPeripheral, flags: List<String>) {
    val metrics = if (device.link == PeripheralLink.OFFLINE) emptyList() else device.metrics()
    CmpFlowRow(
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Indicators(device)
        metrics.forEach { CmpText(text = it.inline(), style = CmpTextStyle.Paragraph3) }
        flags.forEach { CmpTag(text = it, tone = CmpTagTone.Brand) }
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
    val status = device.rowStatus()
    val offline = device.link == PeripheralLink.OFFLINE
    val flashColor = CmpTheme.colors.brandPrimaryDefault
    val shape = RoundedCornerShape(CmpRadius.listItem)
    val rowTone = status?.let { if (it == CmpListItemStatus.Error) PeripheralTone.ERROR else PeripheralTone.WARN }
        ?.colors()?.container ?: Color.Transparent
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashSeq) {
        if (flashSeq != null) {
            flash.snapTo(FLASH_START_ALPHA)
            flash.animateTo(0f, tween(FLASH_MILLIS))
        }
    }
    val flags = device.flags()
    val below: (@Composable ColumnScope.() -> Unit)? = if (!offline || flags.isNotEmpty()) {
        { DeviceRowDetails(device, flags) }
    } else {
        null
    }
    CmpListItem(
        title = deviceTitle(device.type, device.number),
        subtitle = device.address,
        status = status,
        onClick = onClick,
        selected = selected,
        titleMaxLines = 2,
        leading = {
            DeviceIcon(type = device.type, state = if (offline) IconState.OFF else IconState.ON, badge = false)
        },
        below = below,
        trailing = { StatusTag(label = link.label, tone = link.tone) },
        modifier = modifier
            .fillMaxWidth()
            .background(rowTone, shape)
            .clip(shape)
            .drawBehind { drawRect(flashColor.copy(alpha = flash.value)) }
            .testTag(SYSTEM_ROW_TAG_PREFIX + device.address),
    )
}

/** «Базовый блок / BLE-модуль»: модель, прошивка, версия BLE — без кнопок (решение макета). */
@Composable
internal fun BlockHeader(deviceName: String?, firmware: String?, bluetoothVersion: String?) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        HeaderCell(
            label = stringResource(Strings.string.peripherals_system_label_block),
            value = deviceName.orEmpty(),
            sub = firmware?.let { stringResource(Strings.string.peripherals_system_format_firmware, it) },
            modifier = Modifier.weight(1f),
        )
        HeaderCell(
            label = stringResource(Strings.string.peripherals_system_label_ble),
            value = bluetoothVersion
                ?.let { stringResource(Strings.string.peripherals_system_format_version, it) }
                .orEmpty(),
            sub = null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HeaderCell(label: String, value: String, sub: String?, modifier: Modifier) {
    val secondary = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
        CmpText(text = label, color = secondary, style = CmpTextStyle.Additional0)
        CmpText(text = value, style = CmpTextStyle.Headline2, maxLines = 2)
        if (sub != null) CmpText(text = sub, color = secondary, style = CmpTextStyle.Paragraph3)
    }
}

/** «Опрос блока… N из M» с полосой — только в первом проходе. */
@Composable
internal fun PollProgress(polled: Int, total: Int) {
    val colors = CmpTheme.colors
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(vertical = CmpSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpCircularProgressIndicator(modifier = Modifier.size(CmpSize.iconS), strokeWidth = SpinnerStroke)
            CmpText(
                text = stringResource(Strings.string.peripherals_system_loading_poll),
                style = CmpTextStyle.Headline3,
                modifier = Modifier.weight(1f),
            )
            CmpText(
                text = stringResource(Strings.string.peripherals_system_format_progress, polled, total),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph3,
            )
        }
        CmpLinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(CmpSpacing.space4).clip(RoundedCornerShape(CmpRadius.round)),
            progress = if (total == 0) 0f else polled.toFloat() / total,
            color = colors.brandPrimaryDefault,
            trackColor = colors.backgroundSurfaceHighlight,
        )
    }
}

/** Сводка после прохода: «Требуют внимания: N» (ошибкой — если кто-то без связи) или «Все устройства на связи». */
@Composable
internal fun Summary(summary: DevicesSummary, onShow: () -> Unit) {
    val hasAttention = summary.attention > 0
    val colors = CmpTheme.colors
    val tone = when {
        !hasAttention -> null
        summary.anyOffline -> PeripheralTone.ERROR
        else -> PeripheralTone.WARN
    }
    val toneColors = tone?.colors()
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!hasAttention) Modifier else Modifier.cmpPressable(onClick = onShow, shape = shape))
            .background(toneColors?.container ?: colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .heightIn(min = CmpSize.listItem)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12)
            .testTag(SYSTEM_SUMMARY_TAG),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(
            icon = if (!hasAttention) CmpIcons.CheckCircle else CmpIcons.Warning,
            contentDescription = null,
            tint = toneColors?.content ?: colors.successDefault,
            modifier = Modifier.size(CmpSize.iconL),
        )
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            CmpText(
                text = if (!hasAttention) {
                    stringResource(Strings.string.peripherals_system_msg_all_online)
                } else {
                    stringResource(Strings.string.peripherals_system_format_attention, summary.attention)
                },
                style = CmpTextStyle.Headline2,
            )
            CmpText(
                text = stringResource(Strings.string.peripherals_system_format_summary, summary.total, summary.online),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph3,
            )
        }
        if (toneColors != null) {
            CmpRow(verticalAlignment = Alignment.CenterVertically) {
                CmpText(
                    text = stringResource(Strings.string.peripherals_system_btn_show),
                    color = toneColors.content,
                    style = CmpTextStyle.Headline3,
                    uppercase = true,
                )
                CmpIcon(
                    icon = CmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = toneColors.content,
                    modifier = Modifier.size(CmpSize.iconM),
                )
            }
        }
    }
}

/** «Подробнее»: все поля устройства, «Поморгать» (метки, датчики двери) и «Обновить» (все, кроме датчиков шин). */
@Composable
internal fun DeviceDetails(device: SystemPeripheral, blinking: Boolean, onBlink: () -> Unit, onUpdate: () -> Unit) {
    val colors = CmpTheme.colors
    val offline = device.link == PeripheralLink.OFFLINE
    val link = device.link.ui
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            DeviceIcon(
                type = device.type,
                state = if (offline) IconState.OFF else IconState.ON,
                size = CmpSize.iconXxl,
            )
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpText(text = deviceTitle(device.type, device.number), style = CmpTextStyle.Headline1)
                StatusTag(label = link.label, tone = link.tone)
            }
        }
        if (offline) OfflineNotice()
        CmpColumn {
            CmpInfoRow(
                label = stringResource(Strings.string.peripherals_details_label_type),
                value = stringResource(device.type.ui.name),
                compact = true,
            )
            CmpInfoRow(
                label = stringResource(Strings.string.peripherals_details_label_number),
                value = device.number.toString(),
                compact = true,
            )
            CmpInfoRow(
                label = stringResource(Strings.string.peripherals_details_label_address),
                value = device.address,
                mono = true,
                compact = true,
            )
            if (!offline) {
                device.signalDbm?.let { dbm ->
                    IndicatorRow(stringResource(Strings.string.peripherals_details_label_signal)) {
                        SignalBars(dbm = dbm, showClass = true)
                    }
                }
                device.voltage?.let { voltage ->
                    IndicatorRow(stringResource(Strings.string.peripherals_details_label_battery)) {
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
                CmpInfoRow(
                    label = metric.label?.let { stringResource(it) }.orEmpty(),
                    value = metric.value,
                    compact = true,
                )
            }
            device.flags().forEach { flag ->
                CmpInfoRow(
                    label = stringResource(Strings.string.peripherals_details_label_event),
                    value = flag,
                    compact = true,
                )
            }
        }
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            CmpBox(modifier = Modifier.size(CmpSpacing.space8).background(colors.successDefault, CircleShape))
            CmpText(
                text = stringResource(Strings.string.peripherals_details_msg_live),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Additional0,
            )
        }
        if (device.type.canEnterFirmwareMode) {
            DetailActions(device, offline, blinking, onBlink, onUpdate)
        }
    }
}

@Composable
private fun OfflineNotice() {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.errorHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.LinkOff, contentDescription = null, tint = colors.errorDefault)
        CmpText(text = stringResource(Strings.string.peripherals_details_msg_offline), style = CmpTextStyle.Paragraph2)
    }
}

/** Как `CmpInfoRow(compact = true)`, но значение — индикатор (полоски сигнала, батарея). */
@Composable
private fun IndicatorRow(label: String, value: @Composable () -> Unit) {
    CmpColumn {
        CmpColumn(
            modifier = Modifier.fillMaxWidth().heightIn(
                min = IndicatorRowHeight,
            ).padding(vertical = CmpSpacing.space12),
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4, Alignment.CenterVertically),
        ) {
            CmpText(
                text = label,
                color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Additional0,
            )
            value()
        }
        CmpDivider()
    }
}

private val IndicatorRowHeight = 72.dp

@Composable
private fun DetailActions(
    device: SystemPeripheral,
    offline: Boolean,
    blinking: Boolean,
    onBlink: () -> Unit,
    onUpdate: () -> Unit,
) {
    val canBlink = device.type.canBlink
    CmpColumn(
        modifier = Modifier.padding(top = CmpSpacing.space4),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        if (canBlink) {
            CmpButton(
                text = stringResource(
                    if (blinking) {
                        Strings.string.peripherals_details_btn_blinking
                    } else {
                        Strings.string.peripherals_details_btn_blink
                    },
                ),
                onClick = onBlink,
                enabled = !offline && !blinking,
                type = CmpButtonType.Secondary,
                icon = CmpIcons.Flare,
                fullWidth = true,
                modifier = Modifier.testTag(SYSTEM_BLINK_TAG),
            )
        }
        CmpButton(
            text = stringResource(Strings.string.peripherals_details_btn_update),
            onClick = onUpdate,
            enabled = !offline,
            icon = CmpIcons.SystemUpdate,
            fullWidth = true,
            modifier = Modifier.testTag(SYSTEM_UPDATE_TAG),
        )
        val hint = if (offline) {
            stringResource(Strings.string.peripherals_details_hint_unavailable)
        } else {
            listOfNotNull(
                stringResource(Strings.string.peripherals_details_hint_blink).takeIf { canBlink },
                stringResource(Strings.string.peripherals_details_hint_update),
            ).joinToString(" ")
        }
        CmpText(
            text = hint,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Paragraph3,
        )
    }
}
