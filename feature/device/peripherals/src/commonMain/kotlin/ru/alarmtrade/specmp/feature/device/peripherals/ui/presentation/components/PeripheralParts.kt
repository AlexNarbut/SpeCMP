package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import ru.alarmtrade.specmp.core.ui.theme.cmpDomainColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.SignalClass
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.TypeIcon
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.batteryLevel
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.signalClassOf
import ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model.ui

/** Тоны статусов из макета; предупреждение — `cmpDomainColors().warning*` (в Material-схеме роли нет). */
internal enum class PeripheralTone { OK, WARN, ERROR, NEUTRAL, ACCENT }

internal class ToneColors(val container: Color, val content: Color, val dot: Color, val row: Color)

private const val SUCCESS_TINT = 0.22f
private const val WARN_ROW_ALPHA = 0.55f
private const val ERROR_ROW_ALPHA = 0.7f
private const val ACCENT_ROW_ALPHA = 0.6f

@Composable
internal fun PeripheralTone.colors(): ToneColors {
    val theme = cmpThemeColors()
    val domain = cmpDomainColors()
    return when (this) {
        PeripheralTone.OK -> ToneColors(
            container = domain.success.copy(alpha = SUCCESS_TINT).compositeOverOpaque(theme.surfaceContainerHigh),
            content = theme.onSurface,
            dot = domain.success,
            row = Color.Transparent,
        )

        PeripheralTone.WARN -> ToneColors(
            container = domain.warningContainer,
            content = domain.onWarningContainer,
            dot = domain.warning,
            row = domain.warningContainer.copy(alpha = WARN_ROW_ALPHA),
        )

        PeripheralTone.ERROR -> ToneColors(
            container = theme.errorContainer,
            content = theme.onErrorContainer,
            dot = theme.error,
            row = theme.errorContainer.copy(alpha = ERROR_ROW_ALPHA),
        )

        PeripheralTone.NEUTRAL -> ToneColors(
            container = theme.surfaceContainerHighest,
            content = theme.onSurface,
            dot = theme.outline,
            row = Color.Transparent,
        )

        PeripheralTone.ACCENT -> ToneColors(
            container = theme.primaryContainer,
            content = theme.onPrimaryContainer,
            dot = theme.primary,
            row = theme.primaryContainer.copy(alpha = ACCENT_ROW_ALPHA),
        )
    }
}

private fun Color.compositeOverOpaque(background: Color): Color = Color(
    red = red * alpha + background.red * (1 - alpha),
    green = green * alpha + background.green * (1 - alpha),
    blue = blue * alpha + background.blue * (1 - alpha),
)

internal enum class IconState { ON, OFF, FREE }

private const val ICON_SHAPE_SHARE = 0.3f
private const val ICON_IMAGE_SHARE = 0.78f
private const val ICON_VECTOR_SHARE = 0.5f
private const val FREE_ALPHA = 0.75f

