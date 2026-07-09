package ru.alarmtrade.specmp.feature.main.presentation.screen.main.subcomponents

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.layout.CmpSpacer
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.main.presentation.model.MainCardVariant
import ru.alarmtrade.specmp.feature.main.presentation.model.MainHomeAction
import specmp.feature.main.generated.resources.Res
import specmp.feature.main.generated.resources.adv_mount
import specmp.feature.main.generated.resources.can
import specmp.feature.main.generated.resources.chat
import specmp.feature.main.generated.resources.fast_mount
import specmp.feature.main.generated.resources.feedback
import specmp.feature.main.generated.resources.firmware_archive
import specmp.feature.main.generated.resources.main_main_msg_advanced_mount
import specmp.feature.main.generated.resources.main_main_msg_can_scanner
import specmp.feature.main.generated.resources.main_main_msg_cars
import specmp.feature.main.generated.resources.main_main_msg_configurator
import specmp.feature.main.generated.resources.main_main_msg_feedback
import specmp.feature.main.generated.resources.main_main_msg_firmware_archive
import specmp.feature.main.generated.resources.main_main_msg_product_info
import specmp.feature.main.generated.resources.main_main_msg_quick_mount
import specmp.feature.main.generated.resources.main_main_msg_requests
import specmp.feature.main.generated.resources.main_main_msg_tech_support_chat
import specmp.feature.main.generated.resources.main_main_title_advanced_mount
import specmp.feature.main.generated.resources.main_main_title_can_scanner
import specmp.feature.main.generated.resources.main_main_title_cars
import specmp.feature.main.generated.resources.main_main_title_configurator
import specmp.feature.main.generated.resources.main_main_title_feedback
import specmp.feature.main.generated.resources.main_main_title_firmware_archive
import specmp.feature.main.generated.resources.main_main_title_product_info
import specmp.feature.main.generated.resources.main_main_title_quick_mount
import specmp.feature.main.generated.resources.main_main_title_requests
import specmp.feature.main.generated.resources.main_main_title_tech_support_chat
import specmp.feature.main.generated.resources.product_search
import specmp.feature.main.generated.resources.settings_configurator
import specmp.feature.main.generated.resources.user_manual_request
import specmp.feature.main.generated.resources.vehicle_support

private data class MainHomeItem(
    val action: MainHomeAction,
    val icon: DrawableResource,
    val title: StringResource,
    val description: StringResource,
)

