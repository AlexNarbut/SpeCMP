package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_a11y_collapse
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_a11y_topics
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_btn_all_found
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_btn_expand
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_label_all_found
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_label_in_feed
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_label_next
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_label_opens
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_label_setting
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_title_found
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_title_home
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_title_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_product_format_short
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.ActionKind
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantAction
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StatusTone
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockHead
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockTile
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockUi
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.actionTitle
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.blockLabel
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.icon
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.nextStepText
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.settingValueText
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.statusText
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.topicTitle

internal const val DOCK_TAG = "mountassistant_dock"

@Composable
internal fun DockLevelHead(
    head: DockHead,
    onBack: () -> Unit,
    onCollapse: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    CmpRow(
        modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (head is DockHead.Topic) {
            CmpIconButton(
                onClick = onBack,
                icon = CmpIcons.ArrowBack,
                contentDescription = stringResource(Strings.string.mountassistant_dock_a11y_topics),
            )
        }
        CmpFlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            CmpText(
                text = when (head) {
                    DockHead.Home -> stringResource(Strings.string.mountassistant_dock_title_home)
                    is DockHead.Topic -> topicTitle(head.topic)
                    is DockHead.Found -> stringResource(Strings.string.mountassistant_dock_title_found, head.count)
                    DockHead.Nothing -> stringResource(Strings.string.mountassistant_dock_title_nothing)
                },
                style = CmpTextStyle.Paragraph1,
            )
            if (head is DockHead.Topic) {
                AssistantPill(text = statusText(head.status.kind), tone = head.status.tone.pillTone())
            }
        }
        if (onCollapse != null) {
            CmpIconButton(
                onClick = onCollapse,
                icon = CmpIcons.KeyboardArrowDown,
                contentDescription = stringResource(Strings.string.mountassistant_dock_a11y_collapse),
            )
        }
    }
}

internal fun StatusTone.pillTone(): PillTone = when (this) {
    StatusTone.OK -> PillTone.OK
    StatusTone.WARN, StatusTone.LOCK -> PillTone.WARN
    StatusTone.OFF, StatusTone.NEUTRAL -> PillTone.NEUTRAL
}

/** Уровни пульта сменяются сдвигом: вглубь — справа, назад — слева. [carouselHeight] `null` — сетка в 2 колонки. */
@Composable
internal fun DockTiles(dock: DockUi, firmwareVersion: String, onTile: (DockTile) -> Unit, carouselHeight: Dp? = null) {
    AnimatedContent(
        targetState = dock,
        contentKey = { it.head.levelKey() },
        transitionSpec = {
            val deeper = targetState.head is DockHead.Topic
            (slideInHorizontally(tween(SLIDE_MILLIS)) { if (deeper) it / SLIDE_PART else -it / SLIDE_PART } + fadeIn())
                .togetherWith(fadeOut(tween(SLIDE_MILLIS)))
        },
        label = "dock-level",
    ) { level ->
        if (carouselHeight == null) {
            TileGrid(level.tiles, firmwareVersion, onTile)
        } else {
            TileCarousel(level.tiles, firmwareVersion, carouselHeight, onTile)
        }
    }
}

private const val SLIDE_MILLIS = 260
private const val SLIDE_PART = 6

private fun DockHead.levelKey(): String = when (this) {
    DockHead.Home -> "home"
    is DockHead.Topic -> "topic:$topic"
    is DockHead.Found, DockHead.Nothing -> "search"
}

private const val CAROUSEL_SCREEN_PART = 0.13f
private val CarouselMinHeight = 112.dp
private val CarouselMaxHeight = 128.dp

/**
 * Высота плиток карусели — доля высоты окна, чтобы пульт не съедал ленту на невысоких экранах; нижняя граница —
 * под значок, две строки заголовка и подпись плотной плитки, с учётом масштаба шрифта.
 */
@Composable
internal fun carouselTileHeight(screenHeight: Dp): Dp {
    val scale = LocalDensity.current.fontScale
    return (screenHeight * CAROUSEL_SCREEN_PART).coerceIn(CarouselMinHeight * scale, CarouselMaxHeight * scale)
}

/** Ленивый список не выравнивает плитки по самой высокой (intrinsic ему недоступен) — высота одна, от экрана. */
@Composable
private fun TileCarousel(
    tiles: ImmutableList<DockTile>,
    firmwareVersion: String,
    tileHeight: Dp,
    onTile: (DockTile) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tiles, key = { it.key }) { tile ->
            ActionTile(
                content = tileContent(tile, firmwareVersion),
                onClick = { onTile(tile) },
                tag = tileTag(tile),
                modifier = Modifier.height(tileHeight),
                width = if (tile is DockTile.Hero || tile is DockTile.ProductNumber) 280.dp else 184.dp,
                pulseSeq = tile.pulseSeq(),
                dense = true,
            )
        }
    }
}

