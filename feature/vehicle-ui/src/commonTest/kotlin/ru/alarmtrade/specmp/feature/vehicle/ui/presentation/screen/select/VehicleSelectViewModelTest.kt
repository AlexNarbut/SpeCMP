package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCapability
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeature
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarTrim
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesStore
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest

class VehicleSelectViewModelTest {

    // Unconfined: state — stateIn поверх _state, значение доходит сразу после интента (без runCurrent в каждом шаге).
    private val dispatcher = UnconfinedTestDispatcher()
    private val repository = FakeCatalog()
    private val recent = FakeRecent()
    private val viewModels = mutableListOf<VehicleSelectViewModel>()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.viewModel(
        mode: VehicleSelectMode = VehicleSelectMode.VIEW,
        catalog: VehicleCatalog = VehicleCatalog.ALL,
        device: VehicleDeviceFilter? = VehicleDeviceFilter.Device("DX-9X"),
        firmwareCanCode: String? = null,
        warnOnCanMismatch: Boolean = false
    ): Pair<VehicleSelectViewModel, MutableList<VehicleSelectEffect>> {
        val vm = VehicleSelectViewModel(
            request = VehicleSelectRequest(mode, catalog, device, firmwareCanCode, warnOnCanMismatch),
            repository = repository,
            recentStore = recent,
            dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        ).also { viewModels += it }
        val effects = mutableListOf<VehicleSelectEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        // Как экран: state стартует работу по подписке (stateIn).
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        advanceUntilIdle()
        return vm to effects
    }

    private val VehicleSelectViewModel.s get() = state.value

    @Test
    fun `without device the type is chosen first and filters the catalog`() = test {
        val (vm, _) = viewModel(device = null)
        assertTrue(vm.s.showTypeChoice)
        assertEquals(0, repository.brandQueries.size)

        vm.onTypePick(VehicleDeviceType.MICROBOT)
        advanceUntilIdle()

        assertEquals(VehicleDeviceFilter.Type(VehicleDeviceType.MICROBOT), repository.brandQueries.last().device)
        assertEquals(FakeCatalog.brands, vm.s.brands.content)
    }

    @Test
    fun `brand loads models, model with one trim opens its card`() = test {
        val (vm, _) = viewModel()
        vm.onBrandPick(FakeCatalog.toyota)
        advanceUntilIdle()
        assertEquals(FakeCatalog.models, vm.s.models.content)

        vm.onModelPick(FakeCatalog.corolla, wide = false)
        advanceUntilIdle()

        assertEquals(FakeCatalog.corollaTrim.id, vm.s.selection.carId)
        assertIs<LCEState.Content<CarCard, CatalogError>>(vm.s.card)
        assertEquals(CardTab.FEATURES, vm.s.cardUi.tab)
    }

    @Test
    fun `model with several trims shows them and back returns step by step`() = test {
        val (vm, effects) = viewModel()
        vm.onBrandPick(FakeCatalog.toyota)
        advanceUntilIdle()
        vm.onModelPick(FakeCatalog.camry, wide = false)
        advanceUntilIdle()
        assertEquals(FakeCatalog.camryTrims, vm.s.trims.content)
        assertNull(vm.s.selection.carId)

        vm.onTrimOpen(32)
        advanceUntilIdle()
        vm.onBack(VehicleLayout.COMPACT)
        assertNull(vm.s.selection.carId)
        assertEquals(FakeCatalog.camry, vm.s.selection.model)
        vm.onBack(VehicleLayout.COMPACT)
        assertNull(vm.s.selection.model)
        vm.onBack(VehicleLayout.COMPACT)
        assertNull(vm.s.selection.brand)
        vm.onBack(VehicleLayout.COMPACT)
        runCurrent()
        assertEquals(listOf<VehicleSelectEffect>(VehicleSelectEffect.Exit), effects)
    }

