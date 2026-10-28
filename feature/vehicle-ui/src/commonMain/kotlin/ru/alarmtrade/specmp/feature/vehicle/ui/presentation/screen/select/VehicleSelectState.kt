package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError

internal enum class SearchMode { NAME, CAN }

/** Откуда открыта карточка — от этого зависит «назад» на узком окне. */
internal enum class CardSource { CATALOG, CAN, RECENT }

/** Уровень каталога, который можно перезагрузить «Повторить». */
internal enum class CatalogLevel { BRANDS, MODELS, TRIMS, COMBO, CAN, CARD }

/** Вкладки карточки (показываются только непустые). */
internal enum class CardTab { FEATURES, MOUNT_POINTS, SCHEMES, MAPS, USER_MANUALS }

/** Раскладка по ширине окна. */
internal enum class VehicleLayout {
    COMPACT,
    MEDIUM,
    EXPANDED;

    val isWide: Boolean get() = this != COMPACT
}

/** Выбранный путь в каталоге. */
internal data class CatalogSelection(
    val brand: CarBrand? = null,
    val model: CarModel? = null,
    /** Комплектация, чья карточка открыта. */
    val carId: Int? = null,
    val cardSource: CardSource = CardSource.CATALOG
)

/** Поиск «марка модель» на уровне марок: марка распознана, [rest] — фильтр её моделей. */
internal data class ComboSearch(val brand: CarBrand, val rest: String)

/** Что открыто внутри карточки. */
internal data class CardUi(
    val tab: CardTab? = null,
    /** Открытая точка подключения (индекс), `null` — список точек. */
    val point: Int? = null,
    val photo: Int = 0,
    val viewerOpen: Boolean = false,
    val notesExpanded: Boolean = false,
    val collapsedGroups: Set<CarFeatureGroup> = emptySet()
)

internal sealed interface VehicleDialog {
    /** Примечание к возможности автомобиля. */
    data class CapabilityNote(val kind: CapabilityKind, val capability: CarCapability) : VehicleDialog

    /** Выбранный автомобиль не совпадает по CAN-коду с прошивкой блока. */
    data class CanMismatch(val car: CarSummary, val firmwareCanCode: String) : VehicleDialog
}

internal enum class CapabilityKind { ENGINE_BLOCK, KEYLESS_BYPASS }

@Immutable
internal data class VehicleSelectState(
    val mode: VehicleSelectModeUi,
    /** Тип устройства не задан запросом и ещё не выбран — экран выбора типа. */
    val deviceType: VehicleDeviceType? = null,
    val needsType: Boolean = false,
    val searchMode: SearchMode = SearchMode.NAME,
    val query: String = "",
    val canCode: String = "",
    val selection: CatalogSelection = CatalogSelection(),
    val brands: LCEState<List<CarBrand>, CatalogError> = LCEState.None,
    val models: LCEState<List<CarModel>, CatalogError> = LCEState.None,
    val trims: LCEState<List<CarTrim>, CatalogError> = LCEState.None,
    val combo: ComboSearch? = null,
    val comboModels: LCEState<List<CarModel>, CatalogError> = LCEState.None,
    val canResults: LCEState<List<CarSummary>, CatalogError> = LCEState.None,
    val card: LCEState<CarCard, CatalogError> = LCEState.None,
    val cardUi: CardUi = CardUi(),
    val recent: List<CarSummary> = emptyList(),
    /** Применённый фильтр групп функций; [filterDraft] не `null` — открыт фильтр. */
    val groups: Set<CarFeatureGroup> = emptySet(),
    val filterDraft: Set<CarFeatureGroup>? = null,
    val dialog: VehicleDialog? = null,
    /** Идёт получение данных автомобиля для «Выбрать» из строки. */
    val picking: Boolean = false
) {
    val showTypeChoice: Boolean get() = needsType && deviceType == null
}

/** Постоянные параметры экрана из запроса. */
internal data class VehicleSelectModeUi(
    val pick: Boolean,
    /** Каталог «с конфигурациями»: без поиска по CAN-коду и фильтра функций. */
    val configurationsOnly: Boolean,
    /** Тип устройства выбирается пользователем (чип «Сигнализация ▾» в шапке). */
    val typeSelectable: Boolean,
    val deviceName: String?,
    val firmwareCanCode: String?
) {
    val canSearchAvailable: Boolean get() = !configurationsOnly
    val filterAvailable: Boolean get() = !configurationsOnly
}

internal sealed interface VehicleSelectEffect {
    data class Picked(val car: CarSummary) : VehicleSelectEffect
    data object Exit : VehicleSelectEffect
    data class CanCopied(val code: String) : VehicleSelectEffect
}
