package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.pairing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpButtonType
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.DynamicPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.DeviceIcon
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.IconState
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.PeripheralTone
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.StatusTag
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.colors
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components.formatDecimal
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.InstructionSection
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.instruction
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system.deviceTitle

internal const val PAIRING_SLOT_TAG_PREFIX = "peripherals_slot_"
internal const val PAIRING_PAIR_TAG_PREFIX = "peripherals_slot_pair_"
internal const val PAIRING_CANCEL_TAG = "peripherals_pairing_cancel"
internal const val PAIRING_ADDRESS_TAG = "peripherals_pairing_address"

private val RingPanel = 96.dp
private val RingCard = 72.dp
private val RingStroke = 6.dp
private val SpinnerStroke = 2.dp
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
    waiting -> PeripheralTone.ACCENT.colors().container
    offline -> PeripheralTone.WARN.colors().container
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
    val colors = CmpTheme.colors
    val status = slot.status(waiting, deleting)
    val shape = RoundedCornerShape(CmpRadius.listItem)
    val background = slotBackground(waiting, slot.isOffline)
    val flashColor = colors.successDefault
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashSeq) {
        if (flashSeq != null) {
            flash.snapTo(FLASH_START_ALPHA)
            flash.animateTo(0f, tween(FLASH_MILLIS))
        }
    }
    val busy = waiting || deleting
    val showPair = !slot.paired && canPair && !busy
    CmpListItem(
        title = deviceTitle(slot.type, slot.number),
        onClick = if (busy) null else onClick,
        selected = waiting || selected,
        titleMaxLines = 2,
        leading = { DeviceIcon(type = slot.type, state = slot.iconState(waiting)) },
        below = {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                StatusTag(label = status.label, tone = status.tone, busy = status.busy)
                SlotMeta(slot)
            }
        },
        trailing = when {
            showPair -> {
                {
                    CmpButton(
                        text = stringResource(Strings.string.peripherals_pairing_btn_pair),
                        onClick = onPair,
                        size = CmpButtonSize.S,
                        modifier = Modifier.testTag(PAIRING_PAIR_TAG_PREFIX + slot.slot),
                    )
                }
            }

            !busy && !wide -> {
                {
                    CmpIcon(
                        icon = CmpIcons.MoreVert,
                        contentDescription = null,
                        tint = colors.foregroundOnDarkSecondaryDefault,
                    )
                }
            }

            else -> null
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape)
            .clip(shape)
            .drawBehind { drawRect(flashColor.copy(alpha = flash.value)) }
            .testTag(PAIRING_SLOT_TAG_PREFIX + slot.slot),
    )
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
    val colors = CmpTheme.colors
    val color = if (secondsLeft <= RING_WARN_SECONDS) colors.warningDefault else colors.brandPrimaryDefault
    CmpBox(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        CmpCircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            progress = secondsLeft.toFloat() / PAIRING_SECONDS,
            strokeWidth = RingStroke,
            color = color,
            trackColor = colors.backgroundSurfaceHighlight,
        )
        val minutes = secondsLeft / SECONDS_IN_MINUTE
        val seconds = (secondsLeft % SECONDS_IN_MINUTE).toString().padStart(2, '0')
        CmpText(
            text = "$minutes:$seconds",
            style = if (size >=
                RingPanel
            ) {
                CmpTextStyle.Title2
            } else {
                CmpTextStyle.Headline1
            },
        )
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
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    val container = if (panel) {
        Modifier
    } else {
        Modifier
            .background(colors.backgroundPagePrimary, shape)
            .background(colors.backgroundSurfaceCard, shape)
            .border(CmpBorderWidth.listItem, colors.brandPrimaryDefault, shape)
            .padding(CmpSpacing.space16)
    }
    CmpColumn(
        modifier = Modifier.fillMaxWidth().then(container),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CountRing(session.secondsLeft, if (panel) RingPanel else RingCard)
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    CmpCircularProgressIndicator(
                        modifier = Modifier.size(CmpSpacing.space12),
                        strokeWidth = SpinnerStroke,
                    )
                    CmpText(
                        text = stringResource(Strings.string.peripherals_pairing_label_mode),
                        color = colors.brandPrimaryDefault,
                        style = CmpTextStyle.Additional0,
                        uppercase = true,
                    )
                }
                CmpText(
                    text = stringResource(Strings.string.peripherals_pairing_format_waiting, name),
                    style = if (panel) CmpTextStyle.Headline1 else CmpTextStyle.Headline2,
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
        CmpButton(
            text = stringResource(Strings.string.peripherals_pairing_btn_cancel),
            onClick = onCancel,
            type = CmpButtonType.Secondary,
            fullWidth = true,
            modifier = Modifier.testTag(PAIRING_CANCEL_TAG),
        )
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
    val colors = CmpTheme.colors
    val tone = if (success) PeripheralTone.OK.colors() else PeripheralTone.ERROR.colors()
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundPagePrimary, shape)
            .background(tone.container, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space16),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
    ) {
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpBox(
                modifier = Modifier.size(CmpSize.iconXxl).background(tone.content, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = if (success) CmpIcons.Check else CmpIcons.HourglassTop,
                    contentDescription = null,
                    tint = colors.foregroundOnLightPrimaryDefault,
                    modifier = Modifier.size(CmpSize.iconL),
                )
            }
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
                CmpText(
                    text = stringResource(
                        if (success) {
                            Strings.string.peripherals_pairing_format_success_title
                        } else {
                            Strings.string.peripherals_pairing_format_failed_title
                        },
                        name,
                    ),
                    style = CmpTextStyle.Headline1,
                )
                val message = if (success) {
                    Strings.string.peripherals_pairing_msg_success
                } else {
                    Strings.string.peripherals_pairing_msg_failed
                }
                CmpText(text = stringResource(message), style = CmpTextStyle.Paragraph2)
            }
        }
        if (success) {
            CmpButton(
                text = stringResource(Strings.string.peripherals_pairing_btn_done),
                onClick = onClose,
                fullWidth = true,
            )
        } else {
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpButton(
                    text = stringResource(Strings.string.peripherals_pairing_btn_retry),
                    onClick = onRetry,
                    icon = CmpIcons.Refresh,
                    fullWidth = true,
                )
                CmpButton(
                    text = stringResource(Strings.string.peripherals_pairing_btn_instruction),
                    onClick = onInstruction,
                    type = CmpButtonType.Secondary,
                    icon = CmpIcons.MenuBook,
                    fullWidth = true,
                )
                CmpButton(
                    text = stringResource(Strings.string.peripherals_pairing_btn_close),
                    onClick = onClose,
                    type = CmpButtonType.Tertiary,
                    fullWidth = true,
                )
            }
        }
    }
}