    @Test
    fun `back from a single-trim card on compact skips the trims step`() = test {
        val (vm, _) = viewModel()
        vm.onBrandPick(FakeCatalog.toyota)
        advanceUntilIdle()
        vm.onModelPick(FakeCatalog.corolla, wide = false)
        advanceUntilIdle()

        vm.onBack(VehicleLayout.COMPACT)

        assertNull(vm.s.selection.model)
        assertEquals(FakeCatalog.toyota, vm.s.selection.brand)
    }

    @Test
    fun `on wide window tapping the open model collapses it`() = test {
        val (vm, _) = viewModel()
        vm.onBrandPick(FakeCatalog.toyota)
        advanceUntilIdle()
        vm.onModelPick(FakeCatalog.camry, wide = true)
        advanceUntilIdle()

        vm.onModelPick(FakeCatalog.camry, wide = true)

        assertNull(vm.s.selection.model)
    }

    @Test
    fun `level error keeps the selection and retry reloads only that level`() = test {
        repository.failModels = true
        val (vm, _) = viewModel()
        vm.onBrandPick(FakeCatalog.toyota)
        advanceUntilIdle()
        assertEquals(LCEState.Error(CatalogError.LoadFailed), vm.s.models)
        assertEquals(FakeCatalog.toyota, vm.s.selection.brand)

        repository.failModels = false
        val brandLoads = repository.brandQueries.size
        vm.onRetry(CatalogLevel.MODELS)
        advanceUntilIdle()

        assertEquals(FakeCatalog.models, vm.s.models.content)
        assertEquals(brandLoads, repository.brandQueries.size)
    }

    @Test
    fun `connection failure is kept as a network error`() = test {
        repository.failure = AppException(ApiError.NoConnection, IllegalStateException("offline"))
        repository.failBrands = true
        val (vm, _) = viewModel()
        assertEquals(LCEState.Error(CatalogError.Common(ApiError.NoConnection)), vm.s.brands)
    }

    @Test
    fun `filter draft is separate and apply reloads brands with groups`() = test {
        val (vm, _) = viewModel()
        vm.onFilterOpen()
        vm.onFilterToggle(CarFeatureGroup.TRAILER)
        vm.onFilterDismiss()
        assertTrue(vm.s.groups.isEmpty())

        vm.onFilterOpen()
        vm.onFilterToggle(CarFeatureGroup.TRAILER)
        vm.onFilterApply()
        advanceUntilIdle()

        assertEquals(setOf(CarFeatureGroup.TRAILER), vm.s.groups)
        assertEquals(setOf(CarFeatureGroup.TRAILER), repository.brandQueries.last().groups)
        assertNull(vm.s.filterDraft)

        vm.onFilterClear()
        advanceUntilIdle()
        assertTrue(repository.brandQueries.last().groups.isEmpty())
    }

    @Test
    fun `brand and model query loads the brand models`() = test {
        val (vm, _) = viewModel()
        vm.onQueryChange("toyota cam")
        advanceUntilIdle()

        assertEquals(ComboSearch(FakeCatalog.toyota, "cam"), vm.s.combo)
        assertEquals(FakeCatalog.models, vm.s.comboModels.content)

        vm.onModelPick(FakeCatalog.camry, wide = false)
        advanceUntilIdle()
        assertEquals(FakeCatalog.toyota, vm.s.selection.brand)
        assertEquals(FakeCatalog.camry, vm.s.selection.model)
        assertNull(vm.s.combo)
    }

    @Test
    fun `digits from the name search move to the CAN search`() = test {
        val (vm, _) = viewModel()
        vm.onSearchByCan("21549")
        advanceUntilIdle()

        assertEquals(SearchMode.CAN, vm.s.searchMode)
        assertEquals("2154", vm.s.canCode)
        assertEquals(listOf("2154"), repository.canQueries)
    }

