package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_battery_format_voltage
import ru.alarmtrade.specmp.core.localization.resources.peripherals_instruction_msg_none
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_cancel
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_close
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_done
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_instruction
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_pair
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_retry
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_btn_scan
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_error_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_failed_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_pir_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_success_title
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_format_waiting
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_hint_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_hint_pair
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_hint_pair_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_hint_update
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_item_delete
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_item_instruction
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_item_pair
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_item_update
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_label_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_label_mode
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_address
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_failed
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_success
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_msg_warning
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_status_later
import ru.alarmtrade.specmp.core.localization.resources.peripherals_pairing_status_optional
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_deleting
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_free
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_online
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_paired
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_slot_offline
import ru.alarmtrade.specmp.core.localization.resources.peripherals_status_value_waiting
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.DeviceIcon
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.IconState
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.PeripheralTone
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.StatusPill
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.colors
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.formatDecimal
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.InstructionSection
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.instruction
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.deviceTitle

internal const val PAIRING_SLOT_TAG_PREFIX = "peripherals_slot_"
internal const val PAIRING_PAIR_TAG_PREFIX = "peripherals_slot_pair_"
internal const val PAIRING_CANCEL_TAG = "peripherals_pairing_cancel"
internal const val PAIRING_ADDRESS_TAG = "peripherals_pairing_address"

private val BigButton = 56.dp
private const val FLASH_START_ALPHA = 0.5f
private const val FLASH_MILLIS = 1_400
private const val SCHEME_RATIO = 800f / 550f
private const val SECONDS_IN_MINUTE = 60
private const val RING_WARN_SECONDS = 20
private const val DISABLED_ALPHA = 0.8f

/**
 * Статус слота: занятый — по связи (у TPMS «Приживлено»), иначе «Свободный слот»;
 * ожидание и удаление — со спиннером.
 */
private class SlotStatus(val label: StringResource, val tone: PeripheralTone, val busy: Boolean)

private fun DynamicPeripheral.status(waiting: Boolean, deleting: Boolean): SlotStatus = when {
    waiting -> SlotStatus(Strings.string.peripherals_status_value_waiting, PeripheralTone.ACCENT, busy = true)

    deleting -> SlotStatus(Strings.string.peripherals_status_value_deleting, PeripheralTone.NEUTRAL, busy = true)

    !paired -> SlotStatus(Strings.string.peripherals_status_value_free, PeripheralTone.NEUTRAL, busy = false)

    type == PeripheralType.TPMS -> SlotStatus(
        Strings.string.peripherals_status_value_paired,
        PeripheralTone.OK,
        busy = false,
    )

    connected -> SlotStatus(Strings.string.peripherals_status_value_online, PeripheralTone.OK, busy = false)

    else -> SlotStatus(Strings.string.peripherals_status_value_slot_offline, PeripheralTone.WARN, busy = false)
}

private val DynamicPeripheral.isOffline: Boolean
    get() = paired && !connected && type != PeripheralType.TPMS

private fun DynamicPeripheral.iconState(waiting: Boolean): IconState = when {
    !paired || waiting -> IconState.FREE
    isOffline -> IconState.OFF
    else -> IconState.ON
}

@Composable
private fun slotBackground(waiting: Boolean, offline: Boolean): Color = when {
    waiting -> PeripheralTone.ACCENT.colors().row
    offline -> PeripheralTone.WARN.colors().row
    else -> Color.Transparent
}