/** Иконка типа: «нет связи» — бейдж, свободный слот — пунктирная рамка и приглушённо. */
@Composable
internal fun DeviceIcon(type: PeripheralType, state: IconState, size: Dp, modifier: Modifier = Modifier) {
    val theme = cmpThemeColors()
    val tint = if (state == IconState.ON) theme.primary else theme.onSurfaceVariant
    val shape = RoundedCornerShape(size * ICON_SHAPE_SHARE)
    val box = when (state) {
        IconState.FREE -> Modifier.alpha(FREE_ALPHA).border(2.dp, theme.outline, shape)
        else -> Modifier.background(theme.surfaceContainerHigh, shape)
    }
    CmpBox(modifier = modifier.size(size).then(box), contentAlignment = Alignment.Center) {
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
        if (state == IconState.OFF) {
            CmpBox(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(4.dp, 4.dp)
                    .size(24.dp)
                    .background(theme.surface, CircleShape)
                    .padding(2.dp)
                    .background(theme.error, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CmpIcon(
                    icon = CmpIcons.LinkOff,
                    contentDescription = null,
                    tint = theme.onError,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
internal fun StatusPill(
    label: StringResource,
    tone: PeripheralTone,
    modifier: Modifier = Modifier,
    busy: Boolean = false,
    big: Boolean = false,
) {
    val colors = tone.colors()
    CmpRow(
        modifier = modifier
            .heightIn(min = if (big) 36.dp else 32.dp)
            .background(colors.container, CircleShape)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (busy) {
            CmpCircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.5.dp, color = colors.content)
        } else {
            CmpBox(modifier = Modifier.size(10.dp).background(colors.dot, CircleShape))
        }
        CmpText(
            text = stringResource(label),
            color = colors.content,
            fontWeight = FontWeight.SemiBold,
            style = if (big) CmpTextStyle.BodyLarge else CmpTextStyle.Body,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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
    SignalClass.EXCELLENT, SignalClass.GOOD -> cmpThemeColors().onSurface
    SignalClass.WEAK -> cmpDomainColors().warning
    SignalClass.VERY_WEAK -> cmpThemeColors().error
}

private const val BARS = 4
private const val BAR_WIDTH_DIVISOR = 5
private const val BAR_BASE_SHARE = 0.25f
private const val BAR_STEP_SHARE = 0.1875f

/** Четыре полоски + «−67 dBm»; [showClass] — ещё подпись класса («Слабый»). */
@Composable
internal fun SignalBars(dbm: Int, modifier: Modifier = Modifier, size: Dp = 20.dp, showClass: Boolean = false) {
    val signal = signalClassOf(dbm)
    val color = signal.color()
    val weak = signal == SignalClass.WEAK || signal == SignalClass.VERY_WEAK
    val classLabel = stringResource(signal.label)
    val a11y = stringResource(Strings.string.peripherals_signal_a11y_level, classLabel)
    CmpFlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        CmpRow(
            modifier = Modifier.height(size).semantics { contentDescription = a11y },
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val filled = signal.ordinal + 1
            val empty = cmpThemeColors().outlineVariant
            for (bar in 1..BARS) {
                val barColor = if (bar <= filled) color else empty
                CmpBox(
                    modifier = Modifier
                        .width(size / BAR_WIDTH_DIVISOR)
                        .height(size * (BAR_BASE_SHARE + bar * BAR_STEP_SHARE))
                        .background(barColor, RoundedCornerShape(2.dp)),
                )
            }
        }
        CmpText(
            text = stringResource(Strings.string.peripherals_signal_format_dbm, kotlin.math.abs(dbm)),
            color = if (weak) color else cmpThemeColors().onSurface,
            fontWeight = FontWeight.SemiBold,
            style = CmpTextStyle.Body,
            maxLines = 1,
        )
        if (showClass) {
            CmpText(
                text = classLabel,
                color = if (weak) color else cmpThemeColors().onSurfaceVariant,
                style = CmpTextStyle.Body,
                maxLines = 1,
            )
        }
    }
}

/** Батарея: 4 деления + напряжение; низкий заряд — красным с подписью; [percent] — подпись «%» в «Подробнее». */
@Composable
internal fun BatteryIndicator(
    voltage: Float,
    percent: Int?,
    low: Boolean,
    modifier: Modifier = Modifier,
    showPercent: Boolean = false,
) {
    val theme = cmpThemeColors()
    val color = if (low) theme.error else theme.onSurface
    val level = if (low) 1 else percent?.let(::batteryLevel) ?: BARS
    val a11y = if (low) {
        stringResource(Strings.string.peripherals_battery_status_low)
    } else {
        stringResource(Strings.string.peripherals_battery_a11y_level, level)
    }
    CmpRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CmpRow(
            modifier = Modifier.semantics {
                contentDescription = a11y
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpRow(
                modifier = Modifier
                    .width(23.dp)
                    .height(13.dp)
                    .border(2.dp, color, RoundedCornerShape(4.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                for (cell in 1..BARS) {
                    CmpBox(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .height(9.dp)
                            .background(if (cell <= level) color else Color.Transparent, RoundedCornerShape(1.dp)),
                    )
                }
            }
            CmpBox(
                modifier = Modifier.width(
                    3.dp,
                ).height(6.dp).background(color, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)),
            )
        }
        CmpText(
            text = stringResource(Strings.string.peripherals_battery_format_voltage, voltage.formatDecimal(2)),
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = CmpTextStyle.Body,
            maxLines = 1,
        )
        when {
            low -> CmpText(
                text = stringResource(Strings.string.peripherals_battery_status_low),
                color = theme.error,
                fontWeight = FontWeight.Bold,
                style = CmpTextStyle.Body,
                maxLines = 1,
            )

            showPercent && percent != null -> CmpText(
                text = stringResource(Strings.string.peripherals_battery_format_percent, percent),
                color = theme.onSurfaceVariant,
                style = CmpTextStyle.Body,
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
    val color = cmpThemeColors().onSurfaceVariant
    CmpRow(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = title.uppercase(),
            color = color,
            fontWeight = FontWeight.Bold,
            style = CmpTextStyle.Body,
            modifier = Modifier.weight(1f),
        )
        if (trailing != null) CmpText(text = trailing, color = color, style = CmpTextStyle.Body)
    }
}

/** Скелетон строки: заголовок группы, иконка и две строки. */
@Composable
internal fun SkeletonRow(modifier: Modifier = Modifier) {
    CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CmpSkeleton(modifier = Modifier.width(120.dp).height(14.dp))
        CmpRow(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            CmpSkeleton(modifier = Modifier.size(52.dp), shape = RoundedCornerShape(16.dp))
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_TITLE).height(18.dp))
                CmpSkeleton(modifier = Modifier.fillMaxWidth(SKELETON_SUB).height(14.dp))
            }
        }
    }
}

private const val SKELETON_TITLE = 0.6f
private const val SKELETON_SUB = 0.4f
