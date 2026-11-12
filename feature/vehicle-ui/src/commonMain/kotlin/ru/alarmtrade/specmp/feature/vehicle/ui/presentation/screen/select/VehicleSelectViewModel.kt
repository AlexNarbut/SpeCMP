package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.layout.PaneWidthStore
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesRepository
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest

private const val CAN_CODE_LENGTH = 4

/**
 * Весь путь выбора — в state: поворот и смена ширины окна его не сбрасывают. Каждый уровень грузится своей корутиной
 * (ключ — [CatalogLevel]), новая загрузка уровня отменяет прежнюю.
 */
internal class VehicleSelectViewModel(
    private val request: VehicleSelectRequest,
    private val repository: VehicleCatalogRepository,
    private val recentRepository: RecentVehiclesRepository,
    private val paneWidths: PaneWidthStore,
    private val dispatchers: AppDispatchers,
) : BaseViewModel<VehicleSelectState, VehicleSelectEffect>(
    request.initialState().copy(paneWidthsDp = paneWidths.widthsDp.value.getValueIfSuccess().orEmpty()),
) {

    override suspend fun onStart() {
        when (val recent = withContext(dispatchers.io) { recentRepository.recent(request.catalog) }) {
            is Response.Success -> reduce { copy(recent = recent.value) }
            is Response.Error -> logger.w(recent.exception) { "Recent vehicles read failed" }
        }
        if (!currentState.showTypeChoice) loadBrands()
    }

    fun onTypePick(type: VehicleDeviceType) {
        cancelAll()
        reduce {
            copy(
                deviceType = type,
                selection = CatalogSelection(),
                query = "",
                combo = null,
                models = LCEState.None,
                trims = LCEState.None,
                card = LCEState.None,
                canResults = LCEState.None,
            )
        }
        loadBrands()
        if (currentState.searchMode == SearchMode.CAN) searchCan()
    }

    fun onSearchModeChange(mode: SearchMode) {
        if (mode == currentState.searchMode) return
        cancelLaunched(CatalogLevel.CARD)
        reduce {
            copy(
                autoOpenedCan = null,
                searchMode = mode,
                selection = CatalogSelection(),
                query = "",
                combo = null,
                card = LCEState.None,
            )
        }
        if (mode == SearchMode.CAN) searchCan()
    }

    fun onQueryChange(query: String) {
        val previous = currentState.combo
        reduce { copy(query = query).withDetectedCombo() }
        onComboChanged(previous)
    }

    /** «Искать по CAN-коду» из пустого результата: цифры запроса (до 4) переносятся в поле кода. */
    fun onSearchByCan(digits: String) {
        onSearchModeChange(SearchMode.CAN)
        onCanCodeChange(digits.filter(Char::isDigit).take(CAN_CODE_LENGTH))
    }

    fun onCanCodeChange(code: String) {
        val digits = code.filter(Char::isDigit).take(CAN_CODE_LENGTH)
        if (digits == currentState.canCode && currentState.canResults != LCEState.None) return
        cancelLaunched(CatalogLevel.CARD)
        reduce { copy(autoOpenedCan = null, canCode = digits, selection = CatalogSelection(), card = LCEState.None) }
        searchCan()
    }

    fun onBrandPick(brand: CarBrand) {
        val current = currentState
        val reuse = current.combo?.brand?.id == brand.id && current.comboModels is LCEState.Content
        cancelLaunched(CatalogLevel.TRIMS)
        cancelLaunched(CatalogLevel.CARD)
        reduce {
            copy(
                selection = CatalogSelection(brand = brand),
                query = "",
                combo = null,
                models = if (reuse) current.comboModels else LCEState.None,
                trims = LCEState.None,
                card = LCEState.None,
            )
        }
        if (!reuse) loadModels(brand.id)
    }

    /** Единственная комплектация — сразу карточка; на широком окне повторный тап по раскрытой модели сворачивает её. */
    fun onModelPick(model: CarModel, wide: Boolean) {
        val current = currentState
        if (wide && current.selection.model?.id == model.id && current.selection.carId == null) {
            cancelLaunched(CatalogLevel.TRIMS)
            reduce { copy(selection = selection.copy(model = null), trims = LCEState.None) }
            return
        }
        val brand = current.selection.brand ?: current.combo?.brand
        val models = if (current.selection.brand == null) current.comboModels else current.models
        cancelLaunched(CatalogLevel.CARD)
        reduce {
            copy(
                selection = CatalogSelection(brand = brand, model = model),
                models = models,
                query = "",
                combo = null,
                trims = LCEState.Loading,
                card = LCEState.None,
            )
        }
        loadTrims(model.id, openSingle = true)
    }

    fun onCrumb(target: CrumbTarget) {
        cancelLaunched(CatalogLevel.CARD)
        reduce { withCrumb(target) }
    }

    fun onTrimOpen(carId: Int) {
        openCard(carId, CardSource.CATALOG)
    }

    fun onCanResultOpen(car: CarSummary) {
        openCard(car.id, CardSource.CAN)
    }

    fun onRecentOpen(car: CarSummary) {
        openCard(car.id, CardSource.RECENT)
    }

    fun onPaneWidthChange(key: String, widthDp: Int) {
        reduce { copy(paneWidthsDp = paneWidthsDp + (key to widthDp)) }
        intent {
            withContext(dispatchers.io) { paneWidths.setWidthDp(key, widthDp) }
                .onError { logger.w(it) { "Pane width was not saved" } }
        }
    }

    fun onRecentRemove(car: CarSummary) {
        updateRecent { recentRepository.remove(request.catalog, car.id) }
    }

    fun onRetry(level: CatalogLevel) {
        val s = currentState
        when (level) {
            CatalogLevel.BRANDS -> loadBrands()
            CatalogLevel.MODELS -> s.selection.brand?.let { loadModels(it.id) }
            CatalogLevel.TRIMS -> s.selection.model?.let { loadTrims(it.id, openSingle = true) }
            CatalogLevel.COMBO -> s.combo?.let { loadComboModels(it.brand.id) }
            CatalogLevel.CAN -> searchCan()
            CatalogLevel.CARD -> s.selection.carId?.let { loadCard(it) }
        }
    }

    fun onFilterOpen() {
        reduce { copy(filterDraft = groups) }
    }

    fun onFilterToggle(group: CarFeatureGroup) {
        reduce {
            val draft = filterDraft ?: return@reduce this
            copy(filterDraft = if (group in draft) draft - group else draft + group)
        }
    }

    fun onFilterDraftReset() {
        reduce { copy(filterDraft = filterDraft?.let { emptySet() }) }
    }

    fun onFilterDismiss() {
        reduce { copy(filterDraft = null) }
    }

    fun onFilterApply() {
        applyGroups(currentState.filterDraft ?: return)
    }

    fun onFilterClear() {
        applyGroups(emptySet())
    }

    fun onTabSelect(tab: CardTab) {
        updateCardUi { copy(tab = tab, point = null, photo = 0) }
    }

    fun onPointOpen(index: Int) {
        updateCardUi { copy(point = index, photo = 0) }
    }

    fun onPointClose() {
        updateCardUi { copy(point = null, photo = 0) }
    }

    fun onPhotoChange(index: Int) {
        updateCardUi { copy(photo = index) }
    }

    fun onViewerOpen() {
        updateCardUi { copy(viewerOpen = true) }
    }

    fun onViewerClose() {
        updateCardUi { copy(viewerOpen = false) }
    }

    fun onNotesToggle() {
        updateCardUi { copy(notesExpanded = !notesExpanded) }
    }

    fun onGroupToggle(group: CarFeatureGroup) {
        updateCardUi {
            copy(collapsedGroups = if (group in collapsedGroups) collapsedGroups - group else collapsedGroups + group)
        }
    }

    fun onCapabilityNote(kind: CapabilityKind) {
        val card = currentState.card.content ?: return
        val capability = when (kind) {
            CapabilityKind.ENGINE_BLOCK -> card.engineBlockByCan
            CapabilityKind.KEYLESS_BYPASS -> card.keylessBypass
        }
        reduce { copy(dialog = VehicleDialog.CapabilityNote(kind, capability)) }
    }

    fun onCopyCanCode() {
        val code = currentState.card.content?.summary?.canCode ?: return
        postSideEffect(VehicleSelectEffect.CanCopied(code))
    }

    fun onDialogDismiss() {
        reduce { copy(dialog = null) }
    }

    fun onPick(carId: Int) {
        val current = currentState
        if (!current.mode.pick || current.picking) return
        val known = current.card.content?.summary?.takeIf { it.id == carId }
            ?: current.canResults.content?.firstOrNull { it.id == carId }
        if (known != null) {
            choose(known)
            return
        }
        reduce { copy(picking = true) }
        intent {
            val response = withContext(dispatchers.io) { repository.summary(carId) }
            reduce { copy(picking = false) }
            when (response) {
                is Response.Success -> choose(response.value)

                is Response.Error -> postSideEffect(
                    VehicleSelectEffect.PickFailed(CatalogError.from(response.exception)),
                )
            }
        }
    }

    fun onMismatchConfirm() {
        val dialog = currentState.dialog as? VehicleDialog.CanMismatch ?: return
        reduce { copy(dialog = null) }
        finishPick(dialog.car)
    }

    fun onBack(layout: VehicleLayout) {
        val s = currentState
        val pointOpen = s.selection.carId != null && s.cardUi.tab == CardTab.MOUNT_POINTS && s.cardUi.point != null
        when {
            s.dialog != null -> onDialogDismiss()
            s.filterDraft != null -> onFilterDismiss()
            s.cardUi.viewerOpen -> onViewerClose()
            pointOpen -> onPointClose()
            s.selection.carId != null && layout != VehicleLayout.EXPANDED -> closeCard(layout)
            else -> backFromCatalog(layout)
        }
    }

    private fun backFromCatalog(layout: VehicleLayout) {
        val s = currentState
        when {
            s.catalogStepBack(layout) != null -> reduce { catalogStepBack(layout) ?: this }
            s.mode.typeSelectable && s.deviceType != null -> resetType()
            else -> postSideEffect(VehicleSelectEffect.Exit)
        }
    }

    private fun closeCard(layout: VehicleLayout) {
        cancelLaunched(CatalogLevel.CARD)
        reduce { withCardClosed(layout) }
    }

    private fun resetType() {
        cancelAll()
        reduce { withTypeReset() }
    }

    private fun choose(car: CarSummary) {
        val firmwareCode = request.firmwareCanCode
        if (request.warnOnCanMismatch && firmwareCode != null && car.canCode != firmwareCode) {
            reduce { copy(dialog = VehicleDialog.CanMismatch(car, firmwareCode)) }
        } else {
            finishPick(car)
        }
    }

    private fun finishPick(car: CarSummary) {
        rememberRecent(car)
        postSideEffect(VehicleSelectEffect.Picked(car))
    }

    private fun rememberRecent(car: CarSummary) {
        updateRecent { recentRepository.push(request.catalog, car) }
    }

    private fun updateRecent(change: suspend () -> Response<List<CarSummary>>) {
        intent {
            when (val recent = withContext(dispatchers.io) { change() }) {
                is Response.Success -> reduce { copy(recent = recent.value) }
                is Response.Error -> logger.w(recent.exception) { "Recent vehicles update failed" }
            }
        }
    }

    private fun openCard(carId: Int, source: CardSource) {
        reduce { copy(selection = selection.copy(carId = carId, cardSource = source), cardUi = CardUi()) }
        loadCard(carId)
    }

    private fun applyGroups(groups: Set<CarFeatureGroup>) {
        reduce { copy(groups = groups, filterDraft = null) }
        val s = currentState
        loadBrands()
        s.selection.brand?.let { loadModels(it.id) }
        s.selection.model?.let { loadTrims(it.id, openSingle = false) }
        s.combo?.let { loadComboModels(it.brand.id) }
        if (s.searchMode == SearchMode.CAN) searchCan()
        s.selection.carId?.let { loadCard(it) }
    }

    /** Комбо-поиск уже пересчитан в state: пропал — отменить загрузку его моделей, сменил марку — загрузить. */
    private fun onComboChanged(previous: ComboSearch?) {
        val s = currentState
        if (s.searchMode != SearchMode.NAME || s.selection.brand != null) return
        val combo = s.combo
        when {
            combo == null -> cancelLaunched(CatalogLevel.COMBO)
            combo.brand.id != previous?.brand?.id -> loadComboModels(combo.brand.id)
        }
    }

    private fun catalogQuery(): CatalogQuery {
        val s = currentState
        val device = request.device ?: s.deviceType?.let { VehicleDeviceFilter.Type(it) }
        return CatalogQuery(request.catalog, device, if (s.mode.configurationsOnly) emptySet() else s.groups)
    }

    private fun loadBrands() = load(
        level = CatalogLevel.BRANDS,
        set = { load -> copy(brands = load) },
        fetch = { repository.brands(catalogQuery()) },
        onReady = {
            val previous = currentState.combo
            reduce { withDetectedCombo() }
            onComboChanged(previous)
        },
    )

    private fun loadModels(brandId: Int) = load(
        level = CatalogLevel.MODELS,
        set = { load -> copy(models = load) },
        fetch = { repository.models(catalogQuery(), brandId) },
    )

    private fun loadComboModels(brandId: Int) = load(
        level = CatalogLevel.COMBO,
        set = { load -> copy(comboModels = load) },
        fetch = { repository.models(catalogQuery(), brandId) },
    )

    private fun loadTrims(modelId: Int, openSingle: Boolean) = load(
        level = CatalogLevel.TRIMS,
        set = { load -> copy(trims = load) },
        fetch = { repository.trims(catalogQuery(), modelId) },
        onReady = { trims ->
            val selection = currentState.selection
            val stillOnModel = selection.model?.id == modelId && selection.carId == null
            if (openSingle && trims.size == 1 && stillOnModel) {
                openCard(trims.single().id, CardSource.CATALOG)
            }
        },
    )

    private fun searchCan() {
        val s = currentState
        if (s.searchMode != SearchMode.CAN || s.canCode.length != CAN_CODE_LENGTH || s.showTypeChoice) {
            cancelLaunched(CatalogLevel.CAN)
            reduce { copy(canResults = LCEState.None) }
            return
        }
        val code = s.canCode
        load(
            level = CatalogLevel.CAN,
            set = { load -> copy(canResults = load) },
            fetch = { repository.findByCanCode(code, catalogQuery().device, catalogQuery().groups) },
            onReady = { cars ->
                val current = currentState
                if (cars.size == 1 && current.autoOpenedCan != code && current.canCode == code) {
                    reduce { copy(autoOpenedCan = code) }
                    openCard(cars.single().id, CardSource.CAN)
                }
            },
        )
    }

    private fun loadCard(carId: Int) = load(
        level = CatalogLevel.CARD,
        set = { load -> copy(card = load) },
        fetch = { repository.card(carId, catalogQuery().groups) },
        onReady = { card ->
            rememberRecent(card.summary)
            reduce { copy(cardUi = cardUi.copy(tab = cardUi.tab ?: card.firstTab())) }
        },
    )

    private fun <T> load(
        level: CatalogLevel,
        set: VehicleSelectState.(LCEState<T, CatalogError>) -> VehicleSelectState,
        fetch: suspend () -> Response<T>,
        onReady: (T) -> Unit = {},
    ) {
        reduce { set(this, LCEState.Loading) }
        launchReplacing(level) {
            val response = withContext(dispatchers.io) { fetch() }
            reduce { set(this, response.asLCEState(CatalogError::from)) }
            response.getValueIfSuccess()?.let(onReady)
        }
    }

    private fun cancelAll() {
        CatalogLevel.entries.forEach(::cancelLaunched)
    }

    private fun updateCardUi(transform: CardUi.() -> CardUi) {
        reduce { copy(cardUi = cardUi.transform()) }
    }
}
