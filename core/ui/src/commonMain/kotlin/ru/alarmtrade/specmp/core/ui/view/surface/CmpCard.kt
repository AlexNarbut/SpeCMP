package ru.alarmtrade.specmp.core.ui.view.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.view.effect.cmpPressable

private const val DISABLED_ALPHA = 0.4f

/** Карточка: `backgroundSurfaceCard` + рамка 1dp `borderHighlight`, радиус `CmpRadius.cardMedium`, без тени. */
@Composable
fun CmpCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(CmpRadius.cardMedium),
    elevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CardFrame(modifier = modifier, shape = shape, elevation = elevation, border = border, content = content)
}

@Composable
fun CmpClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(CmpRadius.cardMedium),
    elevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    CardFrame(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .cmpPressable(onClick = onClick, enabled = enabled, shape = shape),
        shape = shape,
        elevation = elevation,
        border = border,
        content = content,
    )
}

@Composable
private fun CardFrame(
    modifier: Modifier,
    shape: Shape,
    elevation: Dp,
    border: BorderStroke?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CmpTheme.colors
    val stroke = border ?: BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight)
    CompositionLocalProvider(LocalContentColor provides colors.foregroundOnDarkPrimaryDefault) {
        Column(
            modifier = modifier
                .then(if (elevation > 0.dp) Modifier.shadow(elevation, shape) else Modifier)
                .clip(shape)
                .background(colors.backgroundSurfaceCard, shape)
                .border(stroke, shape),
            content = content,
        )
    }
}
