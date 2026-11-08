package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn

private const val SKELETON_TRANSITION_LABEL = "support_chat_skeleton"
private const val SKELETON_ALPHA_LABEL = "support_chat_skeleton_alpha"

@Composable
internal fun SupportChatLoadingSkeleton(colors: CmpThemeColors, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = SKELETON_TRANSITION_LABEL)
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = SKELETON_ALPHA_LABEL,
    )
    val skeletonColor = colors.surfaceVariant.copy(alpha = alpha)

    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmpBox(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(96.dp)
                .height(28.dp)
                .background(skeletonColor, RoundedCornerShape(percent = 50)),
        )
        SkeletonBubble(widthFraction = 0.78f, height = 72.dp, isMine = true, color = skeletonColor)
        SkeletonBubble(widthFraction = 0.62f, height = 56.dp, isMine = true, color = skeletonColor)
        SkeletonBubble(widthFraction = 0.88f, height = 88.dp, isMine = false, color = skeletonColor)
        SkeletonBubble(widthFraction = 0.72f, height = 180.dp, isMine = true, color = skeletonColor)
        SkeletonBubble(widthFraction = 0.82f, height = 72.dp, isMine = false, color = skeletonColor)
    }
}

@Composable
private fun SkeletonBubble(widthFraction: Float, height: Dp, isMine: Boolean, color: Color) {
    CmpBox(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        CmpBox(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(height)
                .background(
                    color = color,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp,
                    ),
                ),
        )
    }
}
