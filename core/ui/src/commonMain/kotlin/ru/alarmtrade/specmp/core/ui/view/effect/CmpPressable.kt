package ru.alarmtrade.specmp.core.ui.view.effect

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpMotion

/**
 * Нажатие дизайн-системы: сжатие до `CmpMotion.PRESS_SCALE` пружиной, ripple цвета чернил, фокус-кольцо
 * `borderFocus` толщиной `CmpBorderWidth.focus` при фокусе с клавиатуры. Масштаб читается в graphicsLayer —
 * нажатие не вызывает рекомпозицию.
 */
@Composable
fun Modifier.cmpPressable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    role: Role? = Role.Button,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val focused by source.collectIsFocusedAsState()
    val scale = animateFloatAsState(
        targetValue = if (pressed) CmpMotion.PRESS_SCALE else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "press",
    )
    val focusRing = if (focused) {
        Modifier.border(CmpBorderWidth.focus, CmpTheme.colors.borderFocus, shape)
    } else {
        Modifier
    }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
        .then(focusRing)
        .clip(shape)
        .clickable(
            interactionSource = source,
            indication = ripple(color = CmpTheme.colors.foregroundOnDarkPrimaryDefault),
            enabled = enabled,
            role = role,
            onClick = onClick,
        )
}

/** Рамка дизайн-системы: при толщине 0 (`--w-*-width: 0`) рамка не рисуется. */
fun Modifier.cmpBorder(width: Dp, color: Color, shape: Shape): Modifier =
    if (width.value <= 0f || color == Color.Transparent) this else border(width, color, shape)

/** Градиентная рамка (стекло, чипы): сверху светлее, снизу прозрачнее. */
fun Modifier.cmpBorder(width: Dp, brush: Brush, shape: Shape): Modifier =
    if (width.value <= 0f) this else border(width, brush, shape)
