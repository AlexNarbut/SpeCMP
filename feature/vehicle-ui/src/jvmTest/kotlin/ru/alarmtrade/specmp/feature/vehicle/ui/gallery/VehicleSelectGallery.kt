package ru.alarmtrade.specmp.feature.vehicle.ui.gallery

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import java.io.File
import kotlin.test.Test
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.view.dialog.rememberCmpSnackbarHostState
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBus
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarDocument
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeature
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.MountPhoto
import ru.alarmtrade.specmp.feature.vehicle.domain.model.MountPoint
import ru.alarmtrade.specmp.feature.vehicle.domain.model.UserManualRef
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.components.vehicleLayoutOf
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CapabilityKind
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardSource
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardTab
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CardUi
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.CatalogSelection
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.ComboSearch
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.SearchMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleDialog
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectActions
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectContent
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectModeUi
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select.VehicleSelectState

/**
 * Галерея состояний выбора автомобиля: `./gradlew :feature:vehicle-ui:jvmTest --tests '*Gallery*'` →
 * `build/state-gallery/vehicle-select`. Лист на состояние: светлая/тёмная тема × Compact / Medium / Expanded / 1600.
 */
class VehicleSelectGallery {
    private val out = File("build/state-gallery/vehicle-select")

    private val toyota = CarBrand(1, "Toyota", null)
    private val brands = listOf(
        CarBrand(5, "Audi", null),
        CarBrand(6, "BMW", null),
        CarBrand(7, "Hyundai", null),
        CarBrand(8, "Kia", null),
        CarBrand(9, "Lada", null),
        CarBrand(10, "Land Rover", null),
        CarBrand(11, "Mercedes-Benz", null),
        toyota,
        CarBrand(12, "Volkswagen", null),
    )
    private val camry = CarModel(21, "Camry", null, 2017..2023)
    private val models = listOf(
        camry,
        CarModel(22, "Corolla", null, 2018..2022),
        CarModel(23, "Land Cruiser 300", null, 2021..2025),
        CarModel(24, "RAV4", null, 2019..2019),
    )
    private val trims = listOf(CarTrim(31, "2.5 AT"), CarTrim(32, "3.5 AT"), CarTrim(33, "XV70 Hybrid"))
    private val summary = CarSummary(31, "Toyota", "Camry", "2.5 AT", 2017..2023, "2154", null)
    private val recent = listOf(summary, CarSummary(41, "Kia", "K5", "2.5 GDI", 2020..2024, "1032", null))
    private val card = CarCard(
        summary = summary,
        engineBlockByCan = CarCapability.Supported("<p>Блокировка по <b>CAN2</b> через модуль.</p>"),
        keylessBypass = CarCapability.NotSupported(null),
        notes = listOf(
            "<p>Для автозапуска нужен <b>обходчик</b> штатного иммобилайзера. Перед монтажом проверьте версию " +
                "прошивки и наличие штатной охранной системы.</p>",
            "<ul><li>Снимите пломбу</li><li>Подключите CAN-H</li></ul>",
        ),
        features = listOf(
            CarFeature(
                "Двери",
                "Открытие водительской и пассажирских",
                CarFeatureGroup.MAIN,
                listOf(CarBus.CAN1),
                listOf(CarBus.CAN1),
            ),
            CarFeature("Зажигание", null, CarFeatureGroup.MAIN, listOf(CarBus.CAN2, CarBus.LIN), emptyList()),
            CarFeature("Обогрев руля", null, CarFeatureGroup.EXTENDED, emptyList(), listOf(CarBus.CAN_FD1_OR_CAN_FD3)),
            CarFeature(
                "Иммобилайзер",
                null,
                CarFeatureGroup.EXTENDED,
                listOf(CarBus.IMO_KEY1_PLUS_IMO_KEY2),
                listOf(CarBus.BEAN),
            ),
            CarFeature(
                "Заряд батареи",
                null,
                CarFeatureGroup.ELECTRIC,
                listOf(CarBus.CAN3_FD, CarBus.UNKNOWN),
                listOf(CarBus.LIN_IN_OUT),
            ),
            CarFeature("Прицеп", null, CarFeatureGroup.TRAILER, listOf(CarBus.CAN4, CarBus.IMO_KEY2), emptyList()),
        ),
        mountPoints = listOf(
            MountPoint(
                "Замок зажигания",
                "Зажигание",
                "Разъём за панелью",
                listOf(MountPhoto("1", "u1"), MountPhoto("2", "u2")),
            ),
            MountPoint("Блок BCM", "CAN", null, emptyList()),
        ),
        schemes = listOf(CarDocument("Схема подключения", "Основной жгут", "https://x/a.pdf")),
        mountingMaps = listOf(CarDocument("Карта монтажа", null, "https://x/b.pdf")),
        userManuals = listOf(
            UserManualRef(1, "Монтаж без обходчика", "Опыт установщика: где взять CAN и как снять панель"),
        ),
        hasUserManuals = true,
    )

