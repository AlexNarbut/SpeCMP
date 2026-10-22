package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

/** Задержка до автоповтора при удержании и период повтора. */
private const val REPEAT_DELAY_MS = 400L
private const val REPEAT_PERIOD_MS = 80L
private const val PRESSED_OVERLAY_ALPHA = 0.12f
private const val DISABLED_ALPHA = 0.38f

/**
 * Крупная кнопка шага (−/+, ▲/▼) для числовых значений: срабатывает сразу при нажатии, при удержании —
 * повторяет [onStep] (через 400 мс, затем каждые 80 мс). Для TalkBack/VoiceOver — обычная кнопка.
 */
@Composable
fun CmpStepButton(
    icon: CmpIcons,
    contentDescription: String,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 64.dp
) {
    val currentOnStep by rememberUpdatedState(onStep)
    var pressed by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val container = when {
        !enabled -> colors.onSurface.copy(alpha = PRESSED_OVERLAY_ALPHA)

        pressed -> colors.onSecondaryContainer.copy(alpha = PRESSED_OVERLAY_ALPHA).compositeOver(
            colors.secondaryContainer
        )

        else -> colors.secondaryContainer
    }
    val content = if (enabled) colors.onSecondaryContainer else colors.onSurface.copy(alpha = DISABLED_ALPHA)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        modifier = modifier
            .size(size)
            .semantics {
                role = Role.Button
                this.contentDescription = contentDescription
                if (enabled) {
                    onClick {
                        currentOnStep()
                        true
                    }
                } else {
                    disabled()
                }
            }
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown().consume()
                        pressed = true
                        currentOnStep()
                        val repeat = launch {
                            delay(REPEAT_DELAY_MS)
                            while (isActive) {
                                currentOnStep()
                                delay(REPEAT_PERIOD_MS)
                            }
                        }
                        waitForUpOrCancellation()
                        repeat.cancel()
                        pressed = false
                    }
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(size / 2))
        }
    }
}
