package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_changed
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_nc
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_contact_no
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_minus_sign
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_polarity_plus_sign
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_wire
import ru.alarmtrade.specmp.core.localization.resources.settings_common_value_wire_pin
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTagTone
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Wire

private const val OPAQUE = 0xFF000000.toInt()
private const val TWO_COLOR_STRIPE_FROM = 1f / 3
private const val HATCH_STEP = 6f
private val SwatchSize = Size(34f, 18f)
private val WireTagPaddingStart = 6.dp
private val PolaritySize = 28.dp
private val PolarityRing = 2.dp

@Composable
internal fun WireTag(wire: Wire, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    CmpSurface(
        modifier = modifier,
        shape = RoundedCornerShape(CmpRadius.cardMedium),
        color = colors.backgroundSurfaceUltraHighlight,
        border = BorderStroke(CmpBorderWidth.tags, colors.borderHighlight),
    ) {
        CmpRow(
            modifier = Modifier
                .heightIn(min = CmpSize.tag)
                .padding(start = WireTagPaddingStart, end = CmpSpacing.space12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        ) {
            WireSwatch(wire.colors)
            CmpText(
                text = if (wire.name.isBlank()) {
                    stringResource(Strings.string.settings_common_value_wire_pin, wire.pin)
                } else {
                    stringResource(Strings.string.settings_common_value_wire, wire.name, wire.pin)
                },
                style = CmpTextStyle.Additional0,
                color = colors.foregroundOnDarkPrimaryDefault,
                modifier = Modifier.padding(vertical = CmpSpacing.space4),
            )
        }
    }
}

@Composable
private fun WireSwatch(rgb: List<Int>) {
    val outline = CmpTheme.colors.borderLight
    val fills = rgb.map { Color(it or OPAQUE) }
    val shape = RoundedCornerShape(CmpRadius.round)
    Canvas(
        modifier = Modifier
            .size(width = SwatchSize.width.dp, height = SwatchSize.height.dp)
            .clip(shape)
            .border(CmpBorderWidth.inputs, outline, shape),
    ) {
        when (fills.size) {
            0 -> {
                var x = -size.height
                while (x < size.width) {
                    drawLine(outline, Offset(x, size.height), Offset(x + size.height, 0f), strokeWidth = 2f)
                    x += HATCH_STEP * density
                }
            }

            1 -> drawRect(fills[0])

            2 -> {
                drawRect(fills[0])
                drawRect(
                    fills[1],
                    topLeft = Offset(0f, size.height * TWO_COLOR_STRIPE_FROM),
                    size = Size(size.width, size.height * TWO_COLOR_STRIPE_FROM),
                )
            }

            else -> {
                val band = size.height / fills.size
                fills.forEachIndexed { index, color ->
                    drawRect(color, topLeft = Offset(0f, band * index), size = Size(size.width, band))
                }
            }
        }
    }
}

@Composable
internal fun PolarityBadge(polarity: Polarity) {
    if (polarity == Polarity.UNDEFINED) return
    val colors = CmpTheme.colors
    val plus = polarity == Polarity.PLUS
    val ink = colors.foregroundOnDarkPrimaryDefault
    val sign = if (plus) colors.onBrand else ink
    val label = stringResource(
        if (plus) {
            Strings.string.settings_common_value_polarity_plus_sign
        } else {
            Strings.string.settings_common_value_polarity_minus_sign
        },
    )
    // Знак рисуется линиями, а не глифом шрифта: текст в маленьком круге смещается из-за отступов шрифта.
    Canvas(
        modifier = Modifier
            .size(PolaritySize)
            .background(if (plus) colors.brandPrimaryDefault else Color.Transparent, CircleShape)
            .border(PolarityRing, if (plus) colors.brandPrimaryDefault else ink, CircleShape)
            .semantics { contentDescription = label },
    ) {
        val half = size.minDimension * SIGN_HALF_LENGTH
        val stroke = SIGN_STROKE.dp.toPx()
        drawLine(sign, Offset(center.x - half, center.y), Offset(center.x + half, center.y), stroke, StrokeCap.Round)
        if (plus) {
            drawLine(
                sign,
                Offset(center.x, center.y - half),
                Offset(center.x, center.y + half),
                stroke,
                StrokeCap.Round,
            )
        }
    }
}

private const val SIGN_HALF_LENGTH = 0.25f
private const val SIGN_STROKE = 2f

@Composable
internal fun ChangedChip() {
    CmpTag(text = stringResource(Strings.string.settings_common_value_changed), tone = CmpTagTone.Brand)
}

@Composable
internal fun ContactBadge(type: InputContactType) {
    val text = when (type) {
        InputContactType.NORMALLY_OPEN -> stringResource(Strings.string.settings_common_value_contact_no)
        InputContactType.NORMALLY_CLOSED -> stringResource(Strings.string.settings_common_value_contact_nc)
        InputContactType.UNDEFINED -> return
    }
    CmpTag(text = text, tone = CmpTagTone.Neutral)
}
