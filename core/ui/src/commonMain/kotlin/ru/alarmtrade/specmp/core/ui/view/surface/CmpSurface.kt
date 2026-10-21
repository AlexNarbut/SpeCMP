package ru.alarmtrade.specmp.core.ui.view.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CmpSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (color != Color.Unspecified) {
            color
        } else {
            androidx.compose.material3.MaterialTheme.colorScheme.surface
        },
        contentColor = if (contentColor != Color.Unspecified) {
            contentColor
        } else {
            androidx.compose.material3.MaterialTheme.colorScheme.onSurface
        },
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content
    )
}

@Composable
fun CmpClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    color: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    // false отключает невидимую M3-инфляцию layout-бокса до 48dp touch target.
    // Использовать только для плотных составных контролов (segmented control и т.п.),
    // где размер таргета задаёт сам контрол.
    enforceMinimumTouchTarget: Boolean = true,
    content: @Composable () -> Unit
) {
    val surface: @Composable () -> Unit = {
        Surface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            color = if (color != Color.Unspecified) {
                color
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.surface
            },
            contentColor = if (contentColor != Color.Unspecified) {
                contentColor
            } else {
                androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            },
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation,
            border = border,
            content = content
        )
    }

    if (enforceMinimumTouchTarget) {
        surface()
    } else {
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
            content = surface
        )
    }
}