@Composable
internal fun SlotRow(
    slot: DynamicPeripheral,
    waiting: Boolean,
    deleting: Boolean,
    selected: Boolean,
    flashSeq: Int?,
    canPair: Boolean,
    wide: Boolean,
    onClick: () -> Unit,
    onPair: () -> Unit,
) {
    val theme = cmpThemeColors()
    val status = slot.status(waiting, deleting)
    val offline = slot.isOffline
    val shape = RoundedCornerShape(18.dp)
    val background = slotBackground(waiting, offline)
    val success = cmpDomainColors().success
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashSeq) {
        if (flashSeq != null) {
            flash.snapTo(FLASH_START_ALPHA)
            flash.animateTo(0f, tween(FLASH_MILLIS))
        }
    }
    val border = if (waiting || selected) Modifier.border(2.dp, theme.primary, shape) else Modifier
    val busy = waiting || deleting
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background, shape)
            .then(border)
            .drawBehind { drawRect(success.copy(alpha = flash.value)) }
            .then(if (busy) Modifier else Modifier.clickable(onClick = onClick))
            .heightIn(min = 76.dp)
            .padding(10.dp)
            .testTag(PAIRING_SLOT_TAG_PREFIX + slot.slot),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DeviceIcon(type = slot.type, state = slot.iconState(waiting), size = 52.dp)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CmpText(
                text = deviceTitle(slot.type, slot.number),
                color = if (slot.paired) theme.onSurface else theme.onSurfaceVariant,
                style = CmpTextStyle.Headline2,
            )
            CmpRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusPill(label = status.label, tone = status.tone, busy = status.busy)
                SlotMeta(slot)
            }
        }
        when {
            !slot.paired && canPair && !busy -> CmpButton(
                onClick = onPair,
                modifier = Modifier.height(48.dp).testTag(PAIRING_PAIR_TAG_PREFIX + slot.slot),
            ) {
                if (wide) {
                    CmpIcon(
                        icon = CmpIcons.AddLink,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_pair),
                    style = CmpTextStyle.Paragraph2,
                )
            }

            !busy -> CmpIcon(
                icon = if (wide) CmpIcons.ChevronRight else CmpIcons.MoreVert,
                contentDescription = null,
                tint = theme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SlotMeta(slot: DynamicPeripheral) {
    val text = when {
        slot.pirAddress != null -> stringResource(
            Strings.string.peripherals_pairing_format_pir_address,
            slot.pirAddress,
        )

        slot.paired && slot.voltage != null ->
            stringResource(Strings.string.peripherals_battery_format_voltage, slot.voltage.formatDecimal(2))

        else -> null
    }
    if (text != null) CmpText(text = text, style = CmpTextStyle.Headline3, maxLines = 1)
}

/** Отсчёт режима приживления кольцом; последние 20 с — предупреждающим цветом. */
@Composable
internal fun CountRing(secondsLeft: Int, size: Dp) {
    val theme = cmpThemeColors()
    val color = if (secondsLeft <= RING_WARN_SECONDS) cmpDomainColors().warning else theme.primary
    CmpBox(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        CmpCircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            progress = secondsLeft.toFloat() / PAIRING_SECONDS,
            strokeWidth = 6.dp,
            color = color,
            trackColor = theme.surfaceContainerHighest,
        )
        val minutes = secondsLeft / SECONDS_IN_MINUTE
        val seconds = (secondsLeft % SECONDS_IN_MINUTE).toString().padStart(2, '0')
        CmpText(text = "$minutes:$seconds", style = CmpTextStyle.Headline1)
    }
}

/** Режим приживления: [panel] — правая панель широкого окна (инструкция всегда видна), иначе карточка над списком. */
@Composable
internal fun PairingMode(
    session: PairingSession,
    slot: DynamicPeripheral,
    panel: Boolean,
    instructionOpen: Boolean,
    onToggleInstruction: () -> Unit,
    onCancel: () -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onInstruction: () -> Unit,
) {
    val name = deviceTitle(slot.type, slot.number)
    if (session.outcome != PairingOutcome.WAITING) {
        PairingResult(session.outcome == PairingOutcome.SUCCESS, name, onClose, onRetry, onInstruction)
        return
    }
    val theme = cmpThemeColors()
    val shape = RoundedCornerShape(24.dp)
    val container = if (panel) {
        Modifier
    } else {
        Modifier
            .background(theme.surfaceContainerLow, shape)
            .border(2.dp, theme.primary, shape)
            .padding(14.dp)
    }
    CmpColumn(modifier = Modifier.fillMaxWidth().then(container), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CountRing(session.secondsLeft, if (panel) 96.dp else 72.dp)
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CmpCircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.5.dp)
                    CmpText(
                        text = stringResource(Strings.string.peripherals_pairing_label_mode),
                        color = theme.primary,
                        style = CmpTextStyle.Additional0,
                    )
                }
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_format_waiting, name),
                    style = CmpTextStyle.Headline1,
                )
            }
        }
        WarningBox(stringResource(Strings.string.peripherals_pairing_msg_warning))
        if (panel) {
            InstructionView(slot.type)
        } else {
            InstructionToggle(open = instructionOpen, onToggle = onToggleInstruction)
            if (instructionOpen) InstructionView(slot.type)
        }
        CmpOutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().height(BigButton).testTag(PAIRING_CANCEL_TAG),
        ) {
            CmpText(
                text = stringResource(Strings.string.peripherals_pairing_btn_cancel),
                style = CmpTextStyle.Paragraph1,
            )
        }
    }
}