@Composable
private fun WarningBox(text: String) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardSmall)
    CmpRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.warningHighlight, shape)
            .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
            .padding(CmpSpacing.space12),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpIcon(icon = CmpIcons.Warning, contentDescription = null, tint = colors.warningDefault)
        CmpText(text = text, style = CmpTextStyle.Paragraph2)
    }
}

@Composable
private fun InstructionToggle(open: Boolean, onToggle: () -> Unit) {
    CmpListItem(
        title = stringResource(Strings.string.peripherals_pairing_btn_instruction),
        icon = CmpIcons.MenuBook,
        onClick = onToggle,
        flat = open,
        trailing = {
            CmpIcon(
                icon = if (open) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                contentDescription = null,
                tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Инструкция приживления: по разделам — модель, схемы, пронумерованные шаги. */
@Composable
internal fun InstructionView(type: PeripheralType) {
    val sections = type.instruction
    if (sections.isEmpty()) {
        CmpText(
            text = stringResource(Strings.string.peripherals_instruction_msg_none),
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            style = CmpTextStyle.Paragraph2,
        )
        return
    }
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space20)) {
        sections.forEach { InstructionSectionView(it) }
    }
}

@Composable
private fun InstructionSectionView(section: InstructionSection) {
    val colors = CmpTheme.colors
    val shape = RoundedCornerShape(CmpRadius.cardMedium)
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        section.title?.let {
            CmpText(
                text = stringResource(it),
                color = colors.brandPrimaryDefault,
                style = CmpTextStyle.Headline3,
            )
        }
        section.schemes.forEach { scheme ->
            CmpBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(SCHEME_RATIO)
                    .background(colors.backgroundSurfaceCard, shape)
                    .border(CmpBorderWidth.listItem, colors.borderHighlight, shape),
            ) {
                CmpIcon(
                    painter = painterResource(scheme.ink),
                    contentDescription = null,
                    tint = colors.foregroundOnDarkPrimaryDefault,
                    modifier = Modifier.fillMaxSize(),
                )
                scheme.color?.let {
                    CmpIcon(painter = painterResource(it), contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            }
        }
        section.steps.forEachIndexed { index, step ->
            CmpRow(horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
                CmpBox(
                    modifier = Modifier.size(CmpSize.iconL).background(colors.brandPrimaryDefault, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CmpText(
                        text = (index + 1).toString(),
                        color = colors.onBrand,
                        style = CmpTextStyle.Headline3,
                    )
                }
                CmpText(
                    text = stringResource(step),
                    style = CmpTextStyle.Paragraph2,
                    modifier = Modifier.weight(1f).padding(top = CmpSpacing.space4),
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
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            DeviceIcon(type = slot.type, state = slot.iconState(waiting = false), size = CmpSize.iconXxl)
            CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpText(text = deviceTitle(slot.type, slot.number), style = CmpTextStyle.Headline1)
                StatusTag(label = status.label, tone = status.tone)
            }
        }
        CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4)) {
            val instruction = stringResource(Strings.string.peripherals_pairing_item_instruction)
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
                ActionItem(CmpIcons.MenuBook, instruction, onClick = onInstruction)
            } else {
                ActionItem(CmpIcons.MenuBook, instruction, onClick = onInstruction)
                ActionItem(
                    icon = CmpIcons.SystemUpdate,
                    label = stringResource(Strings.string.peripherals_pairing_item_update),
                    hint = stringResource(Strings.string.peripherals_pairing_hint_update),
                    tag = stringResource(Strings.string.peripherals_pairing_status_optional),
                    onClick = onUpdate,
                )
                CmpButton(
                    text = stringResource(Strings.string.peripherals_pairing_item_delete),
                    onClick = onDelete,
                    type = CmpButtonType.TertiaryDanger,
                    icon = CmpIcons.Delete,
                    fullWidth = true,
                )
            }
        }
    }
}

@Composable
private fun ActionItem(icon: CmpIcons, label: String, hint: String? = null, tag: String? = null, onClick: () -> Unit) {
    CmpListItem(
        title = label,
        subtitle = hint,
        icon = icon,
        onClick = onClick,
        flat = true,
        below = tag?.let { { CmpTag(text = it, tone = CmpTagTone.Outline) } },
        trailing = {},
        modifier = Modifier.fillMaxWidth(),
    )
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
    val colors = CmpTheme.colors
    CmpColumn(verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16)) {
        CmpText(text = stringResource(Strings.string.peripherals_pairing_msg_address), style = CmpTextStyle.Paragraph2)
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
                    style = CmpTextStyle.Paragraph3,
                )
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag(PAIRING_ADDRESS_TAG),
        )
        val shape = RoundedCornerShape(CmpRadius.cardSmall)
        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(DISABLED_ALPHA)
                .border(CmpBorderWidth.inputs, colors.borderMediumHighlight, shape)
                .heightIn(min = CmpSize.input)
                .padding(horizontal = CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
        ) {
            CmpIcon(icon = CmpIcons.Search, contentDescription = null, tint = colors.foregroundOnDarkSecondaryDefault)
            CmpText(
                text = stringResource(Strings.string.peripherals_pairing_btn_scan),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Headline3,
                modifier = Modifier.weight(1f),
            )
            CmpTag(text = stringResource(Strings.string.peripherals_pairing_status_later), tone = CmpTagTone.Outline)
        }
        CmpButton(
            text = stringResource(Strings.string.peripherals_pairing_btn_pair),
            onClick = onSubmit,
            enabled = address.isNotEmpty(),
            icon = CmpIcons.AddLink,
            fullWidth = true,
        )
        if (onCancel != null) {
            CmpButton(
                text = stringResource(Strings.string.peripherals_pairing_btn_cancel),
                onClick = onCancel,
                type = CmpButtonType.Tertiary,
                fullWidth = true,
            )
        }
    }
}
