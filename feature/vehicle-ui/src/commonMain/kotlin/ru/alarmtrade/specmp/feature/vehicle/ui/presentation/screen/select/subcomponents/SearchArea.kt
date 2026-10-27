package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.cmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.chip.CmpChip
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpCodeField
import ru.alarmtrade.specmp.core.ui.view.input.CmpSearchField
import ru.alarmtrade.specmp.core.ui.view.input.CmpSegmentedButton
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpFlowRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumb
import ru.alarmtrade.specmp.core.ui.view.navigation.CmpBreadcrumbs
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.VehicleTestTags
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.fullName
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.yearsText
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CrumbTarget
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.Load
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.SearchMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectState
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.valueOrNull
import specmp.feature.vehicle_ui.generated.resources.Res
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_hint_firmware
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_hint_prompt
import specmp.feature.vehicle_ui.generated.resources.vehicle_can_label_code
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_label_crumb_brands
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_label_crumb_can
import specmp.feature.vehicle_ui.generated.resources.vehicle_catalog_label_crumb_model
import specmp.feature.vehicle_ui.generated.resources.vehicle_common_a11y_clear
import specmp.feature.vehicle_ui.generated.resources.vehicle_filter_a11y_clear
import specmp.feature.vehicle_ui.generated.resources.vehicle_filter_label_chip
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_btn_mode_can
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_btn_mode_name
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_placeholder_brand
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_placeholder_brand_can
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_placeholder_model
import specmp.feature.vehicle_ui.generated.resources.vehicle_search_placeholder_trim

private const val CAN_CODE_LENGTH = 4
private val FIELD_HEIGHT = 56.dp
private val HINT_HEIGHT = 24.dp

/**
 * Зона поиска: сегменты «По названию | По CAN-коду» (кроме каталога конфигураций) и поле запроса либо
 * 4 ячейки CAN-кода. [wide] — сегменты и поле в одну строку.
 */
