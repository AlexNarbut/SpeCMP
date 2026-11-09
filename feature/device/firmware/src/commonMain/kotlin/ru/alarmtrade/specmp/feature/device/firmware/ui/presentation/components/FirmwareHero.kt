@file:Suppress("MatchingDeclarationName")

package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface

internal enum class HeroTone { Primary, Neutral, Error }

private const val ICON_FRACTION = 0.46f
private const val PULSE_MILLIS = 1_800
private const val PULSE_FROM = 0.9f
private const val PULSE_TO = 1.35f
private const val PULSE_ALPHA = 0.5f

@Composable
internal fun FirmwareHero(
    icon: CmpIcons,
    modifier: Modifier = Modifier,
    tone: HeroTone = HeroTone.Primary,
    size: Dp = 160.dp,
    pulse: Boolean = false,
) {
    val colors = cmpThemeColors()
    val container = when (tone) {
        HeroTone.Primary -> colors.primaryContainer
        HeroTone.Neutral -> colors.surfaceVariant
        HeroTone.Error -> colors.errorContainer
    }
    val content = when (tone) {
        HeroTone.Primary -> colors.onPrimaryContainer
        HeroTone.Neutral -> colors.onSurfaceVariant
        HeroTone.Error -> colors.onErrorContainer
    }
    CmpBox(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        if (pulse) PulseRing(size = size)
        CmpSurface(modifier = Modifier.size(size), shape = CircleShape, color = container) {
            CmpBox(contentAlignment = Alignment.Center) {
                CmpIcon(
                    icon = icon,
                    contentDescription = null,
                    modifier = Modifier.size(size * ICON_FRACTION),
                    tint = content,
                )
            }
        }
    }
}

@Composable
private fun PulseRing(size: Dp) {
    val transition = rememberInfiniteTransition(label = "firmwareHeroPulse")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "firmwareHeroPulseProgress",
    )
    CmpSurface(
        modifier = Modifier
            .size(size)
            // Масштаб и альфа читаются в фазе draw: пульс не рекомпозирует кольцо каждый кадр.
            .graphicsLayer {
                val p = progress.value
                val scale = PULSE_FROM + (PULSE_TO - PULSE_FROM) * p
                scaleX = scale
                scaleY = scale
                alpha = PULSE_ALPHA * (1f - p)
            },
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(3.dp, cmpThemeColors().primary),
    ) {}
}
