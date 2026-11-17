package ru.alarmtrade.specmp.core.ui.view.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons

private const val REPEAT_DELAY_MS = 400L
private const val REPEAT_PERIOD_MS = 80L

/** Срабатывает сразу при нажатии, при удержании повторяет [onStep]. */
@Composable
fun CmpStepButton(
    icon: CmpIcons,
    contentDescription: String,
    onStep: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = CmpSize.buttonXl + CmpSpacing.space8,
) {
    val currentOnStep by rememberUpdatedState(onStep)
    var pressed by remember { mutableStateOf(false) }
    val colors = CmpTheme.colors
    val container = when {
        !enabled -> colors.backgroundSurfaceUltraHighlight
        pressed -> colors.backgroundSurfaceMediumHighlight
        else -> colors.backgroundSurfaceHighlight
    }
    val content = if (enabled) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkPrimaryDisabled

    Surface(
        shape = RoundedCornerShape(CmpRadius.cardMedium),
        border = BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight),
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
            },
    ) {
        Box(contentAlignment = Alignment.Center) {
            CmpIcon(icon = icon, contentDescription = null, modifier = Modifier.size(size / 2))
        }
    }
}
