package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.vehicle_can_msg_enter
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_a11y_recent_remove
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_card_prompt
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_card_prompt_can
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_pick_brand
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_pick_model
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_msg_single_trim
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_brands
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_can_results
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_models
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_recent
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_search_results
import ru.alarmtrade.specmp.core.localization.resources.vehicle_catalog_title_trims
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpResizeHandle
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.PaneWidthState
import ru.alarmtrade.specmp.core.ui.view.layout.rememberPaneWidthState
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.FadeContent
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.ThirdColumnMinWidth
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardUi
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CenteredCard
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.SearchMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleLayout
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectState
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card.CardPane

private val CardWideMinWidth = 640.dp
private val PaneMinWidth = 200.dp
private const val PANE_MAX_FRACTION = 0.35f
private const val PANE_BRANDS = "vehicle.brands"
private const val PANE_MODELS = "vehicle.models"
private const val PANE_TRIMS = "vehicle.trims"
private const val PANE_CAN = "vehicle.can"

@Composable
internal fun WideCatalog(
    state: VehicleSelectState,
    layout: VehicleLayout,
    windowWidth: Dp,
    actions: VehicleSelectActions,
) {
    if (layout == VehicleLayout.MEDIUM && state.selection.carId != null) {
        CmpColumn(modifier = Modifier.fillMaxSize()) {
            CrumbsBar(state = state, actions = actions)
            CenteredCard { Card(state, actions, wide = true, padding = CmpSpacing.screenPaddingHMedium) }
        }
        return
    }
    // Колонкам и карточке — каждой свой срез state: свайп фото, вкладки и группы карточки не перерисовывают
    // колонки каталога, а ввод в поиске — карточку (state @Immutable: равный срез — пропуск рекомпозиции).
    val catalog = state.forCatalog()
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        TopSearch(catalog, actions)
        CmpRow(modifier = Modifier.fillMaxSize()) {
            val expanded = layout == VehicleLayout.EXPANDED
            if (catalog.searchMode == SearchMode.CAN) {
                if (expanded) {
                    val pane = rememberCatalogPane(catalog, PANE_CAN, 460.dp, windowWidth)
                    CanColumn(catalog, actions, Modifier.width(pane.width))
                    CatalogPaneHandle(PANE_CAN, pane, actions)
                } else {
                    CanColumn(catalog, actions, Modifier.weight(1f))
                    CmpVerticalDivider()
                }
            } else {
                NameColumns(
                    catalog,
                    actions,
                    third = expanded && windowWidth >= ThirdColumnMinWidth,
                    expanded = expanded,
                    windowWidth = windowWidth,
                )
            }
            if (expanded) CardColumn(state.forCard(), actions)
        }
    }
}

private fun VehicleSelectState.forCatalog(): VehicleSelectState =
    copy(card = LCEState.None, cardUi = CardUi(), dialog = null, filterDraft = null, picking = false)

private fun VehicleSelectState.forCard(): VehicleSelectState = copy(
    query = "",
    brands = LCEState.None,
    models = LCEState.None,
    combo = null,
    comboModels = LCEState.None,
    canResults = LCEState.None,
    recent = emptyList(),
    dialog = null,
    filterDraft = null,
    picking = false,
)

@Composable
private fun TopSearch(state: VehicleSelectState, actions: VehicleSelectActions) {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(
            start = CmpSpacing.screenPaddingHMedium,
            end = CmpSpacing.screenPaddingHMedium,
            top = CmpSpacing.space8,
            bottom = CmpSpacing.space12,
        ),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        SearchArea(state = state, wide = true, actions = actions)
        val showRecent = state.searchMode == SearchMode.NAME && state.query.isBlank() && state.recent.isNotEmpty()
        if (showRecent || state.groups.isNotEmpty()) {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                if (state.groups.isNotEmpty()) FilterChip(count = state.groups.size, actions = actions)
                if (showRecent) {
                    CmpRow(
                        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CmpIcon(
                            icon = CmpIcons.History,
                            contentDescription = null,
                            tint = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                            modifier = Modifier.size(CmpSize.iconM),
                        )
                        CmpText(
                            text = stringResource(Strings.string.vehicle_catalog_title_recent),
                            style = CmpTextStyle.Headline3,
                            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
                        )
                    }
                    state.recent.forEach { car ->
                        CmpChip(
                            text = car.fullName(),
                            onClick = { actions.onRecentOpen(car) },
                            icon = CmpIcons.DirectionsCar,
                            onClear = { actions.onRecentRemove(car) },
                            clearContentDescription = stringResource(Strings.string.vehicle_catalog_a11y_recent_remove),
                            modifier = Modifier.testTag(VehicleTestTags.recent(car.id)),
                        )
                    }
                }
            }
        }
    }
    CmpDivider()
}

