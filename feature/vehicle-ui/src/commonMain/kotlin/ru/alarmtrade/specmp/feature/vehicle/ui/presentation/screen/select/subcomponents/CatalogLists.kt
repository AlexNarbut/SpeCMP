package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpOutlinedButton
import ru.alarmtrade.specmp.core.ui.view.divider.CmpDivider
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.BrandLogo
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.CarPhoto
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.FilteredOut
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.LoadFailed
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.PhotoThumbSize
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.SkeletonRows
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.StateAction
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.ThumbRadius
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.ThumbSize
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.VehicleRow
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.fadeItem
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.fadeItems
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.highlighted
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.looksLikeCanCode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.matchesQuery
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.yearsText
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CatalogLevel
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.Load
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.SearchMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectState
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_label_result
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_msg_enter
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_msg_found
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_msg_none
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_title_none
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_a11y_recent_remove
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_label_all_models
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_label_recent
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_combo_none
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_digits
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_models_none_query
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_nothing
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_nothing_can
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_trims_empty
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_msg_trims_none_query
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_combo
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_combo_query
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_digits
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_empty
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_models_empty
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_nothing
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_recent
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_title_trims_empty
import specmp.feature.vehicle_ui.generated.resources.vehicle_common_btn_pick
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_btn_by_can
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_btn_by_name

/** Список уровня каталога: прокрутка, отступы. */
@Composable
internal fun CatalogList(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        content = content
    )
}

/** Состояние загрузки уровня вместо списка; `null` — данные готовы, рисуем список. */
internal fun <T> LazyListScope.levelState(
    load: Load<T>,
    level: CatalogLevel,
    actions: VehicleSelectActions,
    skeleton: Int,
    thumb: Boolean = false,
    compact: Boolean = false
): T? = when (load) {
    is Load.Ready -> load.value

    is Load.Failed -> {
        fadeItem("failed-$level") {
            LoadFailed(offline = load.offline, onRetry = { actions.onRetry(level) }, compact = compact)
        }
        null
    }

    Load.Idle, Load.Loading -> {
        fadeItem("skeleton-$level") { SkeletonRows(count = skeleton, thumb = thumb) }
        null
    }
}

// ── Марки ──────────────────────────────────────────────────────

/** Марки: недавние (если [showRecent]), поиск «марка модель», буквы-разделители, состояния пустоты. */
internal fun LazyListScope.brandItems(
    state: VehicleSelectState,
    actions: VehicleSelectActions,
    showRecent: Boolean,
    compact: Boolean
) {
    val brands = levelState(state.brands, CatalogLevel.BRANDS, actions, skeleton = 6, compact = compact) ?: return
    val combo = state.combo
    if (combo != null) {
        comboItems(state, actions)
        return
    }
    if (showRecent && state.query.isBlank() && state.recent.isNotEmpty()) recentItems(state.recent, actions)
    val list = brands.filter { matchesQuery(it.name, state.query) }
    if (list.isEmpty()) {
        fadeItem("brands-empty") { EmptyBrands(state, actions, compact) }
        return
    }
    var letter: Char? = null
    list.forEach { brand ->
        val first = brand.name.firstOrNull()?.uppercaseChar()
        if (state.query.isBlank() && first != letter) {
            letter = first
            fadeItem("letter-$first") { LetterHeader(first?.toString().orEmpty()) }
        }
        fadeItem("brand-${brand.id}") {
            BrandRow(
                brand = brand,
                query = state.query,
                selected = state.selection.brand?.id == brand.id,
                onClick = { actions.onBrandPick(brand) }
            )
        }
    }
}