@Composable
private fun TileGrid(tiles: ImmutableList<DockTile>, firmwareVersion: String, onTile: (DockTile) -> Unit) {
    val hero = tiles.firstOrNull() as? DockTile.Hero
    CmpColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hero != null) {
            ActionTile(content = tileContent(hero, firmwareVersion), onClick = { onTile(hero) }, tag = tileTag(hero))
        }
        tiles.filter { it !is DockTile.Hero }.chunked(2).forEach { row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { tile ->
                    ActionTile(
                        content = tileContent(tile, firmwareVersion),
                        onClick = { onTile(tile) },
                        tag = tileTag(tile),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        pulseSeq = tile.pulseSeq(),
                    )
                }
                if (row.size == 1) CmpBox(modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun DockTile.pulseSeq(): Long? = when (this) {
    is DockTile.Topic -> pulseSeq
    is DockTile.Hero -> pulseSeq
    else -> null
}

internal fun tileTag(tile: DockTile): String = "mountassistant_tile_${tile.key.replace(':', '_').lowercase()}"

@Composable
internal fun tileContent(tile: DockTile, firmwareVersion: String): TileContent = when (tile) {
    is DockTile.Hero -> heroContent(tile, firmwareVersion)

    is DockTile.Topic -> topicContent(tile)

    is DockTile.Action -> actionContent(tile)

    is DockTile.Setting -> settingContent(tile)

    is DockTile.ProductNumber -> TileContent(
        variant = TileVariant.ACTION,
        title = stringResource(Strings.string.mountassistant_product_format_short, tile.number),
        subtitle = tile.blocked?.let { blockLabel(it) } ?: actionTitle(AssistantAction.PRODUCT_INFO),
        icon = AssistantAction.PRODUCT_INFO.icon,
        filled = tile.blocked == null,
        blocked = tile.blocked != null,
        corner = TileCorner.OPENS,
    )

    is DockTile.SearchAll -> TileContent(
        variant = TileVariant.ACTION,
        title = stringResource(Strings.string.mountassistant_dock_btn_all_found, tile.count),
        subtitle = stringResource(Strings.string.mountassistant_dock_label_all_found),
        icon = CmpIcons.List,
    )
}

@Composable
private fun heroContent(tile: DockTile.Hero, firmwareVersion: String): TileContent = TileContent(
    variant = TileVariant.HERO,
    title = actionTitle(tile.step.action, tile.version),
    subtitle = tile.blocked?.let { blockLabel(it) } ?: nextStepText(tile.step.reason, firmwareVersion),
    icon = tile.step.action.icon,
    filled = true,
    blocked = tile.blocked != null,
    kicker = stringResource(Strings.string.mountassistant_dock_label_next),
)

@Composable
private fun topicContent(tile: DockTile.Topic): TileContent = TileContent(
    variant = TileVariant.TOPIC,
    title = topicTitle(tile.topic),
    subtitle = statusText(tile.status.kind),
    icon = tile.topic.icon,
    blocked = tile.blocked,
    corner = when {
        tile.single == null -> TileCorner.MORE
        tile.single.kind == ActionKind.HANDOFF -> TileCorner.OPENS
        else -> TileCorner.NONE
    },
    tone = tile.status.tone,
)

@Composable
private fun actionContent(tile: DockTile.Action): TileContent {
    val hint = if (tile.inFeed) {
        Strings.string.mountassistant_dock_label_in_feed
    } else {
        Strings.string.mountassistant_dock_label_opens
    }
    return TileContent(
        variant = TileVariant.ACTION,
        title = actionTitle(tile.action, tile.version),
        subtitle = tile.blocked?.let { blockLabel(it) } ?: stringResource(hint),
        icon = tile.action.icon,
        filled = tile.primary,
        blocked = tile.blocked != null,
        corner = if (tile.inFeed) TileCorner.NONE else TileCorner.OPENS,
    )
}

@Composable
private fun settingContent(tile: DockTile.Setting): TileContent = TileContent(
    variant = TileVariant.SETTING,
    title = tile.setting.name,
    subtitle = tile.blocked?.let { blockLabel(it) }.orEmpty(),
    icon = CmpIcons.Tune,
    blocked = tile.blocked != null,
    kicker = stringResource(Strings.string.mountassistant_dock_label_setting),
    value = tile.setting.value?.takeIf { tile.blocked == null }?.let { settingValueText(tile.setting, it) },
)

/** Свёрнутый пульт: быстрые действия строкой и кнопка «Пульт». */
@Composable
internal fun CollapsedDock(quick: ImmutableList<DockTile.Action>, onTile: (DockTile) -> Unit, onExpand: () -> Unit) {
    val colors = cmpThemeColors()
    CmpRow(
        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(quick, key = { it.key }) { tile -> QuickChip(tile) { onTile(tile) } }
        }
        val shape = RoundedCornerShape(12.dp)
        CmpSurface(
            modifier = Modifier.height(48.dp).clip(shape).clickable(onClick = onExpand).testTag(DOCK_EXPAND_TAG),
            shape = shape,
            color = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
        ) {
            CmpRow(
                modifier = Modifier.padding(start = 10.dp, end = 14.dp).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CmpIcon(icon = CmpIcons.KeyboardArrowUp, contentDescription = null)
                CmpText(
                    text = stringResource(Strings.string.mountassistant_dock_btn_expand),
                    style = CmpTextStyle.Paragraph2,
                )
            }
        }
    }
}

internal const val DOCK_EXPAND_TAG = "mountassistant_dock_expand"

@Composable
private fun QuickChip(tile: DockTile.Action, onClick: () -> Unit) {
    val colors = cmpThemeColors()
    val blocked = tile.blocked
    val shape = RoundedCornerShape(12.dp)
    CmpSurface(
        modifier = Modifier.heightIn(min = 48.dp).clip(shape).clickable(onClick = onClick).testTag(tileTag(tile)),
        shape = shape,
        color = if (blocked != null) colors.surfaceContainer else assistantCardColor(),
        contentColor = if (blocked != null) colors.onSurfaceVariant else colors.onSurface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        CmpRow(
            modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = if (blocked != null) CmpIcons.Lock else tile.action.icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (blocked != null) colors.onSurfaceVariant else colors.primary,
            )
            CmpColumn {
                CmpText(
                    text = actionTitle(tile.action),
                    style = CmpTextStyle.Paragraph1,
                    maxLines = 1,
                )
                if (blocked != null) {
                    CmpText(
                        text = blockLabel(blocked),
                        style = CmpTextStyle.Paragraph2,
                        color = colors.tertiary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
