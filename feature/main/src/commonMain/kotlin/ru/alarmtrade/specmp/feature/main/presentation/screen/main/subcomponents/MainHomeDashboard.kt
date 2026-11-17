package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.layout.CmpSpacer
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableCard
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeActionGroup

@Composable
internal fun MainHomeDashboard(
    mode: MainHomeMode,
    actions: List<MainHomeAction>,
    onHomeActionClick: (MainHomeAction) -> Unit,
    modifier: Modifier = Modifier,
    startContentPadding: Dp = 0.dp,
    endContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
    topContentPadding: Dp = 0.dp,
    header: @Composable () -> Unit = {},
) {
    val contentPadding = mode.contentPadding
    val primaryActions = actions.filter { it.group == MainHomeActionGroup.Primary }
    val toolActions = actions.filter { it.group == MainHomeActionGroup.Tool }
    val mainColumns = if (mode == MainHomeMode.Compact) 1 else 2
    val toolColumns = when (mode) {
        MainHomeMode.Compact -> 1
        MainHomeMode.Medium -> 2
        MainHomeMode.Expanded -> 3
        MainHomeMode.Large -> 4
    }
    val toolLayout = if (mode == MainHomeMode.Compact) ToolCardLayout.Row else ToolCardLayout.Grid

    Box(
        modifier = modifier.verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .mainHomeContentWidth(mode.contentMaxWidth)
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
            header()
            MainHomeGrid(
                columns = mainColumns,
                gap = if (mode == MainHomeMode.Compact) CmpSpacing.space12 else CmpSpacing.space16,
                itemHeight = { itemWidth -> mainHomePrimaryCardHeight(mode, itemWidth) },
                items = primaryActions,
            ) { item, itemModifier ->
                MainActionCard(
                    item = item,
                    onClick = { onHomeActionClick(item) },
                    modifier = itemModifier,
                )
            }

            MainHomeGrid(
                columns = toolColumns,
                gap = CmpSpacing.space12,
                itemHeight = { itemWidth -> mainHomeToolCardHeight(mode, itemWidth) },
                items = toolActions,
            ) { item, itemModifier ->
                ToolCard(
                    item = item,
                    layout = toolLayout,
                    onClick = { onHomeActionClick(item) },
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Composable
private fun MainActionCard(item: MainHomeAction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(CmpRadius.cardMedium),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CmpSpacing.space24),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CmpSpacing.space16, Alignment.CenterVertically),
        ) {
            MainHomeIconTile(
                painter = painterResource(item.icon),
                size = MainHomeTokens.PrimaryIconTile,
                isPrimary = true,
            )
            CardTexts(
                item = item,
                titleStyle = CmpTextStyle.Headline1,
                descriptionStyle = CmpTextStyle.Paragraph2,
                descriptionMaxLines = 3,
                textAlign = TextAlign.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
        }
    }
}

@Composable
private fun ToolCard(
    item: MainHomeAction,
    layout: ToolCardLayout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CmpClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(CmpRadius.cardSmall),
    ) {
        if (layout == ToolCardLayout.Row) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CmpSpacing.space16),
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space16),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MainHomeIconTile(
                    painter = painterResource(item.icon),
                    size = CmpSize.touchMin,
                    isPrimary = false,
                )
                CardTexts(
                    item = item,
                    titleStyle = CmpTextStyle.Headline2,
                    descriptionStyle = CmpTextStyle.Paragraph3,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    horizontalAlignment = Alignment.Start,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CmpSpacing.space16),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12, Alignment.CenterVertically),
            ) {
                MainHomeIconTile(
                    painter = painterResource(item.icon),
                    size = CmpSize.touchMin,
                    isPrimary = false,
                )
                CardTexts(
                    item = item,
                    titleStyle = CmpTextStyle.Headline2,
                    descriptionStyle = CmpTextStyle.Paragraph3,
                    textAlign = TextAlign.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
        }
    }
}

@Composable
private fun CardTexts(
    item: MainHomeAction,
    titleStyle: CmpTextStyle,
    descriptionStyle: CmpTextStyle,
    textAlign: TextAlign,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    descriptionMaxLines: Int = 2,
) {
    val colors = CmpTheme.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        CmpText(
            text = stringResource(item.title),
            style = titleStyle,
            color = colors.foregroundOnDarkPrimaryDefault,
            textAlign = textAlign,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        CmpText(
            text = stringResource(item.description),
            style = descriptionStyle,
            color = colors.foregroundOnDarkSecondaryDefault,
            textAlign = textAlign,
            maxLines = descriptionMaxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MainHomeIconTile(painter: Painter, size: Dp, isPrimary: Boolean, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val tint = if (isPrimary) colors.foregroundOnDarkPrimaryDefault else colors.foregroundOnDarkSecondaryDefault
    Box(
        modifier = modifier
            .size(size)
            .background(
                color = if (isPrimary) colors.brandPrimaryHighlight else colors.backgroundSurfaceHighlight,
                shape = RoundedCornerShape(CmpRadius.cardSmall),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CmpIcon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .padding(CmpSpacing.space8)
                .fillMaxSize(),
            tint = tint,
        )
    }
}

@Composable
private fun <T> MainHomeGrid(
    columns: Int,
    gap: Dp,
    itemHeight: (itemWidth: Dp) -> Dp,
    items: List<T>,
    itemContent: @Composable (T, Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemWidth = mainHomeGridItemWidth(
            containerWidth = maxWidth,
            columns = columns,
            gap = gap,
        )
        val resolvedItemHeight = itemHeight(itemWidth)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            items.chunked(columns).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(resolvedItemHeight),
                        ) {
                            itemContent(item, Modifier.fillMaxSize())
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

private fun mainHomeGridItemWidth(containerWidth: Dp, columns: Int, gap: Dp): Dp {
    val totalGap = gap * (columns - 1).toFloat()
    return (containerWidth - totalGap) / columns.toFloat()
}

private fun Modifier.mainHomeContentWidth(maxWidth: Dp): Modifier = if (maxWidth == Dp.Unspecified) {
    fillMaxWidth()
} else {
    fillMaxWidth().widthIn(max = maxWidth)
}

private enum class ToolCardLayout {
    Grid,
    Row,
}