    @Test
    fun `CAN search waits for 4 digits and opens a single result once`() = test {
        val (vm, _) = viewModel()
        vm.onSearchModeChange(SearchMode.CAN)
        vm.onCanCodeChange("215")
        advanceUntilIdle()
        assertTrue(repository.canQueries.isEmpty())

        vm.onCanCodeChange("2154")
        advanceUntilIdle()
        assertEquals(FakeCatalog.camrySummary.id, vm.s.selection.carId)

        vm.onBack(VehicleLayout.COMPACT)
        assertNull(vm.s.selection.carId)
        assertEquals(listOf(FakeCatalog.camrySummary), vm.s.canResults.content)

        vm.onCanCodeChange("1032")
        advanceUntilIdle()
        assertNull(vm.s.selection.carId)
        assertEquals(2, vm.s.canResults.content?.size)
    }

    @Test
    fun `device firmware CAN code is prefilled and searched in CAN mode`() = test {
        val (vm, _) = viewModel(firmwareCanCode = "2154")
        assertEquals("2154", vm.s.canCode)

        vm.onSearchModeChange(SearchMode.CAN)
        advanceUntilIdle()

        assertEquals(listOf("2154"), repository.canQueries)
    }

    @Test
    fun `pick from a trim row loads the summary and remembers the car`() = test {
        val (vm, effects) = viewModel(mode = VehicleSelectMode.PICK)
        vm.onPick(FakeCatalog.camrySummary.id)
        advanceUntilIdle()
        runCurrent()

        assertEquals(listOf<VehicleSelectEffect>(VehicleSelectEffect.Picked(FakeCatalog.camrySummary)), effects)
        assertEquals(listOf(FakeCatalog.camrySummary), recent.recent(VehicleCatalog.ALL))
    }

    @Test
    fun `view mode ignores pick`() = test {
        val (vm, effects) = viewModel()
        vm.onPick(FakeCatalog.camrySummary.id)
        advanceUntilIdle()
        runCurrent()
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `CAN mismatch asks before picking`() = test {
        val (vm, effects) = viewModel(mode = VehicleSelectMode.PICK, firmwareCanCode = "9999", warnOnCanMismatch = true)
        vm.onPick(FakeCatalog.camrySummary.id)
        advanceUntilIdle()
        assertIs<VehicleDialog.CanMismatch>(vm.s.dialog)
        assertTrue(effects.isEmpty())

        vm.onMismatchConfirm()
        runCurrent()

        assertEquals(listOf<VehicleSelectEffect>(VehicleSelectEffect.Picked(FakeCatalog.camrySummary)), effects)
    }

    @Test
    fun `opening a card remembers it and recent opens from the top`() = test {
        val (vm, _) = viewModel()
        vm.onRecentOpen(FakeCatalog.camrySummary)
        advanceUntilIdle()
        assertEquals(listOf(FakeCatalog.camrySummary), vm.s.recent)

        vm.onBack(VehicleLayout.MEDIUM)
        assertEquals(CatalogSelection(), vm.s.selection)
    }

    @Test
    fun `removing a recent car drops it from the list`() = test {
        val (vm, _) = viewModel()
        vm.onRecentOpen(FakeCatalog.camrySummary)
        advanceUntilIdle()

        vm.onRecentRemove(FakeCatalog.camrySummary)
        advanceUntilIdle()

        assertEquals(emptyList(), vm.s.recent)
    }

    @Test
    fun `on expanded window back with an open card leaves the screen`() = test {
        val (vm, effects) = viewModel()
        vm.onRecentOpen(FakeCatalog.camrySummary)
        advanceUntilIdle()

        vm.onBack(VehicleLayout.EXPANDED)
        runCurrent()

        assertEquals(listOf<VehicleSelectEffect>(VehicleSelectEffect.Exit), effects)
    }

    @Test
    fun `back from a chosen type returns to the type choice`() = test {
        val (vm, effects) = viewModel(device = null)
        vm.onTypePick(VehicleDeviceType.ALARM)
        advanceUntilIdle()

        vm.onBack(VehicleLayout.COMPACT)

        assertTrue(vm.s.showTypeChoice)
        assertTrue(effects.isEmpty())
    }

    @Test
    fun `configurations catalog passes no groups`() = test {
        val (vm, _) = viewModel(catalog = VehicleCatalog.WITH_CONFIGURATIONS)
        assertTrue(!vm.s.mode.canSearchAvailable)
        vm.onFilterOpen()
        vm.onFilterToggle(CarFeatureGroup.MAIN)
        vm.onFilterApply()
        advanceUntilIdle()
        assertTrue(repository.brandQueries.last().groups.isEmpty())
        assertEquals(VehicleCatalog.WITH_CONFIGURATIONS, repository.brandQueries.last().catalog)
    }
}

private class FakeCatalog : VehicleCatalogRepository {
    val brandQueries = mutableListOf<CatalogQuery>()
    val canQueries = mutableListOf<String>()
    var failBrands = false
    var failModels = false
    var failure: Throwable = IllegalStateException("server")

