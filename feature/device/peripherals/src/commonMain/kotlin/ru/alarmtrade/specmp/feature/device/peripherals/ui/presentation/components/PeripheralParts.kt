package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.peripherals_battery_a11y_level
import ru.alarmtrade.specmp.core.localization.resources.peripherals_battery_format_percent
import ru.alarmtrade.specmp.core.localization.resources.peripherals_battery_format_voltage
import ru.alarmtrade.specmp.core.localization.resources.peripherals_battery_status_low
import ru.alarmtrade.specmp.core.localization.resources.peripherals_signal_a11y_level
import ru.alarmtrade.specmp.core.localization.resources.peripherals_signal_format_dbm
import ru.alarmtrade.specmp.core.localization.resources.peripherals_signal_value_excellent
import ru.alarmtrade.specmp.core.localization.resources.peripherals_signal_value_good
import ru.alarmtrade.specmp.core.localization.resources.peripherals_signal_value_very_weak
import ru.alarmtrade.specmp.core.localization.resources.peripherals_signal_value_weak
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.SignalClass
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.TypeIcon
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.batteryLevel
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.signalClassOf
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.ui

/** Поля списка: [top] — высота прозрачного топбара (контент уходит под него), [horizontal] — поле экрана. */
@Immutable
internal class ListInsets(val top: Dp, val horizontal: Dp) {
    fun list(): PaddingValues = PaddingValues(
        start = horizontal,
        end = horizontal,
        top = top + CmpSpacing.space8,
        bottom = CmpSpacing.space32,
    )
}

internal enum class PeripheralTone { OK, WARN, ERROR, NEUTRAL, ACCENT }

/** [container] — заливка плашки/строки (`*-highlight`), [content] — цвет иконки и акцента тона. */
internal class ToneColors(val container: Color, val content: Color)

@Composable
internal fun PeripheralTone.colors(): ToneColors {
    val colors = CmpTheme.colors
    return when (this) {
        PeripheralTone.OK -> ToneColors(colors.successHighlight, colors.successDefault)
        PeripheralTone.WARN -> ToneColors(colors.warningHighlight, colors.warningDefault)
        PeripheralTone.ERROR -> ToneColors(colors.errorHighlight, colors.errorDefault)
        PeripheralTone.NEUTRAL -> ToneColors(colors.backgroundSurfaceHighlight, colors.foregroundOnDarkSecondaryDefault)
        PeripheralTone.ACCENT -> ToneColors(colors.brandPrimaryHighlight, colors.brandPrimaryDefault)
    }
}

internal val PeripheralTone.tag: CmpTagTone
    get() = when (this) {
        PeripheralTone.OK -> CmpTagTone.Success
        PeripheralTone.WARN -> CmpTagTone.Warning
        PeripheralTone.ERROR -> CmpTagTone.Error
        PeripheralTone.NEUTRAL -> CmpTagTone.Neutral
        PeripheralTone.ACCENT -> CmpTagTone.Brand
    }

internal enum class IconState { ON, OFF, FREE }

private const val ICON_SHAPE_SHARE = 0.3f
private const val ICON_IMAGE_SHARE = 0.78f
private const val ICON_VECTOR_SHARE = 0.5f
private const val FREE_ALPHA = 0.75f
private val BadgeRing = 2.dp

/**
 * Иконка типа: «нет связи» — бейдж ([badge] = false — когда статус показывает строка списка),
 * свободный слот — рамка и приглушённо.
 */
@Composable
internal fun DeviceIcon(
    type: PeripheralType,
    state: IconState,
    modifier: Modifier = Modifier,
    size: Dp = CmpSize.touchMin,
    badge: Boolean = true,
    blinking: Boolean = false,
) {
    val colors = CmpTheme.colors
    val tint = if (state == IconState.ON) colors.brandPrimaryDefault else colors.foregroundOnDarkSecondaryDefault
    val shape = RoundedCornerShape(size * ICON_SHAPE_SHARE)
    val box = when (state) {
        IconState.FREE -> Modifier.alpha(
            FREE_ALPHA,
        ).border(CmpBorderWidth.listItem, colors.borderMediumHighlight, shape)

        else -> Modifier.background(colors.backgroundSurfaceHighlight, shape)
    }
    val blink = if (blinking) Modifier.blinkingBackground(colors.errorDefault, shape) else Modifier
    CmpBox(modifier = modifier.size(size).then(box).then(blink), contentAlignment = Alignment.Center) {
        when (val icon = type.ui.icon) {
            is TypeIcon.Drawable -> CmpIcon(
                painter = painterResource(icon.resource),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * ICON_IMAGE_SHARE),
            )

            is TypeIcon.Vector -> CmpIcon(
                icon = icon.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * ICON_VECTOR_SHARE),
            )
        }
        if (state == IconState.OFF && badge) {
            CmpBox(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(CmpSpacing.space4, CmpSpacing.space4)
                    .size(CmpSize.iconM)
                    .background(colors.backgroundPagePrimary, CircleShape)
                    .padding(BadgeRing)
                    .background(colors.errorDefault, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = CmpIcons.LinkOff,
                    contentDescription = null,
                    tint = colors.foregroundOnLightPrimaryDefault,
                    modifier = Modifier.size(CmpSize.iconS),
                )
            }
        }
    }
}

