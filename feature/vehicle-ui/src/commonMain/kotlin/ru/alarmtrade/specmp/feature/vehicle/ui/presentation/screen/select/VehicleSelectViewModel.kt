package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.screen.select

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel
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
import ru.alarmtrade.specmp.feature.vehicle.ui.presentation.model.CatalogError
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
    private val dispatchers: AppDispatchers,
) : LegacyBaseViewModel() {

    private val configurationsOnly = request.catalog == VehicleCatalog.WITH_CONFIGURATIONS

    private val _state = MutableStateFlow(
        VehicleSelectState(
            mode = VehicleSelectModeUi(
                pick = request.mode == VehicleSelectMode.PICK,
                configurationsOnly = configurationsOnly,
                typeSelectable = request.device == null,
                deviceName = (request.device as? VehicleDeviceFilter.Device)?.deviceName,
                firmwareCanCode = request.firmwareCanCode,
            ),
            deviceType = (request.device as? VehicleDeviceFilter.Type)?.type,
            canCode = request.firmwareCanCode.takeUnless { configurationsOnly }.orEmpty(),
        ),
    )

    // Старт экрана — при первой подписке на state: недавние (хранилище — на io) и марки, если тип устройства известен.
    val state: StateFlow<VehicleSelectState> = _state
        .launchOnFirstStart {
            // Загрузки уровней каталога: новая загрузка уровня отменяет прежнюю (collectLatest), без полей-Job.
            levelLoads.values.forEach { loads ->
                launch(start = CoroutineStart.UNDISPATCHED) {
                    loads.collectLatest { it.run?.let { run -> runLoad(run) } }
                }
            }
            val recent = withContext(dispatchers.io) { recentStore.recent(request.catalog) }
            _state.update { it.copy(recent = recent) }
            if (!_state.value.showTypeChoice) loadBrands()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), _state.value)

    private val _effect = sideEffectFlow<VehicleSelectEffect>()
    val effect: SharedFlow<VehicleSelectEffect> = _effect.asSharedFlow()

    /** Загрузка уровня каталога ([run]) или её отмена (`null`). */
    private class LevelLoad(val run: (suspend () -> Unit)?)

    // replay = 1: запрос до первой подписки на state не теряется (сборщики стартуют в launchOnFirstStart).
    private val levelLoads: Map<CatalogLevel, MutableSharedFlow<LevelLoad>> = CatalogLevel.entries.associateWith {
        MutableSharedFlow(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
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
                models = LCEState.None,
                trims = LCEState.None,
                card = LCEState.None,
                canResults = LCEState.None,
            )
        }
        loadBrands()
        if (_state.value.searchMode == SearchMode.CAN) searchCan()
    }

    // ── Поиск ──────────────────────────────────────────────────

    fun onSearchModeChange(mode: SearchMode) {
        if (mode == _state.value.searchMode) return
        cancel(CatalogLevel.CARD)
        _state.update {
            it.copy(
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
        if (digits == _state.value.canCode && _state.value.canResults != LCEState.None) return
        cancel(CatalogLevel.CARD)
        _state.update {
            it.copy(autoOpenedCan = null, canCode = digits, selection = CatalogSelection(), card = LCEState.None)
        }
        searchCan()
    }

    // ── Каталог ────────────────────────────────────────────────

    fun onBrandPick(brand: CarBrand) {
        val current = _state.value
        val reuse = current.combo?.brand?.id == brand.id && current.comboModels is LCEState.Content
        cancel(CatalogLevel.TRIMS)
        cancel(CatalogLevel.CARD)
        _state.update {
            it.copy(
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

    /**
     * Модель: грузятся комплектации, единственная — сразу карточка. На широком окне повторный тап по раскрытой
     * модели сворачивает её ([wide]).
     */
    fun onModelPick(model: CarModel, wide: Boolean) {
        val current = _state.value
        if (wide && current.selection.model?.id == model.id && current.selection.carId == null) {
            cancel(CatalogLevel.TRIMS)
            _state.update { it.copy(selection = it.selection.copy(model = null), trims = LCEState.None) }
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
                trims = LCEState.Loading,
                card = LCEState.None,
            )
        }
        loadTrims(model.id, openSingle = true)
    }

    /** Крошки: вернуться к маркам (или к результатам CAN-поиска), к моделям марки, к комплектациям модели. */
    fun onCrumb(target: CrumbTarget) {
        cancel(CatalogLevel.CARD)
        _state.update { it.withCrumb(target) }
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
        val card = _state.value.card.content ?: return
        val capability = when (kind) {
            CapabilityKind.ENGINE_BLOCK -> card.engineBlockByCan
            CapabilityKind.KEYLESS_BYPASS -> card.keylessBypass
        }
        _state.update { it.copy(dialog = VehicleDialog.CapabilityNote(kind, capability)) }
    }

    fun onCopyCanCode() {
        val code = _state.value.card.content?.summary?.canCode ?: return
        _effect.tryEmit(VehicleSelectEffect.CanCopied(code))
    }

    fun onDialogDismiss() = _state.update { it.copy(dialog = null) }

    // ── Выбор ──────────────────────────────────────────────────

    /** «Выбрать» в строке комплектации, результате CAN-поиска или в карточке. */
    fun onPick(carId: Int) {
        val current = _state.value
        if (!current.mode.pick || current.picking) return
        val known = current.card.content?.summary?.takeIf { it.id == carId }
            ?: current.canResults.content?.firstOrNull { it.id == carId }
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

                is Response.Error -> _effect.tryEmit(
                    VehicleSelectEffect.PickFailed(CatalogError.from(response.exception)),
                )
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
        when {
            s.catalogStepBack(layout) != null -> _state.update { it.catalogStepBack(layout) ?: it }
            s.mode.typeSelectable && s.deviceType != null -> resetType()
            else -> _effect.tryEmit(VehicleSelectEffect.Exit)
        }
    }

    // ── Внутреннее ─────────────────────────────────────────────

    private fun closeCard(layout: VehicleLayout) {
        cancel(CatalogLevel.CARD)
        _state.update { it.withCardClosed(layout) }
    }

    private fun resetType() {
        cancelAll()
        _state.update { it.withTypeReset() }
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

    /** Запомнить автомобиль в «недавних» (хранилище атомарно само; Settings — на io); в state — готовый список. */
    private fun rememberRecent(car: CarSummary) {
        updateRecent { recentStore.push(request.catalog, car) }
    }

    private fun updateRecent(change: suspend () -> List<CarSummary>) {
        safeLaunch {
            val recent = withContext(dispatchers.io) { change() }
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
        val combo = s.brands.content?.let { detectCombo(s.query, it) }
        val sameBrand = combo?.brand?.id == s.combo?.brand?.id
        _state.update { it.copy(combo = combo) }
        when {
            combo == null -> {
                cancel(CatalogLevel.COMBO)
                _state.update { it.copy(comboModels = LCEState.None) }
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
        onReady = { updateCombo() },
    )

    private fun loadModels(brandId: Int) = load(
        level = CatalogLevel.MODELS,
        set = { state, load -> state.copy(models = load) },
        fetch = { repository.models(catalogQuery(), brandId) },
    )

    private fun loadComboModels(brandId: Int) = load(
        level = CatalogLevel.COMBO,
        set = { state, load -> state.copy(comboModels = load) },
        fetch = { repository.models(catalogQuery(), brandId) },
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
        },
    )

    private fun searchCan() {
        val s = _state.value
        if (s.searchMode != SearchMode.CAN || s.canCode.length != CAN_CODE_LENGTH || s.showTypeChoice) {
            cancel(CatalogLevel.CAN)
            _state.update { it.copy(canResults = LCEState.None) }
            return
        }
        val code = s.canCode
        load(
            level = CatalogLevel.CAN,
            set = { state, load -> state.copy(canResults = load) },
            fetch = { repository.findByCanCode(code, catalogQuery().device, catalogQuery().groups) },
            onReady = { cars ->
                val current = _state.value
                if (cars.size == 1 && current.autoOpenedCan != code && current.canCode == code) {
                    _state.update { it.copy(autoOpenedCan = code) }
                    openCard(cars.single().id, CardSource.CAN)
                }
            },
        )
    }

    private fun loadCard(carId: Int) = load(
        level = CatalogLevel.CARD,
        set = { state, load -> state.copy(card = load) },
        fetch = { repository.card(carId, catalogQuery().groups) },
        onReady = { card ->
            rememberRecent(card.summary)
            _state.update { it.copy(cardUi = it.cardUi.copy(tab = it.cardUi.tab ?: card.firstTab())) }
        },
    )

    private fun <T> load(
        level: CatalogLevel,
        set: (VehicleSelectState, LCEState<T, CatalogError>) -> VehicleSelectState,
        fetch: suspend () -> Response<T>,
        onReady: (T) -> Unit = {},
    ) {
        _state.update { set(it, LCEState.Loading) }
        levelLoads.getValue(level).tryEmit(
            LevelLoad {
                val response = withContext(dispatchers.io) { fetch() }
                _state.update { set(it, response.asLCEState(CatalogError::from)) }
                response.getValueIfSuccess()?.let(onReady)
            },
        )
    }

    /**
     * Загрузка — отдельной работой VM (сбой логируется, как у safeLaunch, и не обрывает сборщик уровня); отмена
     * сборщика (новый запрос уровня) отменяет и её.
     */
    private suspend fun runLoad(run: suspend () -> Unit) {
        val job = safeLaunch { run() }
        try {
            job.join()
        } finally {
            job.cancel()
        }
    }

    private fun cancel(level: CatalogLevel) {
        levelLoads.getValue(level).tryEmit(LevelLoad(null))
    }

    private fun cancelAll() {
        CatalogLevel.entries.forEach(::cancel)
    }

    private fun updateCardUi(transform: (CardUi) -> CardUi) = _state.update { it.copy(cardUi = transform(it.cardUi)) }

    private companion object {
        val STOP_TIMEOUT = 5.seconds
    }
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
