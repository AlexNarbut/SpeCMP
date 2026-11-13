package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_title_found
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_dock_title_nothing
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_a11y_close
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_label_actions
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_label_product
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_label_settings
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_label_topics
import ru.alarmtrade.specmp.core.localization.resources.mountassistant_search_label_try
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.domain.engine.stemRanges
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StatusTone
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockHead
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockTile
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockUi
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.ResultGroup
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.resultGroups

internal const val SEARCH_RESULTS_TAG = "mountassistant_search_results"

private val PanelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val RowShape = RoundedCornerShape(16.dp)
private val RowMinWidth = 300.dp
private val RowGap = 6.dp
private val PanelPadding = 16.dp
private const val FILLED_ICON_ALPHA = 0.18f
private val NoPulse: () -> Float = { 0f }

/** Найденное в широком окне: строки по группам поверх ленты, над строкой поиска. */
@Composable
internal fun SearchResultsPanel(
    results: DockUi,
    query: String,
    firmwareVersion: String,
    onTile: (DockTile) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    CmpSurface(
        modifier = modifier.testTag(SEARCH_RESULTS_TAG),
        shape = PanelShape,
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
        shadowElevation = 3.dp,
    ) {
        BoxWithConstraints {
            val columns = if (maxWidth - PanelPadding * 2 >= RowMinWidth * 2 + RowGap) 2 else 1
            CmpColumn(
                modifier = Modifier.verticalScroll(rememberScrollState())
                    .padding(start = PanelPadding, end = PanelPadding, top = 8.dp, bottom = PanelPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PanelHead(results.head, onClose)
                resultGroups(results).forEach { (group, tiles) ->
                    ResultGroupRows(group, tiles, columns, query, firmwareVersion, onTile)
                }
            }
        }
    }
}

@Composable
private fun PanelHead(head: DockHead, onClose: () -> Unit) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = if (head is DockHead.Found) {
                stringResource(Strings.string.mountassistant_dock_title_found, head.count)
            } else {
                stringResource(Strings.string.mountassistant_dock_title_nothing)
            },
            modifier = Modifier.weight(1f),
            style = CmpTextStyle.BodyLarge,
            fontWeight = FontWeight.Medium,
        )
        CmpIconButton(
            onClick = onClose,
            icon = CmpIcons.Close,
            contentDescription = stringResource(Strings.string.mountassistant_search_a11y_close),
        )
    }
}

@Composable
private fun ResultGroupRows(
    group: ResultGroup,
    tiles: ImmutableList<DockTile>,
    columns: Int,
    query: String,
    firmwareVersion: String,
    onTile: (DockTile) -> Unit,
) {
    val colors = cmpThemeColors()
    CmpColumn(verticalArrangement = Arrangement.spacedBy(RowGap)) {
        CmpText(
            text = stringResource(
                when (group) {
                    ResultGroup.PRODUCT -> Strings.string.mountassistant_search_label_product
                    ResultGroup.TOPICS -> Strings.string.mountassistant_search_label_topics
                    ResultGroup.ACTIONS -> Strings.string.mountassistant_search_label_actions
                    ResultGroup.SETTINGS -> Strings.string.mountassistant_search_label_settings
                    ResultGroup.TRY -> Strings.string.mountassistant_search_label_try
                },
            ),
            style = CmpTextStyle.Body,
            fontWeight = FontWeight.Medium,
            color = colors.onSurfaceVariant,
        )
        tiles.chunked(columns).forEach { row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(RowGap),
            ) {
                row.forEach { tile ->
                    ResultRow(tile, query, firmwareVersion, { onTile(tile) }, Modifier.weight(1f).fillMaxHeight())
                }
                repeat(columns - row.size) { CmpBox(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

internal fun resultTag(tile: DockTile): String = tileTag(tile).replace("_tile_", "_result_")

@Composable
private fun ResultRow(
    tile: DockTile,
    query: String,
    firmwareVersion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = cmpThemeColors()
    val content = tileContent(tile, firmwareVersion)
    val filled = content.filled && !content.blocked
    val setting = (tile as? DockTile.Setting)?.setting
    val ranges = when {
        tile is DockTile.ProductNumber -> emptyList()
        else -> setting?.nameHighlights?.takeIf { it.isNotEmpty() } ?: stemRanges(content.title, query)
    }
    val subtitle = if (setting != null && !content.blocked) setting.path.joinToString(" › ") else content.subtitle
    val dashed = colors.outline.takeIf { content.blocked }
    CmpSurface(
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp)
            .tileFrame(pulse = NoPulse, ring = colors.primary, dashed = dashed, corner = 16.dp)
            .testTag(resultTag(tile))
            .clip(RowShape)
            .clickable(onClick = onClick),
        shape = RowShape,
        color = when {
            filled -> colors.primary
            content.blocked -> colors.surfaceContainer
            else -> assistantCardColor()
        },
        contentColor = if (filled) colors.onPrimary else colors.onSurface,
        border = if (filled || content.blocked) null else BorderStroke(1.dp, colors.outlineVariant),
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowIcon(content, filled, isSetting = setting != null)
            CmpColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                CmpText(
                    text = highlighted(content.title, ranges),
                    style = CmpTextStyle.BodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (subtitle.isNotBlank()) RowSubtitle(content, subtitle, filled)
            }
            RowTrailing(content, filled)
        }
    }
}

@Composable
private fun RowIcon(content: TileContent, filled: Boolean, isSetting: Boolean) {
    val colors = cmpThemeColors()
    val (background, tint) = when {
        filled -> colors.onPrimary.copy(alpha = FILLED_ICON_ALPHA) to colors.onPrimary
        content.blocked -> colors.surfaceContainerHighest to colors.onSurfaceVariant
        isSetting -> colors.surfaceContainerHigh to colors.primary
        else -> colors.primaryContainer to colors.onPrimaryContainer
    }
    IconBox(icon = if (isSetting) CmpIcons.Tune else content.icon, background = background, tint = tint)
}

@Composable
private fun RowSubtitle(content: TileContent, subtitle: String, filled: Boolean) {
    val colors = cmpThemeColors()
    val tone = content.tone
    val warn = tone == StatusTone.WARN || tone == StatusTone.LOCK
    CmpRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (tone != null && content.variant == TileVariant.TOPIC && !content.blocked) {
            CmpBox(modifier = Modifier.size(8.dp).background(toneColor(tone, colors.outline), CircleShape)) {}
        }
        CmpText(
            text = subtitle,
            style = CmpTextStyle.Body,
            color = when {
                filled -> colors.onPrimary
                content.blocked || warn -> colors.tertiary
                else -> colors.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun RowTrailing(content: TileContent, filled: Boolean) {
    val colors = cmpThemeColors()
    if (content.value != null) {
        AssistantPill(text = content.value)
        return
    }
    val corner = when {
        content.blocked -> CmpIcons.Lock
        content.corner == TileCorner.MORE -> CmpIcons.ChevronRight
        content.corner == TileCorner.OPENS -> CmpIcons.OpenInNew
        else -> return
    }
    CmpIcon(
        icon = corner,
        contentDescription = null,
        modifier = Modifier.size(22.dp),
        tint = if (filled) colors.onPrimary else colors.onSurfaceVariant,
    )
}