@Composable
private fun Modifier.blinkingBackground(color: Color, shape: Shape): Modifier {
    val alpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = BLINK_MAX_ALPHA,
        animationSpec = infiniteRepeatable(tween(BLINK_HALF_PERIOD_MILLIS), RepeatMode.Reverse),
    )
    return drawBehind { drawOutline(shape.createOutline(size, layoutDirection, this), color.copy(alpha = alpha)) }
}

private const val BLINK_MAX_ALPHA = 0.85f
private const val BLINK_HALF_PERIOD_MILLIS = 500

/** Статус — тег тона; ожидание и удаление — со спиннером. */
@Composable
internal fun StatusTag(
    label: StringResource,
    tone: PeripheralTone,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
) {
    CmpTag(
        text = stringResource(label),
        tone = tone.tag,
        loading = busy,
        modifier = modifier,
    )
}

internal val SignalClass.label: StringResource
    get() = when (this) {
        SignalClass.EXCELLENT -> Strings.string.peripherals_signal_value_excellent
        SignalClass.GOOD -> Strings.string.peripherals_signal_value_good
        SignalClass.WEAK -> Strings.string.peripherals_signal_value_weak
        SignalClass.VERY_WEAK -> Strings.string.peripherals_signal_value_very_weak
    }

@Composable
private fun SignalClass.color(): Color = when (this) {
    SignalClass.EXCELLENT, SignalClass.GOOD -> CmpTheme.colors.foregroundOnDarkPrimaryDefault
    SignalClass.WEAK -> CmpTheme.colors.warningDefault
    SignalClass.VERY_WEAK -> CmpTheme.colors.errorDefault
}

private const val BARS = 4
private const val BAR_WIDTH_DIVISOR = 5
private const val BAR_BASE_SHARE = 0.25f
private const val BAR_STEP_SHARE = 0.1875f
private val BarGap = 2.dp
private val BarRadius = 2.dp

/** Четыре полоски + «−67 dBm»; [showClass] — ещё подпись класса («Слабый»). */
@Composable
internal fun SignalBars(dbm: Int, modifier: Modifier = Modifier, showClass: Boolean = false) {
    val colors = CmpTheme.colors
    val signal = signalClassOf(dbm)
    val color = signal.color()
    val weak = signal == SignalClass.WEAK || signal == SignalClass.VERY_WEAK
    val classLabel = stringResource(signal.label)
    val a11y = stringResource(Strings.string.peripherals_signal_a11y_level, classLabel)
    val size = CmpSize.iconM
    CmpFlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpRow(
            modifier = Modifier.height(size).semantics { contentDescription = a11y },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(BarGap),
        ) {
            val filled = signal.ordinal + 1
            for (bar in 1..BARS) {
                val barColor = if (bar <= filled) color else colors.borderMediumHighlight
                CmpBox(
                    modifier = Modifier
                        .width(size / BAR_WIDTH_DIVISOR)
                        .height(size * (BAR_BASE_SHARE + bar * BAR_STEP_SHARE))
                        .background(barColor, RoundedCornerShape(BarRadius)),
                )
            }
        }
        CmpText(
            text = stringResource(Strings.string.peripherals_signal_format_dbm, kotlin.math.abs(dbm)),
            color = if (weak) color else colors.foregroundOnDarkPrimaryDefault,
            style = CmpTextStyle.Headline3,
            maxLines = 1,
        )
        if (showClass) {
            CmpText(
                text = classLabel,
                color = if (weak) color else colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
                maxLines = 1,
            )
        }
    }
}

private val BatteryWidth = 23.dp
private val BatteryHeight = 13.dp
private val BatteryCell = 9.dp
private val BatteryTipWidth = 3.dp
private val BatteryTipHeight = 6.dp
private val BatteryStroke = 2.dp
private val BatteryCellGap = 1.dp

