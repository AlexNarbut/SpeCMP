package ru.alarmtrade.specmp.core.ui.view.progress

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private const val SKELETON_PERIOD_MS = 700
private const val SKELETON_MIN_ALPHA = 0.45f

@Composable
fun CmpSkeleton(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha = transition.animateFloat(
        initialValue = 1f,
        targetValue = SKELETON_MIN_ALPHA,
        animationSpec = infiniteRepeatable(tween(SKELETON_PERIOD_MS, easing = LinearEasing), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    // Альфа читается в фазе draw: пульс не рекомпозирует скелетон каждый кадр.
    Box(
        modifier
            .graphicsLayer { this.alpha = alpha.value }
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, shape),
    )
}
