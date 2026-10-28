package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.error.RequestError
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarBrand
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarCard
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarFeatureGroup
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarModel
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CatalogQuery
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.RecentVehiclesStore
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.detectCombo
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectMode
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation.VehicleSelectRequest

private const val CAN_CODE_LENGTH = 4

/**
 * Выбор автомобиля: каталог «марка → модель → комплектация → карточка», поиск по названию (локально по уровню)
 * и по CAN-коду, фильтр групп функций (на сервере), недавние.
 *
 * Весь путь выбора — в state, поэтому поворот и смена ширины окна его не сбрасывают; шаг узкой раскладки
 * выводится из state, «назад» решает [onBack] по раскладке. Каждый уровень грузится своей корутиной, новая
 * загрузка уровня отменяет прежнюю; ошибка уровня не сбрасывает выбор (повтор — [onRetry]).
 */
@Suppress("TooManyFunctions") // экран с одним state: каждое действие пользователя — свой метод
internal class VehicleSelectViewModel(
    private val request: VehicleSelectRequest,
    private val repository: VehicleCatalogRepository,
    private val recentStore: RecentVehiclesStore,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val recentLock = Mutex()

    private val configurationsOnly = request.catalog == VehicleCatalog.WITH_CONFIGURATIONS

    private val _state = MutableStateFlow(
        VehicleSelectState(
            mode = VehicleSelectModeUi(
                pick = request.mode == VehicleSelectMode.PICK,
                configurationsOnly = configurationsOnly,
                typeSelectable = request.device == null,
                deviceName = (request.device as? VehicleDeviceFilter.Device)?.deviceName,
                firmwareCanCode = request.firmwareCanCode
            ),
            deviceType = (request.device as? VehicleDeviceFilter.Type)?.type,
            needsType = request.device == null,
            canCode = request.firmwareCanCode.takeUnless { configurationsOnly }.orEmpty(),
            recent = recentStore.recent(request.catalog)
        )
    )
    val state: StateFlow<VehicleSelectState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<VehicleSelectEffect>()
    val effect: SharedFlow<VehicleSelectEffect> = _effect.asSharedFlow()

    private val jobs = mutableMapOf<CatalogLevel, Job>()

    /** CAN-код, по которому единственный результат уже открывался автоматически (не открывать повторно). */
    private var autoOpenedCode: String? = null

    init {
        if (!_state.value.showTypeChoice) loadBrands()
    }

    // ── Тип устройства ─────────────────────────────────────────

    fun onTypePick(type: VehicleDeviceType) {
        cancelAll()
        _state.update {
            it.copy(
                deviceType = type,
                selection = CatalogSelection(),
                query = "",
                combo = null,
                models = Load.Idle,
                trims = Load.Idle,
                card = Load.Idle,
                canResults = Load.Idle
            )
        }
        loadBrands()
        if (_state.value.searchMode == SearchMode.CAN) searchCan()
    }

    // ── Поиск ──────────────────────────────────────────────────

    fun onSearchModeChange(mode: SearchMode) {
        if (mode == _state.value.searchMode) return
        autoOpenedCode = null
        cancel(CatalogLevel.CARD)
        _state.update {
            it.copy(searchMode = mode, selection = CatalogSelection(), query = "", combo = null, card = Load.Idle)
        }
        if (mode == SearchMode.CAN) searchCan()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        updateCombo()
    }

    /** «Искать по CAN-коду» из пустого результата: цифры запроса (до 4) переносятся в поле кода. */
    fun onSearchByCan(digits: String) {
        onSearchModeChange(SearchMode.CAN)
        onCanCodeChange(digits.filter(Char::isDigit).take(CAN_CODE_LENGTH))
    }

    fun onCanCodeChange(code: String) {
        val digits = code.filter(Char::isDigit).take(CAN_CODE_LENGTH)
        if (digits == _state.value.canCode && _state.value.canResults != Load.Idle) return
        autoOpenedCode = null
        cancel(CatalogLevel.CARD)
        _state.update { it.copy(canCode = digits, selection = CatalogSelection(), card = Load.Idle) }
        searchCan()
    }

    // ── Каталог ────────────────────────────────────────────────

    fun onBrandPick(brand: CarBrand) {
        val current = _state.value
        val reuse = current.combo?.brand?.id == brand.id && current.comboModels is Load.Ready
        cancel(CatalogLevel.TRIMS)
        cancel(CatalogLevel.CARD)
        _state.update {
            it.copy(
                selection = CatalogSelection(brand = brand),
                query = "",
                combo = null,
                models = if (reuse) current.comboModels else Load.Idle,
                trims = Load.Idle,
                card = Load.Idle
            )
        }
        if (!reuse) loadModels(brand.id)
    }

    /**
     * Модель: грузятся комплектации, единственная — сразу карточка. На широком окне повторный тап по раскрытой
     * модели сворачивает её ([wide]).
     */
    fun onModelPick(model: CarModel, wide: Boolean) {
        val current = _state.value
        if (wide && current.selection.model?.id == model.id && current.selection.carId == null) {
            cancel(CatalogLevel.TRIMS)
            _state.update { it.copy(selection = it.selection.copy(model = null), trims = Load.Idle) }
            return
        }
        val brand = current.selection.brand ?: current.combo?.brand
        val models = if (current.selection.brand == null) current.comboModels else current.models
        cancel(CatalogLevel.CARD)
        _state.update {
            it.copy(
                selection = CatalogSelection(brand = brand, model = model),
                models = models,
                query = "",
                combo = null,
                trims = Load.Loading,
                card = Load.Idle
            )
        }
        loadTrims(model.id, openSingle = true)
    }

    /** Крошки: вернуться к маркам (или к результатам CAN-поиска), к моделям марки, к комплектациям модели. */
    fun onCrumb(target: CrumbTarget) {
        cancel(CatalogLevel.CARD)
        val s = _state.value
        val selection = when (target) {
            CrumbTarget.TOP -> CatalogSelection()
            CrumbTarget.BRAND -> CatalogSelection(brand = s.selection.brand)
            CrumbTarget.MODEL -> s.selection.copy(carId = null, cardSource = CardSource.CATALOG)
        }
        _state.update {
            it.copy(
                selection = selection,
                query = "",
                card = Load.Idle,
                cardUi = CardUi(),
                trims = if (selection.model == null) Load.Idle else it.trims
            )
        }
    }

    fun onTrimOpen(carId: Int) = openCard(carId, CardSource.CATALOG)

    fun onCanResultOpen(car: CarSummary) = openCard(car.id, CardSource.CAN)

    fun onRecentOpen(car: CarSummary) = openCard(car.id, CardSource.RECENT)

    /** «✕» у недавнего автомобиля: убрать из списка (из каталога он никуда не девается). */
    fun onRecentRemove(car: CarSummary) {
        updateRecent { recentStore.remove(request.catalog, car.id) }
    }

    fun onRetry(level: CatalogLevel) {
        val s = _state.value
        when (level) {
            CatalogLevel.BRANDS -> loadBrands()
            CatalogLevel.MODELS -> s.selection.brand?.let { loadModels(it.id) }
            CatalogLevel.TRIMS -> s.selection.model?.let { loadTrims(it.id, openSingle = true) }
            CatalogLevel.COMBO -> s.combo?.let { loadComboModels(it.brand.id) }
            CatalogLevel.CAN -> searchCan()
            CatalogLevel.CARD -> s.selection.carId?.let { loadCard(it) }
        }
    }

    // ── Фильтр функций ─────────────────────────────────────────

    fun onFilterOpen() = _state.update { it.copy(filterDraft = it.groups) }

    fun onFilterToggle(group: CarFeatureGroup) = _state.update { state ->
        val draft = state.filterDraft ?: return@update state
        state.copy(filterDraft = if (group in draft) draft - group else draft + group)
    }

    fun onFilterDraftReset() = _state.update { it.copy(filterDraft = it.filterDraft?.let { emptySet() }) }

    fun onFilterDismiss() = _state.update { it.copy(filterDraft = null) }

    fun onFilterApply() {
        applyGroups(_state.value.filterDraft ?: return)
    }

    fun onFilterClear() = applyGroups(emptySet())

    // ── Карточка ───────────────────────────────────────────────

    fun onTabSelect(tab: CardTab) = updateCardUi { it.copy(tab = tab, point = null, photo = 0) }

    fun onPointOpen(index: Int) = updateCardUi { it.copy(point = index, photo = 0) }

    fun onPointClose() = updateCardUi { it.copy(point = null, photo = 0) }

    fun onPhotoChange(index: Int) = updateCardUi { it.copy(photo = index) }

    fun onViewerOpen() = updateCardUi { it.copy(viewerOpen = true) }

    fun onViewerClose() = updateCardUi { it.copy(viewerOpen = false) }

    fun onNotesToggle() = updateCardUi { it.copy(notesExpanded = !it.notesExpanded) }

    fun onGroupToggle(group: CarFeatureGroup) = updateCardUi { ui ->
        val collapsed = ui.collapsedGroups
        ui.copy(collapsedGroups = if (group in collapsed) collapsed - group else collapsed + group)
    }

    fun onCapabilityNote(kind: CapabilityKind) {
        val card = _state.value.card.valueOrNull() ?: return
        val capability = when (kind) {
            CapabilityKind.ENGINE_BLOCK -> card.engineBlockByCan
            CapabilityKind.KEYLESS_BYPASS -> card.keylessBypass
        }
        _state.update { it.copy(dialog = VehicleDialog.CapabilityNote(kind, capability)) }
    }

    fun onCopyCanCode() {
        val code = _state.value.card.valueOrNull()?.summary?.canCode ?: return
        _effect.tryEmit(VehicleSelectEffect.CanCopied(code))
    }

    fun onDialogDismiss() = _state.update { it.copy(dialog = null) }

    // ── Выбор ──────────────────────────────────────────────────

    /** «Выбрать» в строке комплектации, результате CAN-поиска или в карточке. */
    fun onPick(carId: Int) {
        val current = _state.value
        if (!current.mode.pick || current.picking) return
        val known = current.card.valueOrNull()?.summary?.takeIf { it.id == carId }
            ?: current.canResults.valueOrNull()?.firstOrNull { it.id == carId }
        if (known != null) {
            choose(known)
            return
        }
        _state.update { it.copy(picking = true) }
        safeLaunch {
            val response = withContext(dispatchers.io) { repository.summary(carId) }
            _state.update { it.copy(picking = false) }
            when (response) {
                is Response.Success -> choose(response.value)
                is Response.Error -> logger.w { "Car $carId summary failed: ${response.message}" }
            }
        }
    }

    fun onMismatchConfirm() {
        val dialog = _state.value.dialog as? VehicleDialog.CanMismatch ?: return
        _state.update { it.copy(dialog = null) }
        finishPick(dialog.car)
    }

    // ── Назад ──────────────────────────────────────────────────

    /** Системный «назад»: шаг назад внутри экрана по раскладке, на верхнем уровне — выход. */
    fun onBack(layout: VehicleLayout) {
        val s = _state.value
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

    /** «Назад» по каталогу: на узком окне — на уровень выше, потом к выбору типа, потом выход. */
    private fun backFromCatalog(layout: VehicleLayout) {
        val s = _state.value
        val compactName = layout == VehicleLayout.COMPACT && s.searchMode == SearchMode.NAME
        when {
            compactName && s.selection.model != null ->
                _state.update { it.copy(selection = it.selection.copy(model = null), trims = Load.Idle, query = "") }

            compactName && s.selection.brand != null ->
                _state.update { it.copy(selection = CatalogSelection(), models = Load.Idle, query = "") }

            s.mode.typeSelectable && s.deviceType != null -> resetType()

            else -> _effect.tryEmit(VehicleSelectEffect.Exit)
        }
    }

    // ── Внутреннее ─────────────────────────────────────────────

    private fun closeCard(layout: VehicleLayout) {
        cancel(CatalogLevel.CARD)
        val s = _state.value
        val selection = when (s.selection.cardSource) {
            CardSource.CAN -> CatalogSelection()

            CardSource.RECENT -> CatalogSelection()

            CardSource.CATALOG -> {
                val singleTrim = s.trims.valueOrNull()?.size == 1
                s.selection.copy(
                    carId = null,
                    model = if (singleTrim && layout == VehicleLayout.COMPACT) null else s.selection.model
                )
            }
        }
        _state.update { it.copy(selection = selection, card = Load.Idle, cardUi = CardUi()) }
    }

    private fun resetType() {
        cancelAll()
        _state.update {
            it.copy(
                deviceType = null,
                selection = CatalogSelection(),
                query = "",
                combo = null,
                brands = Load.Idle,
                models = Load.Idle,
                trims = Load.Idle,
                comboModels = Load.Idle,
                card = Load.Idle,
                canResults = Load.Idle
            )
        }
    }

    private fun choose(car: CarSummary) {
        val firmwareCode = request.firmwareCanCode
        if (request.warnOnCanMismatch && firmwareCode != null && car.canCode != firmwareCode) {
            _state.update { it.copy(dialog = VehicleDialog.CanMismatch(car, firmwareCode)) }
        } else {
            finishPick(car)
        }
    }

    private fun finishPick(car: CarSummary) {
        rememberRecent(car)
        _effect.tryEmit(VehicleSelectEffect.Picked(car))
    }

    /**
     * Запомнить автомобиль в «недавних»: чтение-изменение-запись `Settings` — в фоне (io) и под [recentLock]
     * (хранилище не атомарно: два быстрых открытия карточек иначе потеряют одну запись); в state — готовый список.
     */
    private fun rememberRecent(car: CarSummary) {
        updateRecent { recentStore.push(request.catalog, car) }
    }

    private fun updateRecent(change: () -> Unit) {
        safeLaunch {
            val recent = recentLock.withLock {
                withContext(dispatchers.io) {
                    change()
                    recentStore.recent(request.catalog)
                }
            }
            _state.update { it.copy(recent = recent) }
        }
    }

    private fun openCard(carId: Int, source: CardSource) {
        _state.update {
            it.copy(selection = it.selection.copy(carId = carId, cardSource = source), cardUi = CardUi())
        }
        loadCard(carId)
    }

    private fun applyGroups(groups: Set<CarFeatureGroup>) {
        _state.update { it.copy(groups = groups, filterDraft = null) }
        val s = _state.value
        loadBrands()
        s.selection.brand?.let { loadModels(it.id) }
        s.selection.model?.let { loadTrims(it.id, openSingle = false) }
        s.combo?.let { loadComboModels(it.brand.id) }
        if (s.searchMode == SearchMode.CAN) searchCan()
        s.selection.carId?.let { loadCard(it) }
    }

    private fun updateCombo() {
        val s = _state.value
        if (s.searchMode != SearchMode.NAME || s.selection.brand != null) return
        val combo = s.brands.valueOrNull()?.let { detectCombo(s.query, it) }
        val sameBrand = combo?.brand?.id == s.combo?.brand?.id
        _state.update { it.copy(combo = combo) }
        when {
            combo == null -> {
                cancel(CatalogLevel.COMBO)
                _state.update { it.copy(comboModels = Load.Idle) }
            }

            !sameBrand -> loadComboModels(combo.brand.id)
        }
    }

    private fun catalogQuery(): CatalogQuery {
        val s = _state.value
        val device = request.device ?: s.deviceType?.let { VehicleDeviceFilter.Type(it) }
        return CatalogQuery(request.catalog, device, if (configurationsOnly) emptySet() else s.groups)
    }

    private fun loadBrands() = load(
        level = CatalogLevel.BRANDS,
        set = { state, load -> state.copy(brands = load) },
        fetch = { repository.brands(catalogQuery()) },
        onReady = { updateCombo() }
    )

    private fun loadModels(brandId: Int) = load(
        level = CatalogLevel.MODELS,
        set = { state, load -> state.copy(models = load) },
        fetch = { repository.models(catalogQuery(), brandId) }
    )

    private fun loadComboModels(brandId: Int) = load(
        level = CatalogLevel.COMBO,
        set = { state, load -> state.copy(comboModels = load) },
        fetch = { repository.models(catalogQuery(), brandId) }
    )

    private fun loadTrims(modelId: Int, openSingle: Boolean) = load(
        level = CatalogLevel.TRIMS,
        set = { state, load -> state.copy(trims = load) },
        fetch = { repository.trims(catalogQuery(), modelId) },
        onReady = { trims ->
            val selection = _state.value.selection
            val stillOnModel = selection.model?.id == modelId && selection.carId == null
            if (openSingle && trims.size == 1 && stillOnModel) {
                openCard(trims.single().id, CardSource.CATALOG)
            }
        }
    )

    private fun searchCan() {
        val s = _state.value
        if (s.searchMode != SearchMode.CAN || s.canCode.length != CAN_CODE_LENGTH || s.showTypeChoice) {
            cancel(CatalogLevel.CAN)
            _state.update { it.copy(canResults = Load.Idle) }
            return
        }
        val code = s.canCode
        load(
            level = CatalogLevel.CAN,
            set = { state, load -> state.copy(canResults = load) },
            fetch = { repository.findByCanCode(code, catalogQuery().device, catalogQuery().groups) },
            onReady = { cars ->
                if (cars.size == 1 && autoOpenedCode != code && _state.value.canCode == code) {
                    autoOpenedCode = code
                    openCard(cars.single().id, CardSource.CAN)
                }
            }
        )
    }

    private fun loadCard(carId: Int) = load(
        level = CatalogLevel.CARD,
        set = { state, load -> state.copy(card = load) },
        fetch = { repository.card(carId, catalogQuery().groups) },
        onReady = { card ->
            rememberRecent(card.summary)
            _state.update { it.copy(cardUi = it.cardUi.copy(tab = it.cardUi.tab ?: card.firstTab())) }
        }
    )

    private fun <T> load(
        level: CatalogLevel,
        set: (VehicleSelectState, Load<T>) -> VehicleSelectState,
        fetch: suspend () -> Response<T>,
        onReady: (T) -> Unit = {}
    ) {
        jobs.remove(level)?.cancel()
        _state.update { set(it, Load.Loading) }
        jobs[level] = safeLaunch {
            when (val response = withContext(dispatchers.io) { fetch() }) {
                is Response.Success -> {
                    _state.update { set(it, Load.Ready(response.value)) }
                    onReady(response.value)
                }

                is Response.Error -> {
                    logger.w { "Vehicle $level load failed: ${response.message}" }
                    _state.update { set(it, Load.Failed(RequestError.from(response.exception))) }
                }
            }
        }
    }

    private fun cancel(level: CatalogLevel) {
        jobs.remove(level)?.cancel()
    }

    private fun cancelAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
    }

    private fun updateCardUi(transform: (CardUi) -> CardUi) = _state.update { it.copy(cardUi = transform(it.cardUi)) }
}

/** Первая непустая вкладка карточки (`null` — вкладок нет). */
internal fun CarCard.firstTab(): CardTab? = tabs().firstOrNull()

/** Непустые вкладки карточки в порядке макета. */
internal fun CarCard.tabs(): List<CardTab> = buildList {
    if (features.isNotEmpty()) add(CardTab.FEATURES)
    if (mountPoints.isNotEmpty()) add(CardTab.MOUNT_POINTS)
    if (schemes.isNotEmpty()) add(CardTab.SCHEMES)
    if (mountingMaps.isNotEmpty()) add(CardTab.MAPS)
    if (userManuals.isNotEmpty()) add(CardTab.USER_MANUALS)
}