/** Батарея: 4 деления + напряжение; низкий заряд — красным с подписью; [showPercent] — подпись «%» в «Подробнее». */
@Composable
internal fun BatteryIndicator(
    voltage: Float,
    percent: Int?,
    low: Boolean,
    modifier: Modifier = Modifier,
    showPercent: Boolean = false,
) {
    val colors = CmpTheme.colors
    val color = if (low) colors.errorDefault else colors.foregroundOnDarkPrimaryDefault
    val level = if (low) 1 else percent?.let(::batteryLevel) ?: BARS
    val a11y = if (low) {
        stringResource(Strings.string.peripherals_battery_status_low)
    } else {
        stringResource(Strings.string.peripherals_battery_a11y_level, level)
    }
    CmpFlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpRow(
            modifier = Modifier.semantics { contentDescription = a11y },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpRow(
                modifier = Modifier
                    .width(BatteryWidth)
                    .height(BatteryHeight)
                    .border(BatteryStroke, color, RoundedCornerShape(CmpSpacing.space4))
                    .padding(BatteryStroke),
                horizontalArrangement = Arrangement.spacedBy(BatteryCellGap),
            ) {
                for (cell in 1..BARS) {
                    CmpBox(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(BatteryCell)
                            .background(
                                if (cell <=
                                    level
                                ) {
                                    color
                                } else {
                                    Color.Transparent
                                },
                                RoundedCornerShape(BatteryCellGap),
                            ),
                    )
                }
            }
            CmpBox(
                modifier = Modifier
                    .width(BatteryTipWidth)
                    .height(BatteryTipHeight)
                    .background(color, RoundedCornerShape(topEnd = BarRadius, bottomEnd = BarRadius)),
            )
        }
        CmpText(
            text = stringResource(Strings.string.peripherals_battery_format_voltage, voltage.formatDecimal(2)),
            color = color,
            style = CmpTextStyle.Headline3,
            maxLines = 1,
        )
        when {
            low -> CmpText(
                text = stringResource(Strings.string.peripherals_battery_status_low),
                color = colors.errorDefault,
                style = CmpTextStyle.Headline3,
                maxLines = 1,
            )

            showPercent && percent != null -> CmpText(
                text = stringResource(Strings.string.peripherals_battery_format_percent, percent),
                color = colors.foregroundOnDarkSecondaryDefault,
                style = CmpTextStyle.Paragraph2,
                maxLines = 1,
            )
        }
    }
}

private const val DECIMAL_BASE = 10

/** «2,95» — запятая, как в макете (числа без локали платформы). */
internal fun Float.formatDecimal(digits: Int): String {
    var scale = 1
    repeat(digits) { scale *= DECIMAL_BASE }
    val scaled = kotlin.math.round(this * scale).toLong()
    val sign = if (scaled < 0) "-" else ""
    val abs = kotlin.math.abs(scaled)
    val whole = abs / scale
    if (digits == 0) return "$sign$whole"
    val fraction = (abs % scale).toString().padStart(digits, '0')
    return "$sign$whole,$fraction"
}

@Composable
internal fun GroupHeader(title: String, trailing: String?, modifier: Modifier = Modifier) {
    val color = CmpTheme.colors.foregroundOnDarkSecondaryDefault
    CmpRow(
        modifier = modifier.fillMaxWidth().padding(
            start = CmpSpacing.space4,
            end = CmpSpacing.space4,
            top = CmpSpacing.space16,
            bottom = CmpSpacing.space4,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = title,
            color = color,
            style = CmpTextStyle.Additional0,
            uppercase = true,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) CmpText(text = trailing, color = color, style = CmpTextStyle.Additional0)
    }
}

private val SkeletonLabel = 120.dp

/** Скелетон строки: заголовок группы, иконка и две строки. */
@Composable
internal fun SkeletonRow(modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12)) {
        CmpSkeleton(modifier = Modifier.width(SkeletonLabel).height(CmpSpacing.space12))
        CmpRow(
            modifier = Modifier.fillMaxWidth().heightIn(
                min = CmpSize.listItem,
            ).padding(horizontal = CmpSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
        ) {
            CmpSkeleton(modifier = Modifier.size(CmpSize.touchMin), shape = RoundedCornerShape(CmpRadius.cardSmall))
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8)) {
                CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE).height(CmpSpacing.space16))
                CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_SUB).height(CmpSpacing.space12))
            }
        }
    }
}

private const val SKELETON_TITLE = 0.6f
private const val SKELETON_SUB = 0.4f