@Composable
private fun EmptyBrands(state: VehicleSelectState, actions: VehicleSelectActions, compact: Boolean) {
    val q = state.query.trim()
    when {
        q.isEmpty() && state.groups.isNotEmpty() -> FilteredOut(onReset = actions.onFilterClear, compact = compact)

        q.isEmpty() -> CmpStateBox(
            icon = CmpIcons.Inventory2,
            title = stringResource(Res.string.vehicle_catalog_title_empty),
            compact = compact
        )

        looksLikeCanCode(q) && state.mode.canSearchAvailable -> CmpStateBox(
            icon = CmpIcons.Pin,
            title = stringResource(Res.string.vehicle_catalog_title_digits, q),
            text = stringResource(Res.string.vehicle_catalog_msg_digits),
            compact = compact
        ) {
            StateAction(text = stringResource(Res.string.vehicle_search_btn_by_can), onClick = {
                actions.onSearchByCan(q)
            })
        }

        else -> CmpStateBox(
            icon = CmpIcons.SearchOff,
            title = stringResource(Res.string.vehicle_catalog_title_nothing),
            text = stringResource(
                if (state.mode.canSearchAvailable) {
                    Res.string.vehicle_catalog_msg_nothing_can
                } else {
                    Res.string.vehicle_catalog_msg_nothing
                }
            ),
            compact = compact
        ) {
            if (state.mode.canSearchAvailable) {
                StateAction(
                    text = stringResource(Res.string.vehicle_search_btn_by_can),
                    onClick = { actions.onSearchByCan("") }
                )
            }
        }
    }
}

private fun LazyListScope.comboItems(state: VehicleSelectState, actions: VehicleSelectActions) {
    val combo = state.combo ?: return
    fadeItem("combo-brand") {
        VehicleRow(
            title = highlighted(combo.brand.name, state.query),
            subtitle = stringResource(Res.string.vehicle_catalog_label_all_models),
            onClick = { actions.onBrandPick(combo.brand) },
            lead = { BrandLogo(name = combo.brand.name, url = combo.brand.logoUrl) },
            modifier = Modifier.testTag(VehicleTestTags.brand(combo.brand.id))
        )
    }
    fadeItem("combo-title") {
        SectionTitle(
            if (combo.rest.isEmpty()) {
                stringResource(Res.string.vehicle_catalog_title_combo, combo.brand.name)
            } else {
                stringResource(Res.string.vehicle_catalog_title_combo_query, combo.brand.name, combo.rest)
            }
        )
    }
    val models = levelState(state.comboModels, CatalogLevel.COMBO, actions, skeleton = 3, thumb = true, compact = true)
        ?: return
    val matched = models.filter { matchesQuery(it.name, combo.rest) }
    if (matched.isEmpty()) {
        fadeItem("combo-none") {
            CmpStateBox(
                icon = CmpIcons.SearchOff,
                title = stringResource(Res.string.vehicle_catalog_title_nothing),
                text = stringResource(Res.string.vehicle_catalog_msg_combo_none, combo.brand.name, combo.rest),
                compact = true
            )
        }
        return
    }
    fadeItems(matched, key = { "combo-model-${it.id}" }) { model ->
        ModelRow(model = model, query = combo.rest, selected = false, onClick = { actions.onModelPick(model) })
    }
}

