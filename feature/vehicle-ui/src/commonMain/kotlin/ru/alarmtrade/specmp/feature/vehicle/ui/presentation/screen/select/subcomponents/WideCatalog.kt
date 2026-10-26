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
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.ThirdColumnMinWidth
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CenteredCard
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.SearchMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleLayout
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectState
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.card.CardPane
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.valueOrNull
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_msg_enter
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_card_prompt
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_card_prompt_can
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_pick_brand
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_pick_model
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_single_trim
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_brands
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_can_results
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_models
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_recent
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_search_results
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_trims

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
    CmpColumn(modifier = Modifier.fillMaxSize()) {
        TopSearch(state, actions)
        CmpRow(modifier = Modifier.fillMaxSize()) {
            val expanded = layout == VehicleLayout.EXPANDED
            if (state.searchMode == SearchMode.CAN) {
                CanColumn(state, actions, if (expanded) Modifier.width(460.dp) else Modifier.weight(1f))
            } else {
                NameColumns(state, actions, third = expanded && windowWidth >= ThirdColumnMinWidth, expanded = expanded)
            }
            if (expanded) CardColumn(state, actions)
        }
    }
}

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
                            text = stringResource(Res.string.vehicle_catalog_title_recent),
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
            stringResource(Res.string.vehicle_catalog_title_search_results)
        } else {
            stringResource(Res.string.vehicle_catalog_title_brands, state.brands.valueOrNull()?.size ?: 0)
        },
        modifier = Modifier.width(brandWidth)
    ) {
        CatalogList { brandItems(state, actions, showRecent = false, compact = true) }
    }
    CmpVerticalDivider()
    Column(
        title = brand?.name ?: stringResource(Res.string.vehicle_catalog_title_models),
        modifier = if (expanded) Modifier.width(if (third) 300.dp else 340.dp) else Modifier.weight(1f)
    ) {
        if (brand == null) {
            Prompt(stringResource(Res.string.vehicle_catalog_msg_pick_brand))
        } else {
            CatalogList { modelItems(state, actions, accordion = !third, compact = true) }
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
    Column(title = stringResource(Res.string.vehicle_catalog_title_trims), modifier = Modifier.width(260.dp)) {
        val single = state.trims.valueOrNull()?.size == 1
        when {
            state.selection.model == null -> Prompt(stringResource(Res.string.vehicle_catalog_msg_pick_model))
            single -> Prompt(stringResource(Res.string.vehicle_catalog_msg_single_trim))
            else -> CatalogList { trimItems(state, actions, inline = false) }
        }
    }
}

@Composable
private fun CanColumn(state: VehicleSelectState, actions: VehicleSelectActions, modifier: Modifier) {
    Column(
        title = stringResource(Res.string.vehicle_catalog_title_can_results, state.canCode.ifEmpty { "—" }),
        modifier = modifier
    ) {
        if (state.canCode.isEmpty()) {
            Prompt(stringResource(Res.string.vehicle_can_msg_enter))
        } else {
            CatalogList { canItems(state, actions, compact = true) }
        }
    }
    CmpVerticalDivider()
}

@Composable
private fun RowScope.CardColumn(state: VehicleSelectState, actions: VehicleSelectActions) {
    val colors = cmpThemeColors()
    CmpColumn(modifier = Modifier.weight(1f).fillMaxHeight().background(colors.surfaceContainerLowest)) {
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
                                Res.string.vehicle_catalog_msg_card_prompt_can
                            } else {
                                Res.string.vehicle_catalog_msg_card_prompt
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
