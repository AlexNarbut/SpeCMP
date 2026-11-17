package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.progress.CmpSkeleton

@Composable
internal fun SupportChatLoadingSkeleton(modifier: Modifier = Modifier) {
    CmpColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = CmpSpacing.space8),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
    ) {
        CmpSkeleton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(96.dp)
                .height(28.dp),
            shape = RoundedCornerShape(CmpRadius.round),
        )
        SkeletonBubble(widthFraction = 0.78f, height = 72.dp, isMine = true)
        SkeletonBubble(widthFraction = 0.62f, height = 56.dp, isMine = true)
        SkeletonBubble(widthFraction = 0.88f, height = 88.dp, isMine = false)
        SkeletonBubble(widthFraction = 0.72f, height = 180.dp, isMine = true)
        SkeletonBubble(widthFraction = 0.82f, height = 72.dp, isMine = false)
    }
}

@Composable
private fun SkeletonBubble(widthFraction: Float, height: Dp, isMine: Boolean) {
    CmpBox(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        CmpSkeleton(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(height),
            shape = supportChatBubbleShape(isMine = isMine),
        )
    }
}
