package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.divider.CmpVerticalDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
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

/** Ширина панели карточки, от которой её шапка и матрица — в широком варианте. */
private val CardWideMinWidth = 640.dp

/**
 * Широкое окно. Medium — колонки «Марки · Модели» (комплектации раскрываются под моделью), карточка — на всё
 * окно с крошками. Expanded — колонки и карточка справа; при ширине ≥ 1400 dp — третья колонка «Комплектации».
 * Поиск по CAN — колонка результатов (+ карточка справа на Expanded).
 */
@Composable
internal fun WideCatalog(
    state: VehicleSelectState,
    layout: VehicleLayout,
    windowWidth: Dp,
    actions: VehicleSelectActions
) {
    if (layout == VehicleLayout.MEDIUM && state.selection.carId != null) {
        CmpColumn(modifier = Modifier.fillMaxSize()) {
            CrumbsBar(state = state, actions = actions)
            CenteredCard { Card(state, actions, wide = true, padding = 24.dp) }
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
                CanColumn(catalog, actions, if (expanded) Modifier.width(460.dp) else Modifier.weight(1f))
            } else {
                NameColumns(
                    catalog,
                    actions,
                    third = expanded && windowWidth >= ThirdColumnMinWidth,
                    expanded = expanded
                )
            }
            if (expanded) CardColumn(state.forCard(), actions)
        }
    }
}

/** Срез для колонок каталога и поиска: без карточки, диалогов и фильтра. */
private fun VehicleSelectState.forCatalog(): VehicleSelectState =
    copy(card = LCEState.None, cardUi = CardUi(), dialog = null, filterDraft = null, picking = false)

/** Срез для колонки карточки (с крошками): без списков каталога и ввода поиска по названию. */
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
    picking = false
)

@Composable
private fun TopSearch(state: VehicleSelectState, actions: VehicleSelectActions) {
    CmpColumn(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SearchArea(state = state, wide = true, actions = actions)
        val showRecent = state.searchMode == SearchMode.NAME && state.query.isBlank() && state.recent.isNotEmpty()
        if (showRecent || state.groups.isNotEmpty()) {
            CmpFlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically
            ) {
                if (state.groups.isNotEmpty()) FilterChip(count = state.groups.size, actions = actions)
                if (showRecent) {
                    CmpRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CmpIcon(
                            icon = CmpIcons.History,
                            contentDescription = null,
                            tint = cmpThemeColors().onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        CmpText(
                            text = stringResource(Strings.string.vehicle_catalog_title_recent),
                            style = CmpTextStyle.Body,
                            fontWeight = FontWeight.SemiBold,
                            color = cmpThemeColors().onSurfaceVariant
                        )
                    }
                    state.recent.forEach { car ->
                        CmpChip(
                            text = car.fullName(),
                            onClick = { actions.onRecentOpen(car) },
                            icon = CmpIcons.DirectionsCar,
                            onClear = { actions.onRecentRemove(car) },
                            clearContentDescription = stringResource(Strings.string.vehicle_catalog_a11y_recent_remove),
                            modifier = Modifier.testTag(VehicleTestTags.recent(car.id))
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
    expanded: Boolean
) {
    val brand = state.selection.brand
    val brandWidth = when {
        !expanded -> 300.dp
        state.combo != null -> 340.dp
        else -> 260.dp
    }
    Column(
        title = if (state.combo != null) {
            stringResource(Strings.string.vehicle_catalog_title_search_results)
        } else {
            stringResource(Strings.string.vehicle_catalog_title_brands, state.brands.content?.size ?: 0)
        },
        modifier = Modifier.width(brandWidth)
    ) {
        CatalogList { brandItems(state, actions, showRecent = false, compact = true) }
    }
    CmpVerticalDivider()
    Column(
        title = brand?.name ?: stringResource(Strings.string.vehicle_catalog_title_models),
        modifier = if (expanded) Modifier.width(if (third) 300.dp else 340.dp) else Modifier.weight(1f)
    ) {
        FadeContent(targetState = state, contentKey = { it.selection.brand?.id }) { shown ->
            if (shown.selection.brand == null) {
                Prompt(stringResource(Strings.string.vehicle_catalog_msg_pick_brand))
            } else {
                CatalogList { modelItems(shown, actions, accordion = !third, compact = true) }
            }
        }
    }
    if (third) {
        CmpVerticalDivider()
        TrimsColumn(state, actions)
    }
    if (expanded) CmpVerticalDivider()
}

/** Третья колонка «Комплектации» (Expanded от 1400 dp). */
@Composable
private fun TrimsColumn(state: VehicleSelectState, actions: VehicleSelectActions) {
    Column(title = stringResource(Strings.string.vehicle_catalog_title_trims), modifier = Modifier.width(260.dp)) {
        FadeContent(targetState = state, contentKey = { it.selection.model?.id }) { shown ->
            val single = shown.trims.content?.size == 1
            when {
                shown.selection.model == null -> Prompt(stringResource(Strings.string.vehicle_catalog_msg_pick_model))
                single -> Prompt(stringResource(Strings.string.vehicle_catalog_msg_single_trim))
                else -> CatalogList { trimItems(shown, actions, inline = false) }
            }
        }
    }
}

@Composable
private fun CanColumn(state: VehicleSelectState, actions: VehicleSelectActions, modifier: Modifier) {
    Column(
        title = stringResource(Strings.string.vehicle_catalog_title_can_results, state.canCode.ifEmpty { "—" }),
        modifier = modifier
    ) {
        if (state.canCode.isEmpty()) {
            Prompt(stringResource(Strings.string.vehicle_can_msg_enter))
        } else {
            CatalogList { canItems(state, actions, compact = true) }
        }
    }
    CmpVerticalDivider()
}

@Composable
private fun RowScope.CardColumn(state: VehicleSelectState, actions: VehicleSelectActions) {
    val colors = cmpThemeColors()
    FadeContent(
        targetState = state,
        contentKey = { it.selection.carId },
        modifier = Modifier.weight(1f).fillMaxHeight().background(colors.surfaceContainerLowest)
    ) { shown -> CardColumnContent(shown, actions) }
}

@Composable
private fun CardColumnContent(state: VehicleSelectState, actions: VehicleSelectActions) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        if (state.selection.carId == null) {
            CmpBox(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                CmpColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CmpIcon(
                        icon = CmpIcons.DirectionsCar,
                        contentDescription = null,
                        tint = colors.outline,
                        modifier = Modifier.size(56.dp)
                    )
                    CmpText(
                        text = stringResource(
                            if (state.searchMode == SearchMode.CAN) {
                                Strings.string.vehicle_catalog_msg_card_prompt_can
                            } else {
                                Strings.string.vehicle_catalog_msg_card_prompt
                            }
                        ),
                        style = CmpTextStyle.BodyLarge,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 340.dp)
                    )
                }
            }
        } else {
            CrumbsBar(state = state, actions = actions)
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Card(state, actions, wide = maxWidth >= CardWideMinWidth, padding = 20.dp)
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
        padding = padding
    )
}

/** Колонка каталога: заголовок и содержимое. */
@Composable
private fun Column(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    CmpColumn(modifier = modifier.fillMaxHeight()) {
        CmpText(
            text = title,
            style = CmpTextStyle.Body,
            fontWeight = FontWeight.SemiBold,
            color = cmpThemeColors().onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun Prompt(text: String) {
    CmpText(
        text = text,
        style = CmpTextStyle.BodyLarge,
        color = cmpThemeColors().onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}