@Composable
private fun RowScope.NameColumns(
    state: VehicleSelectState,
    actions: VehicleSelectActions,
    third: Boolean,
    expanded: Boolean,
    windowWidth: Dp,
) {
    val brand = state.selection.brand
    val brandWidth = when {
        !expanded -> 300.dp
        state.combo != null -> 340.dp
        else -> 260.dp
    }
    val brandPane = rememberCatalogPane(state, PANE_BRANDS, brandWidth, windowWidth)
    Column(
        title = if (state.combo != null) {
            stringResource(Strings.string.vehicle_catalog_title_search_results)
        } else {
            stringResource(Strings.string.vehicle_catalog_title_brands, state.brands.content?.size ?: 0)
        },
        modifier = Modifier.width(brandPane.width),
    ) {
        CatalogList(horizontalPadding = CmpSpacing.space12) {
            brandItems(state, actions, showRecent = false, compact = true)
        }
    }
    CatalogPaneHandle(PANE_BRANDS, brandPane, actions)
    val modelPane = rememberCatalogPane(state, PANE_MODELS, if (third) 300.dp else 340.dp, windowWidth)
    Column(
        title = brand?.name ?: stringResource(Strings.string.vehicle_catalog_title_models),
        modifier = if (expanded) Modifier.width(modelPane.width) else Modifier.weight(1f),
    ) {
        FadeContent(targetState = state, contentKey = { it.selection.brand?.id }) { shown ->
            if (shown.selection.brand == null) {
                Prompt(stringResource(Strings.string.vehicle_catalog_msg_pick_brand))
            } else {
                CatalogList(horizontalPadding = CmpSpacing.space12) {
                    modelItems(shown, actions, accordion = !third, compact = true)
                }
            }
        }
    }
    if (expanded) CatalogPaneHandle(PANE_MODELS, modelPane, actions)
    if (third) {
        val trimPane = rememberCatalogPane(state, PANE_TRIMS, 260.dp, windowWidth)
        TrimsColumn(state, actions, Modifier.width(trimPane.width))
        CatalogPaneHandle(PANE_TRIMS, trimPane, actions)
    }
}

@Composable
private fun rememberCatalogPane(state: VehicleSelectState, key: String, default: Dp, windowWidth: Dp): PaneWidthState =
    rememberPaneWidthState(
        saved = state.paneWidthsDp[key]?.dp,
        default = default,
        min = PaneMinWidth,
        max = windowWidth * PANE_MAX_FRACTION,
    )

@Composable
private fun CatalogPaneHandle(key: String, pane: PaneWidthState, actions: VehicleSelectActions) {
    CmpResizeHandle(state = pane, onDragStop = { actions.onPaneWidthChange(key, it.value.roundToInt()) })
}

@Composable
private fun TrimsColumn(state: VehicleSelectState, actions: VehicleSelectActions, modifier: Modifier) {
    Column(title = stringResource(Strings.string.vehicle_catalog_title_trims), modifier = modifier) {
        FadeContent(targetState = state, contentKey = { it.selection.model?.id }) { shown ->
            val single = shown.trims.content?.size == 1
            when {
                shown.selection.model == null -> Prompt(stringResource(Strings.string.vehicle_catalog_msg_pick_model))

                single -> Prompt(stringResource(Strings.string.vehicle_catalog_msg_single_trim))

                else -> CatalogList(horizontalPadding = CmpSpacing.space12) {
                    trimItems(shown, actions, inline = false)
                }
            }
        }
    }
}

@Composable
private fun CanColumn(state: VehicleSelectState, actions: VehicleSelectActions, modifier: Modifier) {
    Column(
        title = stringResource(Strings.string.vehicle_catalog_title_can_results, state.canCode.ifEmpty { "—" }),
        modifier = modifier,
    ) {
        if (state.canCode.isEmpty()) {
            Prompt(stringResource(Strings.string.vehicle_can_msg_enter))
        } else {
            CatalogList(horizontalPadding = CmpSpacing.space12) { canItems(state, actions, compact = true) }
        }
    }
}

@Composable
private fun RowScope.CardColumn(state: VehicleSelectState, actions: VehicleSelectActions) {
    FadeContent(
        targetState = state,
        contentKey = { it.selection.carId },
        modifier = Modifier.weight(1f).fillMaxHeight(),
    ) { shown -> CardColumnContent(shown, actions) }
}

@Composable
private fun CardColumnContent(state: VehicleSelectState, actions: VehicleSelectActions) {
    val colors = CmpTheme.colors
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        if (state.selection.carId == null) {
            CmpBox(modifier = Modifier.fillMaxSize().padding(CmpSpacing.space24), contentAlignment = Alignment.Center) {
                CmpColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CmpSpacing.space12),
                ) {
                    CmpIcon(
                        icon = CmpIcons.DirectionsCar,
                        contentDescription = null,
                        tint = colors.foregroundOnDarkTertiary,
                        modifier = Modifier.size(CmpSize.iconXxl),
                    )
                    CmpText(
                        text = stringResource(
                            if (state.searchMode == SearchMode.CAN) {
                                Strings.string.vehicle_catalog_msg_card_prompt_can
                            } else {
                                Strings.string.vehicle_catalog_msg_card_prompt
                            },
                        ),
                        style = CmpTextStyle.Paragraph1,
                        color = colors.foregroundOnDarkSecondaryDefault,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 340.dp),
                    )
                }
            }
        } else {
            CrumbsBar(state = state, actions = actions)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Card(state, actions, wide = maxWidth >= CardWideMinWidth, padding = CmpSpacing.space24)
            }
        }
    }
}

@Composable
private fun Card(state: VehicleSelectState, actions: VehicleSelectActions, wide: Boolean, padding: Dp) {
    CardPane(
        card = state.card,
        ui = state.cardUi,
        groups = state.groups,
        wide = wide,
        pick = state.mode.pick,
        actions = actions.card,
        onRetry = actions.onRetry,
        onPick = actions.onPick,
        padding = padding,
    )
}

@Composable
private fun Column(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    CmpColumn(modifier = modifier.fillMaxHeight()) {
        CmpText(
            text = title,
            style = CmpTextStyle.Headline3,
            color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
            maxLines = 1,
            modifier = Modifier.padding(
                start = CmpSpacing.space16,
                end = CmpSpacing.space12,
                top = CmpSpacing.space12,
                bottom = CmpSpacing.space4,
            ),
        )
        content()
    }
}

@Composable
private fun Prompt(text: String) {
    CmpText(
        text = text,
        style = CmpTextStyle.Paragraph2,
        color = CmpTheme.colors.foregroundOnDarkSecondaryDefault,
        modifier = Modifier.padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
    )
}