    override suspend fun brands(query: CatalogQuery): Response<List<CarBrand>> {
        brandQueries += query
        return if (failBrands) error() else Response.Success(brands)
    }

    override suspend fun models(query: CatalogQuery, brandId: Int): Response<List<CarModel>> =
        if (failModels) error() else Response.Success(models)

    override suspend fun trims(query: CatalogQuery, modelId: Int): Response<List<CarTrim>> =
        Response.Success(if (modelId == camry.id) camryTrims else listOf(corollaTrim))

    override suspend fun card(carId: Int, groups: Set<CarFeatureGroup>): Response<CarCard> =
        Response.Success(card(summaryOf(carId)))

    override suspend fun findByCanCode(
        canCode: String,
        device: VehicleDeviceFilter?,
        groups: Set<CarFeatureGroup>
    ): Response<List<CarSummary>> {
        canQueries += canCode
        return Response.Success(
            when (canCode) {
                "2154" -> listOf(camrySummary)
                "1032" -> listOf(camrySummary.copy(id = 41), camrySummary.copy(id = 42))
                else -> emptyList()
            }
        )
    }

    override suspend fun summary(carId: Int): Response<CarSummary> = Response.Success(summaryOf(carId))

    private fun summaryOf(carId: Int) = camrySummary.copy(id = carId)

    private fun error() = Response.Error(failure)

    companion object {
        val toyota = CarBrand(1, "Toyota", null)
        val brands = listOf(CarBrand(2, "Kia", null), toyota)
        val camry = CarModel(10, "Camry", null, 2017..2023)
        val corolla = CarModel(11, "Corolla", null, 2018..2022)
        val models = listOf(camry, corolla)
        val camryTrims = listOf(CarTrim(31, "2.5 AT"), CarTrim(32, "3.5 AT"))
        val corollaTrim = CarTrim(35, "1.6 CVT")
        val camrySummary = CarSummary(31, "Toyota", "Camry", "2.5 AT", 2017..2023, "2154", null)

        fun card(summary: CarSummary) = CarCard(
            summary = summary,
            engineBlockByCan = CarCapability.NoData,
            keylessBypass = CarCapability.NoData,
            notes = emptyList(),
            features = listOf(
                CarFeature("Двери", null, CarFeatureGroup.MAIN, emptyList(), emptyList())
            ),
            mountPoints = emptyList(),
            schemes = emptyList(),
            mountingMaps = emptyList(),
            userManuals = emptyList(),
            hasUserManuals = false
        )
    }
}

private class FakeRecent : RecentVehiclesStore {
    private val items = mutableMapOf<VehicleCatalog, List<CarSummary>>()

    override fun recent(catalog: VehicleCatalog): List<CarSummary> = items[catalog].orEmpty()

    override fun push(catalog: VehicleCatalog, car: CarSummary) {
        items[catalog] = (listOf(car) + recent(catalog).filter { it.id != car.id }).take(RecentVehiclesStore.MAX_RECENT)
    }

    override fun remove(catalog: VehicleCatalog, carId: Int) {
        items[catalog] = recent(catalog).filter { it.id != carId }
    }
}
