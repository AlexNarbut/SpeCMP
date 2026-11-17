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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.badge.CmpDot
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.device.settings.domain.model.InputContactType
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Polarity
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Wire

private const val OPAQUE = 0xFF000000.toInt()
private const val TWO_COLOR_STRIPE_FROM = 1f / 3
private const val HATCH_STEP = 6f
private val SwatchSize = Size(34f, 18f)

@Composable
internal fun WireTag(wire: Wire, modifier: Modifier = Modifier) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.heightIn(min = 28.dp).padding(start = 4.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            WireSwatch(wire.colors)
            CmpText(
                text = if (wire.name.isBlank()) {
                    stringResource(Strings.string.settings_common_value_wire_pin, wire.pin)
                } else {
                    stringResource(Strings.string.settings_common_value_wire, wire.name, wire.pin)
                },
                style = CmpTextStyle.Additional0,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun WireSwatch(rgb: List<Int>) {
    val outline = cmpThemeColors().outline
    val fills = rgb.map { Color(it or OPAQUE) }
    Canvas(
        modifier = Modifier
            .size(width = SwatchSize.width.dp, height = SwatchSize.height.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, outline, RoundedCornerShape(6.dp)),
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
    val colors = cmpThemeColors()
    val plus = polarity == Polarity.PLUS
    val sign = if (plus) colors.onPrimary else colors.onSurface
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
            .size(24.dp)
            .background(if (plus) colors.primary else Color.Transparent, CircleShape)
            .border(2.dp, if (plus) colors.primary else colors.onSurface, CircleShape)
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
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier
            .heightIn(min = 26.dp)
            .background(colors.tertiaryContainer, CircleShape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CmpDot(size = 8.dp)
        CmpText(
            text = stringResource(Strings.string.settings_common_value_changed),
            style = CmpTextStyle.Additional0,
            color = colors.onTertiaryContainer,
        )
    }
}

@Composable
internal fun ContactBadge(type: InputContactType) {
    val text = when (type) {
        InputContactType.NORMALLY_OPEN -> stringResource(Strings.string.settings_common_value_contact_no)
        InputContactType.NORMALLY_CLOSED -> stringResource(Strings.string.settings_common_value_contact_nc)
        InputContactType.UNDEFINED -> return
    }
    val colors = cmpThemeColors()
    CmpText(
        text = text,
        style = CmpTextStyle.Additional0,
        color = colors.onSurface,
        modifier = Modifier
            .background(colors.surfaceVariant, CircleShape)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}