    private fun state(pick: Boolean = false, typeSelectable: Boolean = true) = VehicleSelectState(
        mode = VehicleSelectModeUi(
            pick = pick,
            configurationsOnly = false,
            typeSelectable = typeSelectable,
            deviceName = if (typeSelectable) null else "DX-9X",
            firmwareCanCode = if (typeSelectable) null else "2154",
        ),
        deviceType = VehicleDeviceType.ALARM,
        brands = LCEState.Content(brands),
        recent = recent,
    )

    private fun render(name: String, state: VehicleSelectState) = StateGallery.render(out, name) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            VehicleSelectContent(
                state = state,
                layout = vehicleLayoutOf(maxWidth),
                windowWidth = maxWidth,
                actions = noActions,
                snackbarHostState = rememberCmpSnackbarHostState(),
            )
        }
    }

    @Test
    fun `00 type choice`() = render("00-type", state().copy(deviceType = null))

    @Test
    fun `01 brands with recent`() = render("01-brands", state())

    @Test
    fun `02 brands loading`() = render("02-brands-loading", state().copy(brands = LCEState.Loading))

    @Test
    fun `03 brands error`() = render("03-brands-error", state().copy(brands = LCEState.Error(CatalogError.LoadFailed)))

    @Test
    fun `04 brands offline`() = render(
        "04-brands-offline",
        state().copy(brands = LCEState.Error(CatalogError.Common(ApiError.NoConnection))),
    )

    @Test
    fun `05 search brands`() = render("05-search", state().copy(query = "la"))

    @Test
    fun `06 search combo`() = render(
        "06-combo",
        state().copy(query = "toyota cam", combo = ComboSearch(toyota, "cam"), comboModels = LCEState.Content(models)),
    )

    @Test
    fun `07 search digits`() = render("07-digits", state().copy(query = "2154"))

    @Test
    fun `08 search nothing`() = render("08-nothing", state().copy(query = "zzz"))

    @Test
    fun `09 filter emptied brands`() = render(
        "09-filter-empty",
        state().copy(brands = LCEState.Content(emptyList()), groups = setOf(CarFeatureGroup.TRAILER)),
    )

    @Test
    fun `10 models`() = render(
        "10-models",
        state().copy(selection = CatalogSelection(brand = toyota), models = LCEState.Content(models)),
    )

    @Test
    fun `11 trims pick`() = render(
        "11-trims-pick",
        state(pick = true).copy(
            selection = CatalogSelection(brand = toyota, model = camry),
            models = LCEState.Content(models),
            trims = LCEState.Content(trims),
        ),
    )

    @Test
    fun `12 can enter`() = render("12-can-enter", state().copy(searchMode = SearchMode.CAN, canCode = "21"))

    @Test
    fun `13 can results pick`() = render(
        "13-can-results",
        state(pick = true).copy(searchMode = SearchMode.CAN, canCode = "2154", canResults = LCEState.Content(recent)),
    )

    @Test
    fun `14 can none`() = render(
        "14-can-none",
        state().copy(searchMode = SearchMode.CAN, canCode = "9999", canResults = LCEState.Content(emptyList())),
    )

    @Test
    fun `15 can prefilled from device`() = render(
        "15-can-device",
        state(typeSelectable = false).copy(
            searchMode = SearchMode.CAN,
            canCode = "2154",
            canResults = LCEState.Loading,
        ),
    )

    private fun cardState(ui: CardUi = CardUi(tab = CardTab.FEATURES), pick: Boolean = false) = state(pick = pick).copy(
        selection = CatalogSelection(
            brand = toyota,
            model = camry,
            carId = summary.id,
            cardSource = CardSource.CATALOG,
        ),
        models = LCEState.Content(models),
        trims = LCEState.Content(trims),
        card = LCEState.Content(card),
        cardUi = ui,
    )

    @Test
    fun `20 card features`() = render("20-card-features", cardState())

    @Test
    fun `21 card notes expanded pick`() = render(
        "21-card-notes",
        cardState(ui = CardUi(tab = CardTab.FEATURES, notesExpanded = true), pick = true),
    )

    @Test
    fun `22 card points`() = render("22-card-points", cardState(ui = CardUi(tab = CardTab.MOUNT_POINTS)))

    @Test
    fun `23 card point detail`() =
        render("23-card-point", cardState(ui = CardUi(tab = CardTab.MOUNT_POINTS, point = 0)))

    @Test
    fun `24 card schemes`() = render("24-card-schemes", cardState(ui = CardUi(tab = CardTab.SCHEMES)))

    @Test
    fun `25 card manuals`() = render("25-card-manuals", cardState(ui = CardUi(tab = CardTab.USER_MANUALS)))

    @Test
    fun `26 card filtered`() = render("26-card-filtered", cardState().copy(groups = setOf(CarFeatureGroup.EXTENDED)))

    @Test
    fun `27 card loading`() = render("27-card-loading", cardState().copy(card = LCEState.Loading))

    @Test
    fun `28 card error`() = render("28-card-error", cardState().copy(card = LCEState.Error(CatalogError.LoadFailed)))

    @Test
    fun `29 card without tabs`() = render(
        "29-card-no-tabs",
        cardState().copy(
            card = LCEState.Content(
                card.copy(
                    notes = emptyList(),
                    features = emptyList(),
                    mountPoints = emptyList(),
                    schemes = emptyList(),
                    mountingMaps = emptyList(),
                    userManuals = emptyList(),
                    engineBlockByCan = CarCapability.NoData,
                    keylessBypass = CarCapability.NoData,
                ),
            ),
        ),
    )

    @Test
    fun `30 filter panel`() = render("30-filter", state().copy(filterDraft = setOf(CarFeatureGroup.MAIN)))

    @Test
    fun `31 capability note`() = render(
        "31-note",
        cardState().copy(dialog = VehicleDialog.CapabilityNote(CapabilityKind.ENGINE_BLOCK, card.engineBlockByCan)),
    )

    @Test
    fun `32 can mismatch`() = render(
        "32-mismatch",
        cardState(pick = true).copy(dialog = VehicleDialog.CanMismatch(summary.copy(canCode = "1032"), "2154")),
    )

    private companion object {
        val noCard = CardActions(
            onTabSelect = {},
            onPointOpen = {},
            onPointClose = {},
            onPhotoChange = {},
            onViewerOpen = {},
            onViewerClose = {},
            onNotesToggle = {},
            onGroupToggle = {},
            onCapabilityNote = {},
            onCopyCanCode = {},
            onOpenDocument = {},
            onOpenFirmwareArchive = {},
            onOpenUserManual = {},
            onDialogDismiss = {},
            onMismatchConfirm = {},
        )
        val noActions = VehicleSelectActions(
            onBack = {},
            onTypePick = {},
            onSearchModeChange = {},
            onQueryChange = {},
            onSearchByCan = {},
            onCanCodeChange = {},
            onBrandPick = {},
            onModelPick = {},
            onTrimOpen = {},
            onCanResultOpen = {},
            onRecentOpen = {},
            onRecentRemove = {},
            onCrumb = {},
            onRetry = {},
            onFilterOpen = {},
            onFilterToggle = {},
            onFilterDraftReset = {},
            onFilterApply = {},
            onFilterDismiss = {},
            onFilterClear = {},
            onPick = {},
            onPaneWidthChange = { _, _ -> },
            card = noCard,
        )
    }
}
