package ru.alarmtrade.specmp.feature.appearance.presentation.screen.appearance.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonFill
import ru.alarmtrade.specmp.core.ui.theme.custom.ButtonShape
import ru.alarmtrade.specmp.core.ui.theme.custom.ControlPadding
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeModeSpec
import ru.alarmtrade.specmp.core.ui.theme.custom.ThemeRadii
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle

internal fun ControlPadding.buttonDims(): ControlDims = when (this) {
    ControlPadding.Small -> ControlDims(height = 48, side = 16)
    ControlPadding.Medium -> ControlDims(height = 56, side = 24)
    ControlPadding.Large -> ControlDims(height = 64, side = 32)
}

internal fun ControlPadding.chipDims(): ControlDims = when (this) {
    ControlPadding.Small -> ControlDims(height = 48, side = 12)
    ControlPadding.Medium -> ControlDims(height = 48, side = 18)
    ControlPadding.Large -> ControlDims(height = 56, side = 24)
}

/** Образец кнопки в цветах черновика, уменьшенный как в макете (70%). */
@Composable
internal fun SampleButton(
    text: String,
    mode: ThemeModeSpec,
    modifier: Modifier = Modifier,
    fill: ButtonFill = mode.buttonFill,
    shape: ButtonShape = mode.buttonShape,
    dims: ControlDims = mode.buttonPadding.buttonDims(),
) {
    val palette = mode.palette
    val outline = buttonShape(shape, mode.radii.buttons)
    val (background, content) = when (fill) {
        ButtonFill.Filled -> palette.primary.toColor() to palette.onPrimary.toColor()
        ButtonFill.Tonal -> palette.card.mix(palette.primary, TONAL_SHARE).toColor() to palette.ink.toColor()
        ButtonFill.Outlined -> null to palette.primary.toColor()
    }
    Box(
        modifier = modifier
            .heightIn(min = scaled(dims.height))
            .then(if (background != null) Modifier.background(background, outline) else Modifier)
            .cmpBorder(if (fill == ButtonFill.Outlined) OUTLINE else 0.dp, content, outline)
            .padding(horizontal = scaled(dims.side)),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(text = text, style = CmpTextStyle.Headline3, color = content, maxLines = 1)
    }
}

@Composable
internal fun SampleChip(text: String, mode: ThemeModeSpec, modifier: Modifier = Modifier) {
    val dims = mode.chipPadding.chipDims()
    val shape = if (mode.radii.chipsArePill) CircleShape else RoundedCornerShape(scaled(mode.radii.chips))
    Box(
        modifier = modifier
            .heightIn(min = scaled(dims.height))
            .cmpBorder(1.dp, mode.palette.outline.toColor(), shape)
            .padding(horizontal = scaled(dims.side)),
        contentAlignment = Alignment.Center,
    ) {
        CmpText(text = text, style = CmpTextStyle.Headline3, color = mode.palette.ink.toColor(), maxLines = 1)
    }
}

internal fun buttonShape(shape: ButtonShape, radius: Int): Shape = when (shape) {
    ButtonShape.Square -> RoundedCornerShape(SQUARE.dp)
    ButtonShape.Rounded -> RoundedCornerShape(radius.coerceIn(0, ThemeRadii.MAX).dp)
    ButtonShape.Pill -> CircleShape
}

private fun scaled(value: Int): Dp = (value * SAMPLE_SCALE).dp

private const val SAMPLE_SCALE = 0.7f
private const val TONAL_SHARE = 0.2f
private const val SQUARE = 4
private val OUTLINE = 2.dp