@Composable
private fun PairingResult(
    success: Boolean,
    name: String,
    onClose: () -> Unit,
    onRetry: () -> Unit,
    onInstruction: () -> Unit,
) {
    val theme = cmpThemeColors()
    val domain = cmpDomainColors()
    val background = if (success) PeripheralTone.OK.colors().container else theme.errorContainer
    val content = if (success) theme.onSurface else theme.onErrorContainer
    CmpColumn(
        modifier = Modifier.fillMaxWidth().background(background, RoundedCornerShape(24.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CmpBox(
                modifier = Modifier.size(56.dp).background(if (success) domain.success else theme.error, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = if (success) CmpIcons.Check else CmpIcons.HourglassTop,
                    contentDescription = null,
                    tint = theme.onError,
                    modifier = Modifier.size(32.dp),
                )
            }
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CmpText(
                    text = stringResource(
                        if (success) {
                            Strings.string.peripherals_pairing_format_success_title
                        } else {
                            Strings.string.peripherals_pairing_format_failed_title
                        },
                        name,
                    ),
                    color = content,
                    style = CmpTextStyle.Headline1,
                )
                val message = if (success) {
                    Strings.string.peripherals_pairing_msg_success
                } else {
                    Strings.string.peripherals_pairing_msg_failed
                }
                CmpText(
                    text = stringResource(message),
                    color = content,
                    style = CmpTextStyle.Paragraph1,
                )
            }
        }
        if (success) {
            CmpButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(BigButton)) {
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_done),
                    style = CmpTextStyle.Paragraph1,
                )
            }
        } else {
            CmpButton(onClick = onRetry, modifier = Modifier.fillMaxWidth().height(BigButton)) {
                CmpIcon(icon = CmpIcons.Refresh, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_retry),
                    style = CmpTextStyle.Paragraph1,
                )
            }
            CmpOutlinedButton(onClick = onInstruction, modifier = Modifier.fillMaxWidth().height(BigButton)) {
                CmpIcon(icon = CmpIcons.MenuBook, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_instruction),
                    style = CmpTextStyle.Paragraph1,
                )
            }
            CmpTextButton(onClick = onClose, modifier = Modifier.fillMaxWidth().height(BigButton)) {
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_close),
                    style = CmpTextStyle.Paragraph1,
                )
            }
        }
    }
}

@Composable
private fun WarningBox(text: String) {
    val domain = cmpDomainColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().background(
            domain.warningContainer,
            RoundedCornerShape(14.dp),
        ).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = domain.onWarningContainer)
        CmpText(text = text, color = domain.onWarningContainer, style = CmpTextStyle.Paragraph2)
    }
}

@Composable
private fun InstructionToggle(open: Boolean, onToggle: () -> Unit) {
    val theme = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (open) Color.Transparent else theme.surfaceContainerHigh)
            .clickable(onClick = onToggle)
            .heightIn(min = 52.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CmpIcon(icon = CmpIcons.MenuBook, contentDescription = null, tint = theme.primary)
        CmpText(
            text = stringResource(Strings.string.peripherals_pairing_btn_instruction),
            style = CmpTextStyle.Headline2,
            modifier = Modifier.weight(1f),
        )
        CmpIcon(
            icon = if (open) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
            contentDescription = null,
            tint = theme.onSurfaceVariant,
        )
    }
}

/** Инструкция приживления: по разделам — модель, схемы, пронумерованные шаги. */
@Composable
internal fun InstructionView(type: PeripheralType) {
    val sections = type.instruction
    if (sections.isEmpty()) {
        CmpText(
            text = stringResource(Strings.string.peripherals_instruction_msg_none),
            color = cmpThemeColors().onSurfaceVariant,
            style = CmpTextStyle.Paragraph1,
        )
        return
    }
    CmpColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        sections.forEach { InstructionSectionView(it) }
    }
}

@Composable
private fun InstructionSectionView(section: InstructionSection) {
    val theme = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        section.title?.let {
            CmpText(
                text = stringResource(it),
                color = theme.primary,
                style = CmpTextStyle.Headline2,
            )
        }
        section.schemes.forEach { scheme ->
            CmpBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(SCHEME_RATIO)
                    .background(theme.surfaceContainerLowest, RoundedCornerShape(16.dp))
                    .border(1.dp, theme.outlineVariant, RoundedCornerShape(16.dp)),
            ) {
                CmpIcon(
                    painter = painterResource(scheme.ink),
                    contentDescription = null,
                    tint = theme.onSurface,
                    modifier = Modifier.fillMaxSize(),
                )
                scheme.color?.let {
                    CmpIcon(painter = painterResource(it), contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            }
        }
        section.steps.forEachIndexed { index, step ->
            CmpRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CmpBox(
                    modifier = Modifier.size(32.dp).background(theme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpText(
                        text = (index + 1).toString(),
                        color = theme.onPrimary,
                        style = CmpTextStyle.Headline3,
                    )
                }
                CmpText(
                    text = stringResource(step),
                    style = CmpTextStyle.Paragraph1,
                    modifier = Modifier.weight(1f).padding(top = 4.dp),
                )
            }
        }
    }
}