internal fun LazyListScope.recentItems(recent: List<CarSummary>, actions: VehicleSelectActions) {
    fadeItem("recent-title") { SectionTitle(stringResource(Res.string.vehicle_catalog_title_recent)) }
    fadeItems(recent, key = { "recent-${it.id}" }) { car ->
        val colors = cmpThemeColors()
        VehicleRow(
            title = AnnotatedString(car.fullName()),
            subtitle = stringResource(Res.string.vehicle_catalog_label_recent, yearsText(car.years), car.canCode),
            onClick = { actions.onRecentOpen(car) },
            dense = true,
            trailing = {
                CmpIconButton(
                    onClick = { actions.onRecentRemove(car) },
                    icon = CmpIcons.Close,
                    contentDescription = stringResource(Res.string.vehicle_catalog_a11y_recent_remove),
                    tint = colors.onSurfaceVariant
                )
            },
            lead = {
                CmpBox(
                    modifier = Modifier
                        .size(ThumbSize)
                        .background(colors.secondaryContainer, RoundedCornerShape(ThumbRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    CmpIcon(icon = CmpIcons.History, contentDescription = null, tint = colors.onSecondaryContainer)
                }
            },
            modifier = Modifier.testTag(VehicleTestTags.recent(car.id))
        )
    }
    fadeItem("recent-divider") { CmpDivider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) }
}

@Composable
internal fun BrandRow(brand: CarBrand, query: String, selected: Boolean, onClick: () -> Unit) {
    VehicleRow(
        title = highlighted(brand.name, query),
        onClick = onClick,
        selected = selected,
        dense = true,
        lead = { BrandLogo(name = brand.name, url = brand.logoUrl) },
        modifier = Modifier.testTag(VehicleTestTags.brand(brand.id))
    )
}

// ── Модели ─────────────────────────────────────────────────────

/** Модели выбранной марки; [accordion] — комплектации раскрываются под выбранной моделью (широкое окно). */
internal fun LazyListScope.modelItems(
    state: VehicleSelectState,
    actions: VehicleSelectActions,
    accordion: Boolean,
    compact: Boolean
) {
    val brand = state.selection.brand ?: return
    val models = levelState(state.models, CatalogLevel.MODELS, actions, skeleton = 5, thumb = true, compact = compact)
        ?: return
    if (models.isEmpty()) {
        fadeItem("models-empty") {
            if (state.groups.isNotEmpty()) {
                FilteredOut(onReset = actions.onFilterClear, compact = compact)
            } else {
                CmpStateBox(
                    icon = CmpIcons.Inventory2,
                    title = stringResource(Res.string.vehicle_catalog_title_models_empty),
                    compact = compact
                )
            }
        }
        return
    }
    val query = if (accordion || state.selection.model == null) state.query else ""
    val list = models.filter { matchesQuery(it.name, query) }
    if (list.isEmpty()) {
        fadeItem("models-none") {
            CmpStateBox(
                icon = CmpIcons.SearchOff,
                title = stringResource(Res.string.vehicle_catalog_title_nothing),
                text = stringResource(Res.string.vehicle_catalog_msg_models_none_query, brand.name, state.query.trim()),
                compact = compact
            )
        }
        return
    }
    list.forEach { model ->
        val open = accordion && state.selection.model?.id == model.id && state.selection.carId == null
        fadeItem("model-${model.id}") {
            ModelRow(
                model = model,
                query = query,
                selected = state.selection.model?.id == model.id && !open,
                expandable = accordion,
                expanded = open,
                onClick = { actions.onModelPick(model) }
            )
        }
        if (open) trimItems(state, actions, inline = true)
    }
}

@Composable
internal fun ModelRow(
    model: CarModel,
    query: String,
    selected: Boolean,
    onClick: () -> Unit,
    expandable: Boolean = false,
    expanded: Boolean = false
) {
    VehicleRow(
        title = highlighted(model.name, query),
        subtitle = yearsText(model.years),
        onClick = onClick,
        selected = selected,
        chevron = !expandable,
        lead = { CarPhoto(url = model.imageUrl, radius = ThumbRadius, modifier = Modifier.size(PhotoThumbSize)) },
        trailing = if (expandable) {
            {
                CmpIcon(
                    icon = if (expanded) CmpIcons.KeyboardArrowUp else CmpIcons.KeyboardArrowDown,
                    contentDescription = null,
                    tint = cmpThemeColors().onSurfaceVariant
                )
            }
        } else {
            null
        },
        modifier = Modifier.testTag(VehicleTestTags.model(model.id))
    )
}

// ── Комплектации ───────────────────────────────────────────────

/** Комплектации выбранной модели; [inline] — под моделью в аккордеоне (без фильтра поиском). */
internal fun LazyListScope.trimItems(state: VehicleSelectState, actions: VehicleSelectActions, inline: Boolean) {
    val model = state.selection.model ?: return
    val trims =
        levelState(state.trims, CatalogLevel.TRIMS, actions, skeleton = 2, thumb = true, compact = true) ?: return
    if (trims.isEmpty()) {
        fadeItem("trims-empty") {
            CmpStateBox(
                icon = CmpIcons.Inventory2,
                title = stringResource(Res.string.vehicle_catalog_title_trims_empty),
                text = stringResource(Res.string.vehicle_catalog_msg_trims_empty, model.name),
                compact = true
            )
        }
        return
    }
    val query = if (inline) "" else state.query
    val list = trims.filter { matchesQuery(it.name, query) }
    if (list.isEmpty()) {
        fadeItem("trims-none") {
            CmpStateBox(
                icon = CmpIcons.SearchOff,
                title = stringResource(Res.string.vehicle_catalog_title_nothing),
                text = stringResource(Res.string.vehicle_catalog_msg_trims_none_query),
                compact = true
            )
        }
        return
    }
    fadeItems(list, key = { "trim-${it.id}" }) { trim ->
        TrimRow(
            trim = trim,
            imageUrl = model.imageUrl,
            query = query,
            selected = state.selection.carId == trim.id,
            pick = state.mode.pick,
            inline = inline,
            actions = actions
        )
    }
}

@Composable
private fun TrimRow(
    trim: CarTrim,
    imageUrl: String?,
    query: String,
    selected: Boolean,
    pick: Boolean,
    inline: Boolean,
    actions: VehicleSelectActions
) {
    VehicleRow(
        title = highlighted(trim.name, query),
        onClick = { actions.onTrimOpen(trim.id) },
        selected = selected,
        dense = true,
        // Своего фото у комплектации нет — фото модели.
        lead = { CarPhoto(url = imageUrl, radius = ThumbRadius, modifier = Modifier.size(PhotoThumbSize)) },
        modifier = Modifier
            .padding(start = if (inline) 24.dp else 0.dp)
            .testTag(VehicleTestTags.trim(trim.id)),
        action = if (pick) {
            {
                PickButton(
                    onClick = { actions.onPick(trim.id) },
                    modifier = Modifier.testTag(VehicleTestTags.trimPick(trim.id))
                )
            }
        } else {
            null
        }
    )
}

@Composable
internal fun PickButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmpOutlinedButton(
        text = stringResource(Res.string.vehicle_common_btn_pick),
        onClick = onClick,
        modifier = modifier.padding(top = 6.dp).heightIn(min = 48.dp)
    )
}

