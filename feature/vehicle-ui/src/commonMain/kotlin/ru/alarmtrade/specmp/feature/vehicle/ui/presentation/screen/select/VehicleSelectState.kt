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
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest

internal enum class SearchMode { NAME, CAN }

/** Откуда открыта карточка — от этого зависит «назад» на узком окне. */
internal enum class CardSource { CATALOG, CAN, RECENT }

internal enum class CatalogLevel { BRANDS, MODELS, TRIMS, COMBO, CAN, CARD }

internal enum class CardTab { FEATURES, MOUNT_POINTS, SCHEMES, MAPS, USER_MANUALS }

internal enum class VehicleLayout {
    COMPACT,
    MEDIUM,
    EXPANDED,
    ;

    val isWide: Boolean get() = this != COMPACT
}

internal data class CatalogSelection(
    val brand: CarBrand? = null,
    val model: CarModel? = null,
    val carId: Int? = null,
    val cardSource: CardSource = CardSource.CATALOG,
)

/** Поиск «марка модель» на уровне марок: марка распознана, [rest] — фильтр её моделей. */
internal data class ComboSearch(val brand: CarBrand, val rest: String)

internal data class CardUi(
    val tab: CardTab? = null,
    /** Открытая точка подключения (индекс), `null` — список точек. */
    val point: Int? = null,
    val photo: Int = 0,
    val viewerOpen: Boolean = false,
    val notesExpanded: Boolean = false,
    val collapsedGroups: Set<CarFeatureGroup> = emptySet(),
)

internal sealed interface VehicleDialog {
    data class CapabilityNote(val kind: CapabilityKind, val capability: CarCapability) : VehicleDialog

    data class CanMismatch(val car: CarSummary, val firmwareCanCode: String) : VehicleDialog
}

internal enum class CapabilityKind { ENGINE_BLOCK, KEYLESS_BYPASS }

@Immutable
internal data class VehicleSelectState(
    val mode: VehicleSelectModeUi,
    /** Тип устройства не задан запросом и ещё не выбран — экран выбора типа. */
    val deviceType: VehicleDeviceType? = null,
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
    /** CAN-код, по которому единственный результат уже открывался автоматически (не открывать повторно). */
    val autoOpenedCan: String? = null,
    val card: LCEState<CarCard, CatalogError> = LCEState.None,
    val cardUi: CardUi = CardUi(),
    val recent: List<CarSummary> = emptyList(),
    /** Применённый фильтр групп функций; [filterDraft] не `null` — открыт фильтр. */
    val groups: Set<CarFeatureGroup> = emptySet(),
    val filterDraft: Set<CarFeatureGroup>? = null,
    val dialog: VehicleDialog? = null,
    /** Идёт получение данных автомобиля для «Выбрать» из строки. */
    val picking: Boolean = false,
) {
    val showTypeChoice: Boolean get() = mode.typeSelectable && deviceType == null
}

internal fun VehicleSelectRequest.initialState(): VehicleSelectState {
    val configurationsOnly = catalog == VehicleCatalog.WITH_CONFIGURATIONS
    return VehicleSelectState(
        mode = VehicleSelectModeUi(
            pick = mode == VehicleSelectMode.PICK,
            configurationsOnly = configurationsOnly,
            typeSelectable = device == null,
            deviceName = (device as? VehicleDeviceFilter.Device)?.deviceName,
            firmwareCanCode = firmwareCanCode,
        ),
        deviceType = (device as? VehicleDeviceFilter.Type)?.type,
        canCode = firmwareCanCode.takeUnless { configurationsOnly }.orEmpty(),
    )
}

internal data class VehicleSelectModeUi(
    val pick: Boolean,
    /** Каталог «с конфигурациями»: без поиска по CAN-коду и фильтра функций. */
    val configurationsOnly: Boolean,
    val typeSelectable: Boolean,
    val deviceName: String?,
    val firmwareCanCode: String?,
) {
    val canSearchAvailable: Boolean get() = !configurationsOnly
    val filterAvailable: Boolean get() = !configurationsOnly
}

internal enum class CompactStep { BRANDS, MODELS, TRIMS, CAN, CARD }

internal fun VehicleSelectState.compactStep(): CompactStep = when {
    selection.carId != null -> CompactStep.CARD
    searchMode == SearchMode.CAN -> CompactStep.CAN
    selection.model != null -> CompactStep.TRIMS
    selection.brand != null -> CompactStep.MODELS
    else -> CompactStep.BRANDS
}
