package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceScope
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.capabilities
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace
import ru.alarmtrade.specmp.feature.device.settings.domain.model.search
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.accepts
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.opensEditor
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

private const val SEARCH_DEBOUNCE_MILLIS = 150L

/**
 * Сессия редактора настроек для [source] (одна на граф настроек).
 *
 * - Меню читается при открытии; для блока — на каждое (пере)подключение, с `reload`: незаписанные правки при обрыве
 *   связи теряются, после переподключения настройки перечитываются из блока.
 * - Правка ([onValueChange]) меняет значение в сессии SDK, после неё меню перечитывается из кэша сессии (правка может
 *   поменять соседние настройки); изменённые — пересечение `unsavedIds` SDK с настройками,
 *   чьё значение отличается от снимка «как было» ([baseline]): возврат исходного значения снимает подсветку.
 * - Запись ([onWrite]) — `save`; после успеха снимок «как было» = текущее меню.
 * - Сессия SDK закрывается в [onCleared] (в app-скоупе: `viewModelScope` к этому моменту отменён).
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class DeviceSettingsViewModel(
    private val source: SettingsSource,
    private val repository: DeviceSettingsRepository,
    private val preferences: DeviceSettingsPreferences,
    private val carsRepository: VehicleCatalogRepository,
    deviceSession: DeviceSession,
    private val appScope: CoroutineScope,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val deviceScope = DeviceScope(viewModelScope, deviceSession)
    private val followsDevice = source.capabilities.followsDeviceConnection

    private val _state = MutableStateFlow(
        DeviceSettingsState(
            treeWidthDp = preferences.treeWidthDp(),
            canResetToFactory = source.capabilities.canResetToFactory
        )
    )
    val state: StateFlow<DeviceSettingsState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<DeviceSettingsEffect>()
    val effect: SharedFlow<DeviceSettingsEffect> = _effect.asSharedFlow()

    /** Значения «как было» (после чтения или записи) по id настройки. */
    private val baseline = MutableStateFlow<Map<Int, SettingValue>>(emptyMap())

    init {
        if (followsDevice) {
            deviceScope.onReady(::onDeviceReady)
        } else {
            safeLaunch { load() }
        }
        _state
            .map { it.searchQuery to (it.menu as? SettingsMenu.Ready)?.root }
            .distinctUntilChanged()
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .mapLatest { (query, root) -> root?.search(query).orEmpty() }
            .flowOn(dispatchers.default)
            .onEach { hits -> _state.update { it.copy(searchHits = hits) } }
            .launchIn(viewModelScope)
    }

    private suspend fun onDeviceReady(device: AlarmDevice) {
        _state.update {
            it.copy(header = SettingsHeader(device.systemName, device.systemId, device.can?.code))
        }
        load()
    }

    private suspend fun load() = executeWithProceedLock(needSkipAnother = false) {
        _state.update {
            it.copy(
                menu = SettingsMenu.Loading,
                changedIds = emptySet(),
                sectionChanges = emptyMap(),
                editing = null,
                dialog = null,
                isWriting = false,
                isResetting = false
            )
        }
        when (val response = withContext(dispatchers.io) { repository.open(source, reload = true) }) {
            is Response.Error -> _state.update { it.copy(menu = SettingsMenu.Error) }

            is Response.Success -> {
                val root = response.value
                baseline.update { root.valuesById() }
                _state.update { it.copy(menu = SettingsMenu.Ready(root)) }
                refreshChanged()
                loadCarNames(root)
            }
        }
    }

    fun onRetry() {
        work { load() }
    }

    /** Значение изменено прямо в строке (переключатель, сегменты) или применено из окна редактирования. */
    fun onValueChange(settingId: Int, value: SettingValue) {
        work { applyValue(settingId, value) }
    }

    private suspend fun applyValue(settingId: Int, value: SettingValue) = executeWithProceedLock(
        needSkipAnother = false
    ) {
        when (val response = withContext(dispatchers.io) { repository.setValue(source, settingId, value) }) {
            is Response.Error -> _effect.tryEmit(DeviceSettingsEffect.SetValueFailed)

            is Response.Success -> {
                // Правка может поменять соседние настройки (например, выбор одного таймерного канала снимает другие):
                // перечитываем меню из кэша сессии SDK (без опроса блока); не вышло — заменяем только эту настройку.
                val reread = withContext(dispatchers.io) { repository.open(source, reload = false) }.getValueIfSuccess()
                _state.update { state ->
                    val menu = state.menu as? SettingsMenu.Ready ?: return@update state
                    state.copy(menu = SettingsMenu.Ready(reread ?: menu.root.replace(response.value)))
                }
                refreshChanged()
                currentRoot()?.let(::loadCarNames)
            }
        }
    }

    fun onSettingClick(settingId: Int) {
        val setting = currentRoot()?.findNode(settingId) as? Setting ?: return
        if (!setting.opensEditor) return
        if (setting is CanCodeSetting && _state.value.header?.singleCanCode != null) {
            _state.update { it.copy(dialog = SettingsDialog.SingleCanFirmware) }
            return
        }
        val original = baseline.value[settingId] ?: setting.value
        _state.update { it.copy(editing = EditingSetting(setting, original = original, draft = setting.value)) }
    }

    fun onDraftChange(draft: SettingValue) {
        _state.update { state ->
            val editing = state.editing ?: return@update state
            // Другой CAN-код — найденные автомобили к нему не относятся.
            val codeChanged =
                (draft as? SettingValue.Can)?.value?.code != (editing.draft as? SettingValue.Can)?.value?.code
            state.copy(
                editing = editing.copy(
                    draft = draft,
                    carSearch = if (codeChanged) CarSearch.Idle else editing.carSearch
                )
            )
        }
    }

    /** Найти автомобиль по набранному CAN-коду; один найденный — сразу привязывается к коду. */
    fun onCarSearch() {
        val editing = _state.value.editing ?: return
        val code = (editing.draft as? SettingValue.Can)?.value?.code ?: return
        if (!editing.setting.accepts(editing.draft)) return
        _state.update { it.copy(editing = it.editing?.copy(carSearch = CarSearch.Loading)) }
        safeLaunch {
            val response = withContext(dispatchers.io) { carsRepository.findByCanCode(code, vehicleDevice()) }
            _state.update { state ->
                val current = state.editing ?: return@update state
                if ((current.draft as? SettingValue.Can)?.value?.code != code) return@update state
                when (response) {
                    is Response.Error -> state.copy(editing = current.copy(carSearch = CarSearch.Failed))

                    is Response.Success -> {
                        val cars = response.value
                        val draft = cars.singleOrNull()?.let { SettingValue.Can(CanCode(code, it.id)) } ?: current.draft
                        state.copy(
                            editing = current.copy(draft = draft, carSearch = CarSearch.Found(cars)),
                            cars = state.cars + cars.associateBy { it.id }
                        )
                    }
                }
            }
        }
    }

    /** Выбран автомобиль из нескольких подходящих к коду. */
    fun onCarPick(carId: Int) {
        _state.update { state ->
            val editing = state.editing ?: return@update state
            val draft = editing.draft as? SettingValue.Can ?: return@update state
            state.copy(editing = editing.copy(draft = SettingValue.Can(draft.value.copy(carId = carId))))
        }
    }

    /** «Выбрать из каталога» в окне CAN-кода. */
    fun onVehiclePickerOpen() {
        if (_state.value.editing?.draft is SettingValue.Can) _state.update { it.copy(vehiclePicker = true) }
    }

    fun onVehiclePickerClose() = _state.update { it.copy(vehiclePicker = false) }

    /** Автомобиль выбран в каталоге: его CAN-код и `carId` — в черновик окна CAN-кода (применяется по «Готово»). */
    fun onCarPicked(car: CarSummary) {
        _state.update { state ->
            val editing = state.editing
            if (editing == null || editing.draft !is SettingValue.Can) return@update state.copy(vehiclePicker = false)
            state.copy(
                vehiclePicker = false,
                editing = editing.copy(
                    draft = SettingValue.Can(CanCode(car.canCode, car.id)),
                    carSearch = CarSearch.Idle
                ),
                cars = state.cars + (car.id to car)
            )
        }
    }

    fun onEditReset() {
        _state.update { state -> state.copy(editing = state.editing?.let { it.copy(draft = it.original) }) }
    }

    fun onEditApply() {
        val editing = _state.value.editing ?: return
        if (!editing.setting.accepts(editing.draft)) return
        _state.update { it.copy(editing = null) }
        if (editing.draft != editing.setting.value) onValueChange(editing.setting.id, editing.draft)
    }

    fun onEditDismiss() {
        _state.update { it.copy(editing = null) }
    }

    fun onDescriptionClick(nodeId: Int) {
        val node = currentRoot()?.findNode(nodeId) ?: return
        _state.update { it.copy(dialog = SettingsDialog.Description(node)) }
    }

    fun onDialogDismiss() {
        _state.update { it.copy(dialog = null) }
    }

    /** Ширина колонки дерева изменена перетаскиванием (по окончании) — запоминается для следующих входов. */
    fun onTreeWidthChange(widthDp: Int) {
        _state.update { it.copy(treeWidthDp = widthDp) }
        preferences.setTreeWidthDp(widthDp)
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    /** Выход ([target] — «назад» с корня или к обновлению CAN): с незаписанными изменениями — подтверждение. */
    fun onExitRequest(target: ExitTarget = ExitTarget.BACK) {
        if (_state.value.changedCount > 0) {
            _state.update { it.copy(dialog = SettingsDialog.ExitConfirm(target)) }
        } else {
            _state.update { it.copy(dialog = null) }
            _effect.tryEmit(DeviceSettingsEffect.Exit(target))
        }
    }

    /** «Перейти к обновлению» в диалоге единой CAN-прошивки. */
    fun onSingleCanUpdate() = onExitRequest(ExitTarget.FIRMWARE_UPDATE)

    fun onExitDiscard() {
        val target = exitTarget()
        _state.update { it.copy(dialog = null) }
        _effect.tryEmit(DeviceSettingsEffect.Exit(target))
    }

    private fun exitTarget(): ExitTarget =
        (_state.value.dialog as? SettingsDialog.ExitConfirm)?.target ?: ExitTarget.BACK

    fun onWrite() {
        work { write(exitTo = null) }
    }

    fun onExitWrite() {
        val target = exitTarget()
        work { write(exitTo = target) }
    }

    // Следом за правками в очереди (не пропуск): запись не должна теряться, пока применяется значение.
    private suspend fun write(exitTo: ExitTarget?) = executeWithProceedLock(needSkipAnother = false) {
        val count = _state.value.changedCount
        if (count == 0 && exitTo == null) return@executeWithProceedLock
        _state.update { it.copy(isWriting = true, dialog = null) }
        val response = try {
            withContext(dispatchers.io) { repository.save(source) }
        } finally {
            _state.update { it.copy(isWriting = false) }
        }
        when (response) {
            is Response.Error -> _state.update { it.copy(dialog = SettingsDialog.WriteError) }

            is Response.Success -> {
                currentRoot()?.let { root -> baseline.update { root.valuesById() } }
                refreshChanged()
                _effect.tryEmit(DeviceSettingsEffect.Written(count))
                exitTo?.let { _effect.tryEmit(DeviceSettingsEffect.Exit(it)) }
            }
        }
    }

    /** Меню «⋯» → «Сбросить к заводским»: сначала подтверждение. */
    fun onResetRequest() {
        if (!_state.value.canResetToFactory) return
        _state.update { it.copy(dialog = SettingsDialog.ResetConfirm) }
    }

    /** Подтверждён сброс (или «Повторить» после ошибки): после успеха меню перечитывается из блока. */
    fun onResetConfirm() {
        work { reset() }
    }

    private suspend fun reset() {
        var done = false
        executeWithProceedLock(needSkipAnother = false) {
            _state.update { it.copy(isResetting = true, dialog = null) }
            val response = try {
                withContext(dispatchers.io) { repository.resetDeviceToFactory() }
            } finally {
                _state.update { it.copy(isResetting = false) }
            }
            when (response) {
                is Response.Error -> _state.update { it.copy(dialog = SettingsDialog.ResetError) }
                is Response.Success -> done = true
            }
        }
        if (done) {
            load()
            _effect.tryEmit(DeviceSettingsEffect.ResetDone)
        }
    }

    /** Пересчитать изменённые настройки и счётчики разделов. */
    private suspend fun refreshChanged() {
        val root = currentRoot() ?: return
        val unsaved = withContext(dispatchers.io) { repository.unsavedIds(source) }.getValueIfSuccess()
        val changes = withContext(dispatchers.default) {
            val before = baseline.value
            val differing = root.settings().filter { it.value != before[it.id] }.mapTo(mutableSetOf()) { it.id }
            val changed = if (unsaved == null) differing else differing.intersect(unsaved)
            changed to root.sectionChanges(changed)
        }
        _state.update { it.copy(changedIds = changes.first, sectionChanges = changes.second) }
    }

    /** Подписи автомобилей к CAN-кодам меню (с сервера, без связи — без подписи). */
    private fun loadCarNames(root: SettingsSection) {
        val known = _state.value.cars.keys
        root.settings().filterIsInstance<CanCodeSetting>()
            .mapNotNull { it.value.value.carId }
            .filter { it !in known }
            .distinct()
            .forEach { carId ->
                safeLaunch {
                    withContext(dispatchers.io) { carsRepository.summary(carId) }.getValueIfSuccess()?.let { car ->
                        _state.update { it.copy(cars = it.cars + (car.id to car)) }
                    }
                }
            }
    }

    /** Каталог автомобилей — под модель подключённого блока (его подсистемы); блок неизвестен — без фильтра. */
    private fun vehicleDevice(): VehicleDeviceFilter? =
        _state.value.header?.let { VehicleDeviceFilter.Device(it.deviceName) }

    private fun currentRoot(): SettingsSection? = (_state.value.menu as? SettingsMenu.Ready)?.root

    /** Работа с источником: для блока — только при связи (отменяется при обрыве), иначе — в скоупе VM. */
    private fun work(block: suspend CoroutineScope.() -> Unit): Job? =
        if (followsDevice) deviceScope.launch(block) else safeLaunch(block = block)

    override fun onCleared() {
        appScope.launch(dispatchers.io) { repository.close(source) }
        super.onCleared()
    }
}

/** Число изменённых настроек в каждом разделе дерева (на любой глубине); разделы без изменений не входят. */
private fun SettingsSection.sectionChanges(changed: Set<Int>): Map<Int, Int> {
    val result = mutableMapOf<Int, Int>()
    fun count(section: SettingsSection): Int {
        var total = 0
        section.children.forEach { child ->
            total += when (child) {
                is SettingsSection -> count(child)
                is Setting -> if (child.id in changed) 1 else 0
            }
        }
        if (total > 0) result[section.id] = total
        return total
    }
    count(this)
    return result
}

/** Значения всех настроек меню по id — снимок «как было». */
private fun SettingsSection.valuesById(): Map<Int, SettingValue> = settings().associate { setting ->
    setting.id to
        setting.value
}