// ── Поиск по CAN-коду ──────────────────────────────────────────

internal fun LazyListScope.canItems(state: VehicleSelectState, actions: VehicleSelectActions, compact: Boolean) {
    if (state.canCode.length < CAN_LENGTH) {
        fadeItem("can-enter") {
            CmpText(
                text = stringResource(Res.string.vehicle_can_msg_enter),
                style = CmpTextStyle.BodyLarge,
                color = cmpThemeColors().onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
        return
    }
    val cars = levelState(state.canResults, CatalogLevel.CAN, actions, skeleton = 3, thumb = true, compact = compact)
        ?: return
    if (cars.isEmpty()) {
        fadeItem("can-none") {
            CmpStateBox(
                icon = CmpIcons.SearchOff,
                title = stringResource(Res.string.vehicle_can_title_none, state.canCode),
                text = stringResource(Res.string.vehicle_can_msg_none),
                compact = compact
            ) {
                StateAction(
                    text = stringResource(Res.string.vehicle_search_btn_by_name),
                    onClick = { actions.onSearchModeChange(SearchMode.NAME) }
                )
            }
        }
        return
    }
    fadeItem("can-found") { SectionTitle(stringResource(Res.string.vehicle_can_msg_found, cars.size)) }
    fadeItems(cars, key = { "can-${it.id}" }) { car ->
        VehicleRow(
            title = AnnotatedString("${car.brand} ${car.model}"),
            subtitle = stringResource(Res.string.vehicle_can_label_result, yearsText(car.years), car.name),
            onClick = { actions.onCanResultOpen(car) },
            selected = state.selection.carId == car.id,
            lead = { CarPhoto(url = car.imageUrl, radius = ThumbRadius, modifier = Modifier.size(PhotoThumbSize)) },
            action = if (state.mode.pick) {
                { PickButton(onClick = { actions.onPick(car.id) }) }
            } else {
                null
            },
            modifier = Modifier.testTag(VehicleTestTags.canResult(car.id))
        )
    }
}

private const val CAN_LENGTH = 4

// ── Общее ──────────────────────────────────────────────────────

@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    CmpText(
        text = text,
        style = CmpTextStyle.Body,
        fontWeight = FontWeight.SemiBold,
        color = cmpThemeColors().onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun LetterHeader(letter: String) {
    CmpText(
        text = letter,
        style = CmpTextStyle.BodyLarge,
        fontWeight = FontWeight.Bold,
        color = cmpThemeColors().primary,
        modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 4.dp).height(24.dp)
    )
}
