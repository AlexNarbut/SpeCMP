package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.em
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpMotion
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.view.effect.cmpBorder
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.core.ui.view.text.toTextStyle

private const val DEFAULT_HOLD_MS = 1500
private const val LABEL_TRACKING = 0.03f
private const val INVERT_LABEL_AT = 0.5f

/**
 * Удержание для опасных действий (сброс, стирание, прошивка): заливка слева направо за [durationMs]; отпустили раньше —
 * отмена, заливка уходит. На 100% — [onConfirm] и [doneText]. Для доступности — обычный клик в семантике.
 */
@Composable
fun CmpHoldToConfirmButton(
    text: String,
    doneText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = true,
    durationMs: Int = DEFAULT_HOLD_MS,
) {
    val colors = CmpTheme.colors
    val fill = if (danger) colors.errorDefault else colors.brandPrimaryDefault
    val shape = RoundedCornerShape(CmpRadius.buttons)
    val progress = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }
    val confirm by rememberUpdatedState(onConfirm)
    val scope = rememberCoroutineScope()
    val finish = {
        done = true
        confirm()
    }
    val labelColor = holdLabelColor(enabled, done || progress.value > INVERT_LABEL_AT, fill)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CmpSize.hold)
            .graphicsLayer {
                val scale = if (progress.value > 0f && !done) CmpMotion.PRESS_SCALE else 1f
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(colors.backgroundSurfaceUltraHighlight, shape)
            .cmpBorder(CmpBorderWidth.inputs, if (enabled) fill else colors.borderUltraHighlight, shape)
            .drawBehind {
                val fraction = if (done) 1f else progress.value
                if (fraction > 0f) drawRect(fill, size = Size(size.width * fraction, size.height))
            }
            .semantics {
                role = Role.Button
                if (enabled && !done) {
                    onClick {
                        finish()
                        true
                    }
                }
            }
            .holdGesture(active = enabled && !done) {
                val hold = scope.launch {
                    progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
                    finish()
                }
                val release: () -> Unit = {
                    if (!done) {
                        hold.cancel()
                        scope.launch { progress.animateTo(0f, tween(CmpMotion.STANDARD_MS)) }
                    }
                }
                release
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = (if (done) doneText else text).uppercase(),
            color = labelColor,
            style = CmpTextStyle.Headline2.toTextStyle().copy(letterSpacing = LABEL_TRACKING.em),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun holdLabelColor(enabled: Boolean, inverted: Boolean, fill: Color): Color = when {
    !enabled -> CmpTheme.colors.foregroundOnDarkPrimaryDisabled
    inverted -> CmpTheme.colors.staticWhite
    else -> fill
}

/** Удержание: [onPress] на касании возвращает действие на отпускание/отмену. */
private fun Modifier.holdGesture(active: Boolean, onPress: () -> () -> Unit): Modifier = pointerInput(active) {
    if (!active) return@pointerInput
    awaitEachGesture {
        awaitFirstDown()
        val release = onPress()
        waitForUpOrCancellation()
        release()
    }
}
