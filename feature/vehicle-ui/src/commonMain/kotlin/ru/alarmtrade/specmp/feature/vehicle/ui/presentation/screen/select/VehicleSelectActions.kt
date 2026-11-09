package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType

@Immutable
internal class VehicleSelectActions(
    val onBack: () -> Unit,
    val onTypePick: (VehicleDeviceType) -> Unit,
    val onSearchModeChange: (SearchMode) -> Unit,
    val onQueryChange: (String) -> Unit,
    val onSearchByCan: (String) -> Unit,
    val onCanCodeChange: (String) -> Unit,
    val onBrandPick: (CarBrand) -> Unit,
    val onModelPick: (CarModel) -> Unit,
    val onTrimOpen: (Int) -> Unit,
    val onCanResultOpen: (CarSummary) -> Unit,
    val onRecentOpen: (CarSummary) -> Unit,
    val onRecentRemove: (CarSummary) -> Unit,
    val onCrumb: (CrumbTarget) -> Unit,
    val onRetry: (CatalogLevel) -> Unit,
    val onFilterOpen: () -> Unit,
    val onFilterToggle: (CarFeatureGroup) -> Unit,
    val onFilterDraftReset: () -> Unit,
    val onFilterApply: () -> Unit,
    val onFilterDismiss: () -> Unit,
    val onFilterClear: () -> Unit,
    val onPick: (Int) -> Unit,
    val card: CardActions,
)

@Immutable
internal class CardActions(
    val onTabSelect: (CardTab) -> Unit,
    val onPointOpen: (Int) -> Unit,
    val onPointClose: () -> Unit,
    val onPhotoChange: (Int) -> Unit,
    val onViewerOpen: () -> Unit,
    val onViewerClose: () -> Unit,
    val onNotesToggle: () -> Unit,
    val onGroupToggle: (CarFeatureGroup) -> Unit,
    val onCapabilityNote: (CapabilityKind) -> Unit,
    val onCopyCanCode: () -> Unit,
    val onOpenDocument: (String) -> Unit,
    val onOpenFirmwareArchive: ((String) -> Unit)?,
    val onOpenUserManual: ((Int) -> Unit)?,
    val onDialogDismiss: () -> Unit,
    val onMismatchConfirm: () -> Unit,
)

internal enum class CrumbTarget { TOP, BRAND, MODEL }
