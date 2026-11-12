package ru.alarmtrade.specmp.core.ui.view.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Постоянная полоса прокрутки списка: видно, сколько ещё листать. Платформенной общей нет (iOS не рисует
 * индикатор у Compose-списков). Позиция читается в фазе отрисовки — прокрутка не вызывает рекомпозицию.
 * Высота элементов оценивается по видимым — для однородных списков точно, для смешанных приблизительно.
 */
fun Modifier.cmpVerticalScrollbar(state: LazyListState, color: Color): Modifier = drawWithContent {
    drawContent()
    val info = state.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty() || (!state.canScrollForward && !state.canScrollBackward)) return@drawWithContent

    val itemSize = visible.sumOf { it.size }.toFloat() / visible.size
    val viewport = size.height
    val content = itemSize * info.totalItemsCount
    val scrollRange = (content - viewport).coerceAtLeast(1f)
    val scrolled = state.firstVisibleItemIndex * itemSize + state.firstVisibleItemScrollOffset
    val thumbHeight = (viewport * viewport / content).coerceIn(ThumbMinHeight.toPx(), viewport)
    val thumbTop = (scrolled / scrollRange).coerceIn(0f, 1f) * (viewport - thumbHeight)
    val width = ThumbWidth.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - width - ThumbEndPadding.toPx(), thumbTop),
        size = Size(width, thumbHeight),
        cornerRadius = CornerRadius(width / 2),
    )
}

private val ThumbWidth = 4.dp
private val ThumbMinHeight = 32.dp
private val ThumbEndPadding = 2.dp
