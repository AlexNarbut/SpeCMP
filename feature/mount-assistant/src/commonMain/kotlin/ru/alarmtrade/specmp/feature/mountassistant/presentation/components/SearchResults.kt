package ru.alarmtrade.specmp.feature.mountassistant.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.surface.CmpTag
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockHead
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockTile
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.DockUi
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.ResultGroup
import ru.alarmtrade.specmp.feature.mountassistant.presentation.model.resultGroups

internal const val SEARCH_RESULTS_TAG = "mountassistant_search_results"

private val PanelShape = RoundedCornerShape(topStart = CmpRadius.sheet, topEnd = CmpRadius.sheet)
private val RowMinWidth = 300.dp
private val RowGap = CmpSpacing.space8
private val PanelPadding = CmpSpacing.space16
private const val RESULT_TITLE_LINES = 3

/** Найденное в широком окне: строки по группам поверх ленты, над строкой поиска. */
@Composable
internal fun SearchResultsPanel(
    results: DockUi,
    firmwareVersion: String,
    onTile: (DockTile) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CmpTheme.colors
    CmpSurface(
        modifier = modifier.testTag(SEARCH_RESULTS_TAG),
        shape = PanelShape,
        color = colors.backgroundPagePrimary,
        border = BorderStroke(CmpBorderWidth.listItem, colors.borderHighlight),
    ) {
        BoxWithConstraints(modifier = Modifier.background(colors.backgroundSurfaceUltraHighlight)) {
            val columns = if (maxWidth - PanelPadding * 2 >= RowMinWidth * 2 + RowGap) 2 else 1
            CmpColumn(
                modifier = Modifier.verticalScroll(rememberScrollState())
                    .padding(start = PanelPadding, end = PanelPadding, top = CmpSpacing.space8, bottom = PanelPadding),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
            ) {
                PanelHead(results.head, onClose)
                resultGroups(results).forEach { (group, tiles) ->
                    ResultGroupRows(group, tiles, columns, firmwareVersion, onTile)
                }
            }
        }
    }
}

@Composable
private fun PanelHead(head: DockHead, onClose: () -> Unit) {
    CmpRow(
        modifier = Modifier.fillMaxWidth().heightIn(min = CmpSize.touchMin),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = if (head is DockHead.Found) {
                stringResource(Strings.string.mountassistant_dock_title_found, head.count)
            } else {
                stringResource(Strings.string.mountassistant_dock_title_nothing)
            },
            modifier = Modifier.weight(1f),
            style = CmpTextStyle.Headline2,
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
    firmwareVersion: String,
    onTile: (DockTile) -> Unit,
) {
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
            style = CmpTextStyle.Additional0,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        )
        tiles.chunked(columns).forEach { row ->
            CmpRow(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(RowGap),
            ) {
                row.forEach { tile ->
                    ResultRow(tile, firmwareVersion, { onTile(tile) }, Modifier.weight(1f).fillMaxHeight())
                }
                repeat(columns - row.size) { CmpBox(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

internal fun resultTag(tile: DockTile): String = tileTag(tile).replace("_tile_", "_result_")

@Composable
private fun ResultRow(tile: DockTile, firmwareVersion: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val content = tileContent(tile, firmwareVersion)
    val setting = (tile as? DockTile.Setting)?.setting
    val subtitle = if (setting != null && !content.blocked) setting.path.joinToString(" › ") else content.subtitle
    CmpListItem(
        title = content.title,
        modifier = modifier.testTag(resultTag(tile)),
        subtitle = subtitle.takeIf { it.isNotBlank() },
        icon = if (setting != null) CmpIcons.Tune else content.icon,
        status = content.tone?.takeIf { content.variant == TileVariant.TOPIC && !content.blocked }?.listStatus(),
        onClick = onClick,
        selected = content.filled && !content.blocked,
        titleMaxLines = RESULT_TITLE_LINES,
        trailing = { RowTrailing(content) },
    )
}

@Composable
private fun RowTrailing(content: TileContent) {
    if (content.value != null) {
        CmpTag(text = content.value)
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
        modifier = Modifier.size(CmpSize.iconM),
        tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
    )
}
