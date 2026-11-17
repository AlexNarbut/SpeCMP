package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.layout.CmpSpacer
import ru.alarmtrade.specmp.core.ui.view.surface.CmpCard
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeActionGroup

private const val SHIMMER_BAND_WIDTH = 600f
private val TileShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.cardSmall)
private val LineShape: RoundedCornerShape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(CmpRadius.tags)

@Composable
internal fun MainHomeSkeleton(
    mode: MainHomeMode,
    modifier: Modifier = Modifier,
    startContentPadding: Dp = 0.dp,
    endContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
    topContentPadding: Dp = 0.dp,
) {
    val contentPadding = mode.contentPadding
    val visibleActions = MainHomeAction.visibleActions()
    val primaryActionsCount = visibleActions.count { it.group == MainHomeActionGroup.Primary }
    val toolActionsCount = visibleActions.count { it.group == MainHomeActionGroup.Tool }
    val mainColumns = if (mode == MainHomeMode.Compact) 1 else 2
    val toolColumns = when (mode) {
        MainHomeMode.Compact -> 1
        MainHomeMode.Medium -> 2
        MainHomeMode.Expanded -> 3
        MainHomeMode.Large -> 4
    }
    val isToolRow = mode == MainHomeMode.Compact
    val shimmerBrush = rememberMainHomeSkeletonBrush()

    Box(
        modifier = modifier.verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .mainHomeSkeletonContentWidth(mode.contentMaxWidth)
                .padding(
                    start = contentPadding + startContentPadding,
                    top = contentPadding + topContentPadding,
                    end = contentPadding + endContentPadding,
                    bottom = contentPadding + bottomContentPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(
                if (mode == MainHomeMode.Compact) CmpSpacing.space16 else CmpSpacing.space24,
            ),
        ) {
            MainHomeSkeletonGrid(
                columns = mainColumns,
                gap = if (mode == MainHomeMode.Compact) CmpSpacing.space12 else CmpSpacing.space16,
                itemHeight = { itemWidth -> mainHomePrimaryCardHeight(mode, itemWidth) },
                itemCount = primaryActionsCount,
            ) { itemModifier ->
                MainCardSkeleton(
                    shimmerBrush = shimmerBrush,
                    modifier = itemModifier,
                )
            }

            MainHomeSkeletonGrid(
                columns = toolColumns,
                gap = CmpSpacing.space12,
                itemHeight = { itemWidth -> mainHomeToolCardHeight(mode, itemWidth) },
                itemCount = toolActionsCount,
            ) { itemModifier ->
                ToolCardSkeleton(
                    isRow = isToolRow,
                    shimmerBrush = shimmerBrush,
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Composable
private fun MainCardSkeleton(shimmerBrush: Brush, modifier: Modifier = Modifier) {
    CmpCard(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(CmpRadius.cardMedium),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CmpSpacing.space24, vertical = CmpSpacing.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16, Alignment.CenterVertically),
        ) {
            SkeletonBlock(size = MainHomeTokens.PrimaryIconTile, shimmerBrush = shimmerBrush, shape = TileShape)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                SkeletonBlock(width = 150.dp, height = 18.dp, shimmerBrush = shimmerBrush)
                SkeletonBlock(width = 200.dp, height = 13.dp, shimmerBrush = shimmerBrush)
            }
        }
    }
}

@Composable
private fun ToolCardSkeleton(isRow: Boolean, shimmerBrush: Brush, modifier: Modifier = Modifier) {
    CmpCard(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
    ) {
        if (isRow) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space12),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonBlock(size = CmpSize.touchMin, shimmerBrush = shimmerBrush, shape = TileShape)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                ) {
                    SkeletonBlock(width = 140.dp, height = 15.dp, shimmerBrush = shimmerBrush)
                    SkeletonBlock(
                        modifier = Modifier.fillMaxWidth(fraction = 0.7f),
                        height = 12.dp,
                        shimmerBrush = shimmerBrush,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space20),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.CenterVertically),
            ) {
                SkeletonBlock(size = CmpSize.touchMin, shimmerBrush = shimmerBrush, shape = TileShape)
                SkeletonBlock(width = 96.dp, height = 15.dp, shimmerBrush = shimmerBrush)
                SkeletonBlock(width = 120.dp, height = 12.dp, shimmerBrush = shimmerBrush)
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    shimmerBrush: Brush,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp? = null,
    size: Dp? = null,
    shape: Shape = LineShape,
) {
    val shapedModifier = when {
        size != null -> modifier.size(size)
        width != null && height != null -> modifier.width(width).height(height)
        height != null -> modifier.height(height)
        else -> modifier
    }

    Box(
        modifier = shapedModifier.background(
            brush = shimmerBrush,
            shape = shape,
        ),
    )
}

@Composable
private fun rememberMainHomeSkeletonBrush(): Brush {
    val colors = CmpTheme.colors
    val transition = rememberInfiniteTransition(label = "MainHomeSkeletonShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1_400,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "MainHomeSkeletonShimmerProgress",
    )
    val startX = -SHIMMER_BAND_WIDTH + progress * 1_200f

    return Brush.linearGradient(
        colors = listOf(
            colors.backgroundSurfaceHighlight,
            colors.backgroundSurfaceMediumHighlight,
            colors.backgroundSurfaceHighlight,
        ),
        start = Offset(startX, 0f),
        end = Offset(startX + SHIMMER_BAND_WIDTH, 0f),
    )
}

@Composable
private fun MainHomeSkeletonGrid(
    columns: Int,
    gap: Dp,
    itemHeight: (itemWidth: Dp) -> Dp,
    itemCount: Int,
    itemContent: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemWidth = mainHomeSkeletonGridItemWidth(
            containerWidth = maxWidth,
            columns = columns,
            gap = gap,
        )
        val resolvedItemHeight = itemHeight(itemWidth)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            List(itemCount) { it }.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    rowItems.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(resolvedItemHeight),
                        ) {
                            itemContent(Modifier.fillMaxSize())
                        }
                    }
                    repeat(columns - rowItems.size) {
                        CmpSpacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun mainHomeSkeletonGridItemWidth(containerWidth: Dp, columns: Int, gap: Dp): Dp {
    val totalGap = gap * (columns - 1).toFloat()
    return (containerWidth - totalGap) / columns.toFloat()
}

private fun Modifier.mainHomeSkeletonContentWidth(maxWidth: Dp): Modifier = if (maxWidth == Dp.Unspecified) {
    fillMaxWidth()
} else {
    fillMaxWidth().widthIn(max = maxWidth)
}
