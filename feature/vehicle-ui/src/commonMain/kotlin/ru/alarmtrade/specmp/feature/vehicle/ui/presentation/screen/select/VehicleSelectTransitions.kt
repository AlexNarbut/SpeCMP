package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.detectCombo

/*
 * Переходы по каталогу — чистые функции над state (тестируются без VM). VM только отменяет устаревшие загрузки и
 * шлёт эффекты; решения «куда шагнуть» — здесь.
 */

/** Крошки: к маркам (или результатам CAN-поиска), к моделям марки, к комплектациям модели. */
internal fun VehicleSelectState.withCrumb(target: CrumbTarget): VehicleSelectState {
    val next = when (target) {
        CrumbTarget.TOP -> CatalogSelection()
        CrumbTarget.BRAND -> CatalogSelection(brand = selection.brand)
        CrumbTarget.MODEL -> selection.copy(carId = null, cardSource = CardSource.CATALOG)
    }
    return copy(
        selection = next,
        query = "",
        card = LCEState.None,
        cardUi = CardUi(),
        trims = if (next.model == null) LCEState.None else trims,
    )
}

/**
 * Закрыть карточку: из CAN-поиска и недавних — к началу; из каталога — к комплектациям модели, а если комплектация
 * одна и окно узкое — сразу к моделям (экрана из одной строки нет).
 */
internal fun VehicleSelectState.withCardClosed(layout: VehicleLayout): VehicleSelectState {
    val next = when (selection.cardSource) {
        CardSource.CAN, CardSource.RECENT -> CatalogSelection()

        CardSource.CATALOG -> {
            val singleTrim = trims.content?.size == 1
            selection.copy(
                carId = null,
                model = if (singleTrim && layout == VehicleLayout.COMPACT) null else selection.model,
            )
        }
    }
    return copy(selection = next, card = LCEState.None, cardUi = CardUi())
}

/** Шаг «назад» по каталогу на узком окне в поиске по названию; `null` — шага внутри каталога нет. */
internal fun VehicleSelectState.catalogStepBack(layout: VehicleLayout): VehicleSelectState? {
    if (layout != VehicleLayout.COMPACT || searchMode != SearchMode.NAME) return null
    return when {
        selection.model != null -> copy(selection = selection.copy(model = null), trims = LCEState.None, query = "")
        selection.brand != null -> copy(selection = CatalogSelection(), models = LCEState.None, query = "")
        else -> null
    }
}

/** Сброс выбранного типа устройства: каталог — с экрана выбора типа. */
internal fun VehicleSelectState.withTypeReset(): VehicleSelectState = copy(
    deviceType = null,
    selection = CatalogSelection(),
    query = "",
    combo = null,
    brands = LCEState.None,
    models = LCEState.None,
    trims = LCEState.None,
    comboModels = LCEState.None,
    card = LCEState.None,
    canResults = LCEState.None,
)

/**
 * «Марка модель» в поиске по названию на уровне марок — вместе с запросом: марка распознана по загруженным маркам,
 * без неё модели комбо-поиска сбрасываются. Вне этого уровня state не меняется.
 */
internal fun VehicleSelectState.withDetectedCombo(): VehicleSelectState {
    if (searchMode != SearchMode.NAME || selection.brand != null) return this
    val detected = brands.content?.let { detectCombo(query, it) }
    return copy(combo = detected, comboModels = if (detected == null) LCEState.None else comboModels)
}