/** Действия слота: свободный — «Приживить», «Инструкция»; занятый — «Инструкция», «Обновить», «Удалить». */
@Composable
internal fun SlotActions(
    slot: DynamicPeripheral,
    onPair: () -> Unit,
    onInstruction: () -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit,
) {
    val status = slot.status(waiting = false, deleting = false)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            DeviceIcon(
                type = slot.type,
                state = slot.iconState(waiting = false),
                size = 60.dp,
            )
            CmpColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CmpText(text = deviceTitle(slot.type, slot.number), style = CmpTextStyle.Headline1)
                StatusPill(label = status.label, tone = status.tone)
            }
        }
        CmpColumn {
            if (!slot.paired) {
                ActionItem(
                    icon = CmpIcons.AddLink,
                    label = stringResource(Strings.string.peripherals_pairing_item_pair),
                    hint = stringResource(
                        if (slot.type == PeripheralType.TPMS) {
                            Strings.string.peripherals_pairing_hint_pair_address
                        } else {
                            Strings.string.peripherals_pairing_hint_pair
                        },
                    ),
                    onClick = onPair,
                )
                ActionItem(
                    CmpIcons.MenuBook,
                    stringResource(Strings.string.peripherals_pairing_item_instruction),
                    onClick = onInstruction,
                )
            } else {
                ActionItem(
                    CmpIcons.MenuBook,
                    stringResource(Strings.string.peripherals_pairing_item_instruction),
                    onClick = onInstruction,
                )
                ActionItem(
                    icon = CmpIcons.SystemUpdate,
                    label = stringResource(Strings.string.peripherals_pairing_item_update),
                    hint = stringResource(Strings.string.peripherals_pairing_hint_update),
                    tag = stringResource(Strings.string.peripherals_pairing_status_optional),
                    onClick = onUpdate,
                )
                ActionItem(
                    icon = CmpIcons.Delete,
                    label = stringResource(Strings.string.peripherals_pairing_item_delete),
                    danger = true,
                    onClick = onDelete,
                )
            }
        }
    }
}

@Composable
private fun ActionItem(
    icon: CmpIcons,
    label: String,
    hint: String? = null,
    tag: String? = null,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    val theme = cmpThemeColors()
    val color = if (danger) theme.error else theme.onSurface
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .heightIn(min = 64.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CmpIcon(icon = icon, contentDescription = null, tint = if (danger) theme.error else theme.primary)
        CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpText(text = label, color = color, style = CmpTextStyle.Headline2)
                tag?.let { PhaseTag(it) }
            }
            hint?.let { CmpText(text = it, color = theme.onSurfaceVariant, style = CmpTextStyle.Paragraph2) }
        }
    }
}

/** Метка этапа («Опционально», «Позже») — пунктиром, чтобы не путать с данными. */
@Composable
private fun PhaseTag(text: String) {
    val tertiary = cmpThemeColors().tertiary
    CmpBox(
        modifier = Modifier.border(
            1.5.dp,
            tertiary,
            RoundedCornerShape(6.dp),
        ).padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        CmpText(text = text, color = tertiary, style = CmpTextStyle.Additional0)
    }
}

/** Адрес датчика шины: 8 hex-символов; QR-сканер — позже. */
@Composable
internal fun AddressForm(
    address: String,
    error: Boolean,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: (() -> Unit)?,
) {
    val theme = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CmpText(text = stringResource(Strings.string.peripherals_pairing_msg_address), style = CmpTextStyle.Paragraph1)
        CmpTextField(
            value = address,
            onValueChange = onChange,
            label = stringResource(Strings.string.peripherals_pairing_label_address),
            isError = error,
            supportingText = {
                CmpText(
                    text = stringResource(
                        if (error) {
                            Strings.string.peripherals_pairing_error_address
                        } else {
                            Strings.string.peripherals_pairing_hint_address
                        },
                    ),
                    style = CmpTextStyle.Paragraph2,
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(PAIRING_ADDRESS_TAG),
        )
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(DISABLED_ALPHA)
                .border(1.5.dp, theme.outline, RoundedCornerShape(14.dp))
                .heightIn(min = 56.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CmpIcon(icon = CmpIcons.Search, contentDescription = null, tint = theme.onSurfaceVariant)
            CmpText(
                text = stringResource(Strings.string.peripherals_pairing_btn_scan),
                color = theme.onSurfaceVariant,
                style = CmpTextStyle.Headline2,
                modifier = Modifier.weight(1f),
            )
            PhaseTag(stringResource(Strings.string.peripherals_pairing_status_later))
        }
        CmpButton(
            onClick = onSubmit,
            enabled = address.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(BigButton),
        ) {
            CmpIcon(icon = CmpIcons.AddLink, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            CmpText(text = stringResource(Strings.string.peripherals_pairing_btn_pair), style = CmpTextStyle.Paragraph1)
        }
        if (onCancel != null) {
            CmpTextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().height(BigButton)) {
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_btn_cancel),
                    style = CmpTextStyle.Paragraph1,
                )
            }
        }
    }
}