@Composable
internal fun SearchArea(
    state: VehicleSelectState,
    wide: Boolean,
    actions: VehicleSelectActions,
    modifier: Modifier = Modifier
) {
    val segments: @Composable (Modifier) -> Unit = { segmentsModifier ->
        CmpSegmentedButton(
            options = listOf(
                stringResource(Res.string.vehicle_search_btn_mode_name),
                stringResource(Res.string.vehicle_search_btn_mode_can)
            ),
            selectedIndex = state.searchMode.ordinal,
            onSelect = { actions.onSearchModeChange(SearchMode.entries[it]) },
            modifier = segmentsModifier
        )
    }
    val input: @Composable (Modifier) -> Unit = { inputModifier ->
        if (state.searchMode == SearchMode.NAME) {
            NameField(state, actions, inputModifier)
        } else {
            CanField(state, actions, inputModifier)
        }
    }
    if (wide) {
        CmpRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Под полем — строка подсказки: сегменты центрируются по высоте самого поля, а не всего слота.
            if (state.mode.canSearchAvailable) {
                CmpBox(modifier = Modifier.height(FIELD_HEIGHT), contentAlignment = Alignment.CenterStart) {
                    segments(Modifier.widthIn(max = 340.dp))
                }
            }
            input(Modifier.widthIn(max = 560.dp).weight(1f, fill = false))
        }
    } else {
        CmpColumn(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.mode.canSearchAvailable) segments(Modifier.fillMaxWidth())
            input(Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NameField(state: VehicleSelectState, actions: VehicleSelectActions, modifier: Modifier) {
    val selection = state.selection
    val placeholder = when {
        selection.model != null && selection.carId == null ->
            stringResource(Res.string.vehicle_search_placeholder_trim, selection.model.name)

        selection.brand != null -> stringResource(Res.string.vehicle_search_placeholder_model, selection.brand.name)

        state.mode.canSearchAvailable -> stringResource(Res.string.vehicle_search_placeholder_brand_can)

        else -> stringResource(Res.string.vehicle_search_placeholder_brand)
    }
    InputSlot(modifier = modifier, hint = {}) {
        CmpSearchField(
            value = state.query,
            onValueChange = actions.onQueryChange,
            placeholder = placeholder,
            clearContentDescription = stringResource(Res.string.vehicle_common_a11y_clear),
            modifier = Modifier.fillMaxWidth().testTag(VehicleTestTags.SEARCH)
        )
    }
}

/**
 * Поле поиска и строка подсказки под ним фиксированной высоты в обоих режимах — переключение «По названию /
 * По CAN-коду» не сдвигает список под зоной поиска.
 */
@Composable
private fun InputSlot(modifier: Modifier, hint: @Composable () -> Unit, field: @Composable () -> Unit) {
    CmpColumn(modifier = modifier) {
        CmpBox(modifier = Modifier.fillMaxWidth().height(FIELD_HEIGHT), contentAlignment = Alignment.CenterStart) {
            field()
        }
        CmpBox(modifier = Modifier.fillMaxWidth().height(HINT_HEIGHT), contentAlignment = Alignment.CenterStart) {
            hint()
        }
    }
}

@Composable
private fun CanField(state: VehicleSelectState, actions: VehicleSelectActions, modifier: Modifier) {
    val fromFirmware = state.mode.firmwareCanCode != null && state.canCode == state.mode.firmwareCanCode
    InputSlot(modifier = modifier, hint = { CanHint(state, fromFirmware) }) {
        CmpCodeField(
            value = state.canCode,
            onValueChange = actions.onCanCodeChange,
            label = stringResource(Res.string.vehicle_can_label_code),
            clearContentDescription = stringResource(Res.string.vehicle_common_a11y_clear),
            length = CAN_CODE_LENGTH,
            modifier = Modifier.testTag(VehicleTestTags.CAN_INPUT)
        )
    }
}

@Composable
private fun CanHint(state: VehicleSelectState, fromFirmware: Boolean) {
    val colors = cmpThemeColors()
    when {
        fromFirmware -> CmpRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CmpIcon(
                icon = CmpIcons.Memory,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            CmpText(
                text = stringResource(Res.string.vehicle_can_hint_firmware),
                style = CmpTextStyle.Body,
                color = colors.primary
            )
        }

        state.canCode.length < CAN_CODE_LENGTH -> CmpText(
            text = stringResource(Res.string.vehicle_can_hint_prompt),
            style = CmpTextStyle.Body,
            color = colors.onSurfaceVariant
        )
    }
}

/**
 * Крошки пути и чип фильтра (узкое окно — под поиском; карточка на Medium — над ней). [typeChip] — чип типа
 * устройства на узком окне (в шапке ему не хватает места).
 */
@Composable
internal fun CrumbsBar(
    state: VehicleSelectState,
    actions: VehicleSelectActions,
    modifier: Modifier = Modifier,
    typeChip: Boolean = false
) {
    val crumbs = crumbs(state)
    val filterActive = state.groups.isNotEmpty()
    val type = state.deviceType.takeIf { typeChip && state.mode.typeSelectable }
    if (crumbs.size <= 1 && !filterActive && type == null) return
    CmpFlowRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        type?.let { TypeChip(type = it, onPick = actions.onTypePick) }
        if (crumbs.size > 1) {
            CmpBreadcrumbs(
                items = crumbs.map { CmpBreadcrumb(it.first) },
                onItemClick = { index -> crumbs[index].second?.let(actions.onCrumb) }
            )
        }
        if (filterActive) FilterChip(count = state.groups.size, actions = actions)
    }
}

@Composable
internal fun FilterChip(count: Int, actions: VehicleSelectActions) {
    CmpChip(
        text = stringResource(Res.string.vehicle_filter_label_chip, count),
        onClick = actions.onFilterOpen,
        icon = CmpIcons.FilterAlt,
        selected = true,
        onClear = actions.onFilterClear,
        clearContentDescription = stringResource(Res.string.vehicle_filter_a11y_clear),
        modifier = Modifier.testTag(VehicleTestTags.FILTER_CLEAR)
    )
}

/** Крошки: подпись и куда ведёт (`null` — текущий уровень, не нажимается). */
@Composable
private fun crumbs(state: VehicleSelectState): List<Pair<String, CrumbTarget?>> {
    val selection = state.selection
    val card = state.card.valueOrNull()?.summary?.takeIf { it.id == selection.carId }
    val items = mutableListOf<Pair<String, CrumbTarget?>>()
    if (state.searchMode == SearchMode.CAN) {
        items += stringResource(Res.string.vehicle_catalog_label_crumb_can, state.canCode) to CrumbTarget.TOP
        if (selection.carId != null && card != null) items += card.fullName() to null
        return items
    }
    items += stringResource(Res.string.vehicle_catalog_label_crumb_brands) to CrumbTarget.TOP
    selection.brand?.let { items += it.name to CrumbTarget.BRAND }
    selection.model?.let {
        items +=
            stringResource(Res.string.vehicle_catalog_label_crumb_model, it.name, yearsText(it.years)) to
            CrumbTarget.MODEL
    }
    if (selection.carId != null) {
        val trimName = state.trims.valueOrNull()?.firstOrNull { it.id == selection.carId }?.name
        val label = when {
            selection.brand == null && card != null -> card.fullName()
            trimName != null -> trimName
            card != null -> card.name
            else -> null
        }
        label?.let { items += it to null }
    }
    return items.mapIndexed { index, item -> if (index == items.lastIndex) item.first to null else item }
}

/** Уровень, ещё не загруженный, — для подсказок «пусто». */
internal fun <T> Load<List<T>>.isEmptyReady(): Boolean = (this as? Load.Ready)?.value?.isEmpty() == true