@Composable
internal fun MainHomeDashboard(
    mode: MainHomeMode,
    cardVariant: MainCardVariant,
    onHomeActionClick: (MainHomeAction) -> Unit,
    modifier: Modifier = Modifier,
    startContentPadding: Dp = 0.dp,
    endContentPadding: Dp = 0.dp,
    bottomContentPadding: Dp = 0.dp,
    colors: CmpThemeColors = cmpThemeColors(),
) {
    val contentPadding = mode.contentPadding
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
                    top = contentPadding,
                    end = contentPadding + endContentPadding,
                    bottom = contentPadding + bottomContentPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(if (mode == MainHomeMode.Compact) 16.dp else 24.dp),
        ) {
            MainHomeGrid(
                columns = mainColumns,
                gap = if (mode == MainHomeMode.Compact) 12.dp else 16.dp,
                itemHeight = { itemWidth -> mainHomePrimaryCardHeight(mode, itemWidth) },
                items = mainHomeActions(),
            ) { item, itemModifier ->
                MainActionCard(
                    item = item,
                    variant = cardVariant,
                    colors = colors,
                    onClick = { onHomeActionClick(item.action) },
                    modifier = itemModifier,
                )
            }

            MainHomeGrid(
                columns = toolColumns,
                gap = 12.dp,
                itemHeight = { itemWidth -> mainHomeToolCardHeight(mode, itemWidth) },
                items = mainHomeTools(),
            ) { item, itemModifier ->
                ToolCard(
                    item = item,
                    layout = toolLayout,
                    colors = colors,
                    onClick = { onHomeActionClick(item.action) },
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Composable
private fun MainActionCard(
    item: MainHomeItem,
    variant: MainCardVariant,
    colors: CmpThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCentered = variant == MainCardVariant.Centered

    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(MainHomeTokens.PrimaryCardRadius),
        color = colors.surfaceContainerLow,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        if (isCentered) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            ) {
                MainHomeIconTile(
                    painter = painterResource(item.icon),
                    size = 64.dp,
                    isPrimary = true,
                    colors = colors,
                )
                MainActionText(
                    item = item,
                    colors = colors,
                    textAlign = TextAlign.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MainHomeIconTile(
                    painter = painterResource(item.icon),
                    size = 64.dp,
                    isPrimary = true,
                    colors = colors,
                )
                MainActionText(
                    item = item,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    horizontalAlignment = Alignment.Start,
                )
            }
        }
    }
}

@Composable
private fun MainActionText(
    item: MainHomeItem,
    colors: CmpThemeColors,
    textAlign: TextAlign,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CmpText(
            text = stringResource(item.title),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            textAlign = textAlign,
        )
        CmpText(
            text = stringResource(item.description),
            fontSize = 14.sp,
            color = colors.onSurfaceVariant,
            textAlign = textAlign,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ToolCard(
    item: MainHomeItem,
    layout: ToolCardLayout,
    colors: CmpThemeColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRow = layout == ToolCardLayout.Row

    CmpClickableSurface(
        onClick = onClick,
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(MainHomeTokens.CardRadius),
        color = colors.surface,
        contentColor = colors.onSurface,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        if (isRow) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MainHomeIconTile(
                    painter = painterResource(item.icon),
                    size = 48.dp,
                    isPrimary = false,
                    colors = colors,
                )
                ToolText(
                    item = item,
                    colors = colors,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start,
                    horizontalAlignment = Alignment.Start,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            ) {
                MainHomeIconTile(
                    painter = painterResource(item.icon),
                    size = 48.dp,
                    isPrimary = false,
                    colors = colors,
                )
                ToolText(
                    item = item,
                    colors = colors,
                    textAlign = TextAlign.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
            }
        }
    }
}

@Composable
private fun ToolText(
    item: MainHomeItem,
    colors: CmpThemeColors,
    textAlign: TextAlign,
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CmpText(
            text = stringResource(item.title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface,
            textAlign = textAlign,
        )
        CmpText(
            text = stringResource(item.description),
            fontSize = 12.sp,
            color = colors.onSurfaceVariant,
            textAlign = textAlign,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MainHomeIconTile(
    painter: Painter,
    size: Dp,
    isPrimary: Boolean,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
    iconPadding: Dp = 6.dp,
) {
    CmpSurface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(MainHomeTokens.CardRadius),
        color = if (isPrimary) colors.primaryContainer else colors.surfaceVariant,
        contentColor = if (isPrimary) colors.onPrimaryContainer else colors.onSurfaceVariant,
    ) {
        Box(contentAlignment = Alignment.Center) {
            CmpIcon(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .padding(iconPadding)
                    .fillMaxSize(),
                tint = if (isPrimary) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
        }
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

private fun mainHomeGridItemWidth(
    containerWidth: Dp,
    columns: Int,
    gap: Dp,
): Dp {
    val totalGap = gap * (columns - 1).toFloat()
    return (containerWidth - totalGap) / columns.toFloat()
}

private fun Modifier.mainHomeContentWidth(maxWidth: Dp): Modifier {
    return if (maxWidth == Dp.Unspecified) {
        fillMaxWidth()
    } else {
        fillMaxWidth().widthIn(max = maxWidth)
    }
}

private enum class ToolCardLayout {
    Grid,
    Row,
}

private fun mainHomeActions(): List<MainHomeItem> {
    return listOf(
        MainHomeItem(
            action = MainHomeAction.QuickMount,
            icon = Res.drawable.fast_mount,
            title = Res.string.main_main_title_quick_mount,
            description = Res.string.main_main_msg_quick_mount,
        ),
        MainHomeItem(
            action = MainHomeAction.AdvancedMount,
            icon = Res.drawable.adv_mount,
            title = Res.string.main_main_title_advanced_mount,
            description = Res.string.main_main_msg_advanced_mount,
        ),
    )
}

private fun mainHomeTools(): List<MainHomeItem> {
    return listOf(
        MainHomeItem(
            action = MainHomeAction.Configurator,
            icon = Res.drawable.settings_configurator,
            title = Res.string.main_main_title_configurator,
            description = Res.string.main_main_msg_configurator,
        ),
        MainHomeItem(
            action = MainHomeAction.FirmwareArchive,
            icon = Res.drawable.firmware_archive,
            title = Res.string.main_main_title_firmware_archive,
            description = Res.string.main_main_msg_firmware_archive,
        ),
        MainHomeItem(
            action = MainHomeAction.Cars,
            icon = Res.drawable.vehicle_support,
            title = Res.string.main_main_title_cars,
            description = Res.string.main_main_msg_cars,
        ),
        MainHomeItem(
            action = MainHomeAction.CanScanner,
            icon = Res.drawable.can,
            title = Res.string.main_main_title_can_scanner,
            description = Res.string.main_main_msg_can_scanner,
        ),
        MainHomeItem(
            action = MainHomeAction.Requests,
            icon = Res.drawable.user_manual_request,
            title = Res.string.main_main_title_requests,
            description = Res.string.main_main_msg_requests,
        ),
        MainHomeItem(
            action = MainHomeAction.Feedback,
            icon = Res.drawable.feedback,
            title = Res.string.main_main_title_feedback,
            description = Res.string.main_main_msg_feedback,
        ),
        MainHomeItem(
            action = MainHomeAction.ProductInfo,
            icon = Res.drawable.product_search,
            title = Res.string.main_main_title_product_info,
            description = Res.string.main_main_msg_product_info,
        ),
        MainHomeItem(
            action = MainHomeAction.TechSupportChat,
            icon = Res.drawable.chat,
            title = Res.string.main_main_title_tech_support_chat,
            description = Res.string.main_main_msg_tech_support_chat,
        ),
    )
}
