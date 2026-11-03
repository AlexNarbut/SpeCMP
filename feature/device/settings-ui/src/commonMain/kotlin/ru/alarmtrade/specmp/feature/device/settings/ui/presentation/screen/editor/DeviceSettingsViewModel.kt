package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.CanCodeSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSearchHit
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.capabilities
import ru.alarmtrade.specmp.feature.device.settings.domain.model.deviceNameOrNull
import ru.alarmtrade.specmp.feature.device.settings.domain.model.diffTo
import ru.alarmtrade.specmp.feature.device.settings.domain.model.findNode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.replace
import ru.alarmtrade.specmp.feature.device.settings.domain.model.reusing
import ru.alarmtrade.specmp.feature.device.settings.domain.model.search
import ru.alarmtrade.specmp.feature.device.settings.domain.model.settings
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.opensEditor
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationPickKind
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.navigation.ConfigurationPickRequest
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.settingsFileName
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.withoutSettingsExtension
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

private const val SEARCH_DEBOUNCE_MILLIS = 150L
private val STATE_STOP_TIMEOUT = 5.seconds

/**
 * Сессия редактора настроек для [source] (одна на граф настроек).
 *
 * - Меню читается при открытии; для блока — на каждое (пере)подключение, с `reload`: незаписанные правки при обрыве
 *   связи теряются, после переподключения настройки перечитываются из блока.
 * - Правка ([onValueChange]) меняет значение в сессии SDK, после неё меню перечитывается из кэша сессии (правка может
 *   поменять соседние настройки); изменённые — пересечение `unsavedIds` SDK с настройками, чьё значение отличается от
 *   снимка «как было» ([SettingsChangeTracker]): возврат исходного значения снимает подсветку.
 * - Запись ([onWrite]) — `save`; после успеха снимок «как было» = текущее меню.
 * - Сессия SDK закрывается в [onCleared] (в app-скоупе: `viewModelScope` к этому моменту отменён).
 * - Конфигурации: «Загрузить из…» (блок) — снимок источника в модели блока → «Что изменится» → копирование в сессию
 *   блока (без записи; SDK не отмечает скопированное как изменённое — изменённые после загрузки считает
 *   [SettingsChangeTracker.markLoaded]); «Сохранить в…» — «Мои конфигурации» или файл (снимок → системное
 *   сохранение). Для конфигураций «Сохранить» пишет в источник, у источников без записи — только «как…».
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class DeviceSettingsViewModel(
    private val source: SettingsSource,
    options: SettingsEditorOptions,
    private val repository: DeviceSettingsRepository,
    private val localConfigurations: LocalConfigurationsRepository,
    private val preferences: DeviceSettingsPreferences,
    private val carsRepository: VehicleCatalogRepository,
    deviceSession: DeviceSession,
    private val appScope: CoroutineScope,
    private val dispatchers: AppDispatchers
) : BaseDeviceViewModel(deviceSession) {

    private val followsDevice = source.capabilities.followsDeviceConnection

    private val _state = MutableStateFlow(
        DeviceSettingsState(
            treeWidthDp = preferences.treeWidthDp(),
            canResetToFactory = source.capabilities.canResetToFactory,
            source = source.header(),
            canWriteToDevice = options.canWriteToDevice
        )
    )
    private val _effect = sideEffectFlow<DeviceSettingsEffect>()
    val effect: SharedFlow<DeviceSettingsEffect> = _effect.asSharedFlow()

    /** «Как было», «загружено» и правило «изменено» — см. [SettingsChangeTracker]. */
    private val changes = SettingsChangeTracker()

    /**
     * `carId`, подпись к которым уже запрашивалась в этом чтении меню (в том числе неудачно — без сети запрос не
     * повторяется на каждую правку). Проверка и запись — одним атомарным `getAndUpdate`: параллельные правки не
     * запросят один `carId` дважды.
     */
    private val requestedCarIds = MutableStateFlow<Set<Int>>(emptySet())

    /** Набираемый запрос: в state попадает вместе с результатом (после debounce), а не на каждый символ. */
    private val searchQuery = MutableStateFlow("")

    /** Конфигурация, которую загрузить сразу после первого чтения меню блока («Записать в блок» из конфигуратора). */
    private val pendingLoad = MutableStateFlow(options.loadOnOpen)

    /** Снимок для «Что изменится» (временный файл) и откуда он; после загрузки или отмены — удаляется. */
    private val preview = MutableStateFlow<Preview?>(null)

    /** Снимок, отданный системному сохранению файла; [Export.commit] — правки считаются сохранёнными («как…»). */
    private val export = MutableStateFlow<Export?>(null)

    /** Поиск: запрос в state попадает вместе с результатом (после debounce), а не на каждый символ. */
    private val search: Flow<Pair<String, List<SettingsSearchHit>>> =
        combine(searchQuery, _state.map { it.menu.content }.distinctUntilChanged { a, b -> a === b }) { query, root ->
            query to root
        }
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .mapLatest { (query, root) -> query to root?.search(query).orEmpty() }
            .onStart { emit("" to emptyList()) }

    // Источник не блока (файл, мои, облако, заводские) читается при первой подписке на state, один раз за жизнь VM.
    // Блок перечитывается на каждое (пере)подключение — это реакция init, живущая всю жизнь VM.
    val state: StateFlow<DeviceSettingsState> = combine(_state, connectedDeviceName, search) {
            state,
            connected,
            (query, hits)
        ->
        state.copy(connectedDeviceName = connected, searchQuery = query, searchHits = hits)
    }
        // Блок — чтение на каждое (пере)подключение; конфигурация/файл — одно чтение.
        .launchOnFirstStart { if (followsDevice) onEachDeviceReady(::onDeviceReady) else load() }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT), _state.value)

    private suspend fun onDeviceReady(device: AlarmDevice) {
        _state.update {
            it.copy(header = SettingsHeader(device.systemName, device.systemId, device.can?.code))
        }
        load()
    }

    private suspend fun load() {
        readMenu()
        // «Записать в блок» из конфигуратора: после первого чтения блока — сравнение с конфигурацией.
        if (_state.value.menu is LCEState.Content) {
            pendingLoad.getAndUpdate { null }?.let { requestLoad(it, closeAfter = false) }
        }
    }

    private suspend fun readMenu() = executeWithProceedLock(needSkipAnother = false) {
        requestedCarIds.update { emptySet() }
        changes.clearLoaded()
        // Сравнение с прежним меню устарело (а прерванное обрывом связи осталось бы в Busy) — сбрасываем.
        discardPreview()
        _state.update {
            it.copy(
                menu = LCEState.Loading,
                changedIds = emptySet(),
                sectionChanges = emptyMap(),
                editing = null,
                dialog = null,
                diff = null,
                isWriting = false,
                isResetting = false
            )
        }
        when (val response = withContext(dispatchers.io) { repository.open(source, reload = true) }) {
            is Response.Error -> {
                logger.w(response.exception) { "Settings menu read failed: ${response.message}" }
                _state.update { it.copy(menu = LCEState.Error(SettingsError.from(response.exception))) }
            }

            is Response.Success -> {
                val root = response.value
                // Обход всего дерева — на default, как и в showMenu.
                withContext(dispatchers.default) { changes.resetBaseline(root) }
                showMenu(root)
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
            is Response.Error -> {
                logger.w(response.exception) { "Setting $settingId change failed: ${response.message}" }
                _effect.tryEmit(DeviceSettingsEffect.SetValueFailed)
            }

            is Response.Success -> {
                // Правка может поменять соседние настройки (например, выбор одного таймерного канала снимает другие):
                // перечитываем меню из кэша сессии SDK (без опроса блока); не вышло — заменяем только эту настройку.
                // Неизменённые узлы берутся из текущего меню — их строки не перерисовываются.
                val current = currentRoot() ?: return@executeWithProceedLock
                val reread = withContext(dispatchers.io) { repository.open(source, reload = false) }.getValueIfSuccess()
                val root = withContext(dispatchers.default) {
                    reread?.reusing(current) ?: current.replace(response.value)
                }
                showMenu(root)
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
        val original = changes.original(settingId) ?: setting.value
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
                    carSearch = if (codeChanged) LCEState.None else editing.carSearch
                )
            )
        }
    }

    /** Найти автомобиль по набранному CAN-коду; один найденный — сразу привязывается к коду. */
    fun onCarSearch() {
        val editing = _state.value.editing ?: return
        val code = (editing.draft as? SettingValue.Can)?.value?.code ?: return
        if (!editing.canApply) return
        _state.update { it.copy(editing = it.editing?.copy(carSearch = LCEState.Loading)) }
        safeLaunch {
            val response = withContext(dispatchers.io) { carsRepository.findByCanCode(code, vehicleDevice()) }
            _state.update { it.withCarSearch(code, response) }
        }
    }

    /** Выбран автомобиль из нескольких подходящих к коду. */
    fun onCarPick(carId: Int) = _state.update { it.withCarPicked(carId) }

    /** «Выбрать из каталога» в окне CAN-кода. */
    fun onVehiclePickerOpen() {
        if (_state.value.editing?.draft is SettingValue.Can) _state.update { it.copy(vehiclePicker = true) }
    }

    fun onVehiclePickerClose() = _state.update { it.copy(vehiclePicker = false) }

    /** Автомобиль выбран в каталоге: его CAN-код и `carId` — в черновик окна CAN-кода (применяется по «Готово»). */
    fun onCarPicked(car: CarSummary) = _state.update { it.withCatalogCar(car) }

    fun onEditReset() {
        _state.update { state -> state.copy(editing = state.editing?.let { it.copy(draft = it.original) }) }
    }

    fun onEditApply() {
        val editing = _state.value.editing ?: return
        if (!editing.canApply) return
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
        searchQuery.update { query }
    }

    /**
     * Выход ([target] — «назад» с корня или к обновлению CAN): с незаписанными изменениями — подтверждение (блок —
     * «Записать и выйти», конфигурация — «Сохранить»).
     */
    fun onExitRequest(target: ExitTarget = ExitTarget.BACK) {
        if (_state.value.changedCount > 0) {
            val dialog = if (source == SettingsSource.Device) {
                SettingsDialog.ExitConfirm(target)
            } else {
                SettingsDialog.ExitSave
            }
            _state.update { it.copy(dialog = dialog) }
        } else {
            _state.update { it.copy(dialog = null) }
            _effect.tryEmit(DeviceSettingsEffect.Exit(target))
        }
    }

    /** «Перейти к обновлению» в диалоге единой CAN-прошивки. */
    fun onSingleCanUpdate() = onExitRequest(ExitTarget.FIRMWARE_UPDATE)

    fun onExitDiscard() {
        val target = takeExitTarget() ?: return
        _effect.tryEmit(DeviceSettingsEffect.Exit(target))
    }

    /**
     * Диалог выхода забирается сразу (интенты — на главном потоке по очереди): повторный тап его уже не застанет и не
     * запишет/не выйдет второй раз. `null` — диалога нет.
     */
    private fun takeExitTarget(): ExitTarget? {
        val dialog = _state.value.dialog
        val target = when (dialog) {
            is SettingsDialog.ExitConfirm -> dialog.target
            SettingsDialog.ExitSave -> ExitTarget.BACK
            else -> return null
        }
        _state.update { it.copy(dialog = null) }
        return target
    }

    fun onWrite() {
        work { write(exitTo = null) }
    }

    fun onExitWrite() {
        val target = takeExitTarget() ?: return
        work { write(exitTo = target) }
    }

    // Следом за правками в очереди (не пропуск): запись не должна теряться, пока применяется значение.
    private suspend fun write(exitTo: ExitTarget?) = executeWithProceedLock(needSkipAnother = false) {
        val count = _state.value.changedCount
        if (count == 0 && exitTo == null) return@executeWithProceedLock
        val error = saveToSource { state, inProgress -> state.copy(isWriting = inProgress) }
        if (error != null) {
            logger.w(error.exception) { "Settings write failed: ${error.message}" }
            _state.update { it.copy(dialog = SettingsDialog.WriteError) }
            return@executeWithProceedLock
        }
        _effect.tryEmit(DeviceSettingsEffect.Written(count))
        exitTo?.let { _effect.tryEmit(DeviceSettingsEffect.Exit(it)) }
    }

    /**
     * `save` источника (блок — запись в блок, конфигурация/файл — сохранение) с флагом прогресса [progress]; успех —
     * снимок «как было» = текущее меню. Ответ — ошибка или `null`.
     */
    private suspend fun saveToSource(progress: (DeviceSettingsState, Boolean) -> DeviceSettingsState): Response.Error? {
        _state.update { progress(it, true).copy(dialog = null) }
        val response = try {
            withContext(dispatchers.io) { repository.save(source) }
        } finally {
            _state.update { progress(it, false) }
        }
        if (response is Response.Success) commit()
        return response as? Response.Error
    }

    /** Правки записаны/сохранены: снимок «как было» = текущее меню, подсветка снимается. */
    private suspend fun commit() {
        currentRoot()?.let { root -> withContext(dispatchers.default) { changes.resetBaseline(root) } }
        changes.clearLoaded()
        refreshChanged()
    }

    // ── Загрузка конфигурации в редактор блока (K7 → K9) ─────────────────────────────────────────────────────

    /** Меню «⋯» → «Загрузить из…». */
    fun onLoadRequest() {
        if (source != SettingsSource.Device || currentRoot() == null) return
        _state.update { it.copy(dialog = SettingsDialog.LoadSources) }
    }

    fun onLoadSourcePick(kind: LoadSourceKind) {
        val deviceName = deviceModel() ?: return
        _state.update { it.copy(dialog = null) }
        when (kind) {
            LoadSourceKind.MINE, LoadSourceKind.CLOUD -> _state.update {
                it.copy(
                    configurationPicker = ConfigurationPickRequest(
                        kind = if (kind == LoadSourceKind.MINE) {
                            ConfigurationPickKind.MINE
                        } else {
                            ConfigurationPickKind.CLOUD
                        },
                        deviceName = deviceName,
                        firmwareCanCode = it.header?.singleCanCode
                    )
                )
            }

            LoadSourceKind.FILE -> _effect.tryEmit(DeviceSettingsEffect.PickFile)

            LoadSourceKind.DEFAULTS -> requestLoad(SettingsSource.Defaults(deviceName), closeAfter = true)
        }
    }

    /** Выбрана конфигурация в «Моих» или облаке. */
    fun onConfigurationPicked(picked: SettingsSource) {
        _state.update { it.copy(configurationPicker = null) }
        requestLoad(picked, closeAfter = true)
    }

    fun onConfigurationPickerClose() = _state.update { it.copy(configurationPicker = null) }

    /** Результат системного выбора файла настроек: модель — модель блока. */
    fun onFilePicked(result: SettingsFilePickResult) {
        when (result) {
            is SettingsFilePickResult.Picked -> {
                val deviceName = deviceModel() ?: return
                requestLoad(
                    SettingsSource.File(result.path, deviceName, name = result.name, inPlace = result.inPlace),
                    closeAfter = true
                )
            }

            SettingsFilePickResult.Failed -> _state.update {
                it.copy(dialog = SettingsDialog.LoadError(LoadErrorKind.FILE))
            }

            SettingsFilePickResult.Cancelled -> Unit
        }
    }

    /** Модель конфигурации другая — предупреждение, иначе сразу сравнение. */
    private fun requestLoad(from: SettingsSource, closeAfter: Boolean) {
        val deviceName = deviceModel() ?: return
        val sourceDeviceName = from.deviceNameOrNull
        if (sourceDeviceName != null && !sourceDeviceName.equals(deviceName, ignoreCase = true)) {
            _state.update {
                it.copy(
                    dialog = SettingsDialog.ModelMismatch(
                        from,
                        sourceDeviceName,
                        deviceName,
                        forWrite = false,
                        closeAfter = closeAfter
                    )
                )
            }
            return
        }
        startPreview(from, closeAfter, notStarted = null)
    }

    /**
     * Сравнение с [from]: «Сравниваем…» — сразу (повторный запуск во время сравнения ничего не делает), сама работа —
     * в очереди за правками (не пропуск: иначе выбор «Загрузить из…», пока применяется значение, молча терялся).
     * Без связи работа не стартует — показываем [notStarted].
     */
    private fun startPreview(from: SettingsSource, closeAfter: Boolean, notStarted: DiffState?) {
        if (_state.value.diff is DiffState.Busy) return
        _state.update { it.copy(diff = DiffState.Busy(from.displayTitle), dialog = null) }
        work { preview(from, closeAfter) } ?: _state.update { it.copy(diff = notStarted) }
    }

    /** «Продолжить» в предупреждении о другой модели. */
    fun onMismatchContinue() {
        val dialog = _state.value.dialog as? SettingsDialog.ModelMismatch ?: return
        _state.update { it.copy(dialog = null) }
        if (dialog.forWrite) {
            _effect.tryEmit(DeviceSettingsEffect.WriteToDevice(source))
        } else {
            startPreview(dialog.source, dialog.closeAfter, notStarted = null)
        }
    }

    /** «Повторить» в ошибке сравнения. */
    fun onDiffRetry() {
        val error = _state.value.diff as? DiffState.Error ?: return
        startPreview(error.from, error.closeAfter, notStarted = error)
    }

    /** «Сравниваем…» снимается и при отмене (обрыв связи, в т.ч. пока работа ждёт lock) и раннем выходе. */
    private suspend fun preview(from: SettingsSource, closeAfter: Boolean) {
        try {
            executeWithProceedLock(needSkipAnother = false) { previewLocked(from, closeAfter) }
        } finally {
            _state.update { if (it.diff is DiffState.Busy) it.copy(diff = null) else it }
        }
    }

    private suspend fun previewLocked(from: SettingsSource, closeAfter: Boolean) {
        val deviceName = deviceModel() ?: return
        val current = currentRoot() ?: return
        val title = from.displayTitle
        discardPreview()
        val snapshot = withContext(dispatchers.io) { repository.snapshot(from, deviceName) }
        if (closeAfter && from != source) withContext(dispatchers.io) { repository.close(from) }
        val next = (snapshot as? Response.Success)?.let {
            withContext(dispatchers.io) { repository.open(it.value, reload = true) }
        }
        if (snapshot !is Response.Success || next !is Response.Success) {
            (snapshot as? Response.Error)?.let {
                logger.w(it.exception) { "Configuration snapshot failed: ${it.message}" }
            }
            (next as? Response.Error)?.let { logger.w(it.exception) { "Configuration read failed: ${it.message}" } }
            (snapshot as? Response.Success)?.let { discard(it.value) }
            _state.update {
                if (from is SettingsSource.File) {
                    it.copy(diff = null, dialog = SettingsDialog.LoadError(LoadErrorKind.FILE))
                } else {
                    it.copy(diff = DiffState.Error(title, from, closeAfter))
                }
            }
            return
        }
        val diff = withContext(dispatchers.default) { current.diffTo(next.value) }
        preview.update { Preview(from, snapshot.value) }
        _state.update { it.copy(diff = DiffState.Ready(title, diff, pendingChanges = it.changedCount)) }
    }

    /** «Загрузить» в «Что изменится»: значения конфигурации — в сессию блока (в блок — только по «Записать»). */
    fun onDiffConfirm() {
        val ready = _state.value.diff as? DiffState.Ready ?: return
        // Снимок забирается и флаг ставится уже в запущенной работе: без связи work не стартует — сравнение остаётся.
        work {
            val snapshot = preview.getAndUpdate { null }?.snapshot ?: return@work
            apply(snapshot, ready.diff.total)
        }
    }

    /** Флаг и снимок снимаются в finally: работу с блоком отменяет обрыв связи (в т.ч. пока ждёт lock). */
    private suspend fun apply(snapshot: SettingsSource.File, total: Int) {
        _state.update { it.copy(diff = null, isApplying = true) }
        try {
            executeWithProceedLock(needSkipAnother = false) { copyIntoSource(snapshot, total) }
        } finally {
            discard(snapshot)
            _state.update { it.copy(isApplying = false) }
        }
    }

    private suspend fun copyIntoSource(snapshot: SettingsSource.File, total: Int) {
        val response = withContext(dispatchers.io) {
            repository.copy(from = snapshot, to = source, saveChanges = false)
        }
        when (response) {
            is Response.Error -> {
                logger.w(response.exception) { "Configuration load failed: ${response.message}" }
                val kind = if (response.exception is SettingsMismatchException) {
                    LoadErrorKind.MISMATCH
                } else {
                    LoadErrorKind.OTHER
                }
                _state.update { it.copy(dialog = SettingsDialog.LoadError(kind)) }
            }

            is Response.Success -> {
                val current = currentRoot()
                val reread = withContext(dispatchers.io) { repository.open(source, reload = false) }.getValueIfSuccess()
                val root = reread ?: response.value
                withContext(dispatchers.default) { changes.markLoaded(root) }
                showMenu(current?.let { root.reusing(it) } ?: root)
                _effect.tryEmit(DeviceSettingsEffect.Loaded(total))
            }
        }
    }

    fun onDiffDismiss() {
        _state.update { it.copy(diff = null) }
        discardPreview()
    }

    private fun discardPreview() {
        preview.getAndUpdate { null }?.let { discard(it.snapshot) }
    }

    /** Удалить снимок в app-скоупе: переживает уход с экрана. */
    private fun discard(snapshot: SettingsSource.File) {
        appScope.launch(dispatchers.io) { repository.discardSnapshot(snapshot) }
    }

    // ── Сохранение (K6) и конфигурации (K5) ───────────────────────────────────────────────────────────────────

    /** Действие панели «Изменено: N · …». */
    fun onBarAction() {
        when (_state.value.barAction) {
            BarAction.WRITE -> onWrite()
            BarAction.SAVE -> onSaveRequest(exitAfter = false)
            BarAction.SAVE_AS -> onSaveToRequest(exitAfter = false)
        }
    }

    /** «Сохранить в…»: «Мои конфигурации» / файл. */
    fun onSaveToRequest(exitAfter: Boolean = false) {
        if (currentRoot() == null) return
        _state.update { it.copy(dialog = SettingsDialog.SaveTargets(exitAfter)) }
    }

    fun onSaveTargetPick(target: SaveTarget) {
        val exitAfter = (_state.value.dialog as? SettingsDialog.SaveTargets)?.exitAfter ?: false
        val deviceName = deviceModel() ?: return
        _state.update { it.copy(dialog = null) }
        when (target) {
            SaveTarget.MINE -> _state.update {
                it.copy(saveForm = SaveForm(defaultConfigurationName(), "", deviceName, exitAfter))
            }

            SaveTarget.FILE -> saving { exportFile(exitAfter) }
        }
    }

    /** «Сохранить в мои» из меню конфигурации. */
    fun onSaveToMineRequest() {
        val deviceName = deviceModel() ?: return
        _state.update { it.copy(saveForm = SaveForm(defaultConfigurationName(), "", deviceName)) }
    }

    /** «Сохранить в файл» / «Сохранить как файл…» из меню. */
    fun onExportRequest() {
        saving { exportFile(exitAfter = false) }
    }

    fun onSaveFormChange(name: String, description: String) {
        _state.update { state ->
            state.copy(
                saveForm = state.saveForm?.copy(
                    name = name.take(LocalConfiguration.NAME_MAX_LENGTH),
                    description = description.take(LocalConfiguration.DESCRIPTION_MAX_LENGTH)
                )
            )
        }
    }

    fun onSaveFormDismiss() = _state.update { it.copy(saveForm = null) }

    fun onSaveFormSubmit() {
        val form = _state.value.saveForm ?: return
        if (form.name.isBlank()) return
        _state.update { it.copy(saveForm = null) }
        saving { saveToMine(form) }
    }

    private suspend fun saveToMine(form: SaveForm) = executeWithProceedLock(needSkipAnother = false) {
        _state.update { it.copy(isSaving = true, dialog = null) }
        val response = try {
            withContext(dispatchers.io) {
                localConfigurations.create(form.name.trim(), form.description.trim(), form.deviceName, from = source)
            }
        } finally {
            _state.update { it.copy(isSaving = false) }
        }
        when (response) {
            is Response.Error -> {
                logger.w(response.exception) { "Save to configurations failed: ${response.message}" }
                _state.update { it.copy(dialog = SettingsDialog.SaveError(SaveRetry.Mine(form))) }
            }

            is Response.Success -> {
                if (savesAsNew()) commit()
                _effect.tryEmit(DeviceSettingsEffect.SavedToMine(response.value))
                if (form.exitAfter) _effect.tryEmit(DeviceSettingsEffect.Exit(ExitTarget.BACK))
            }
        }
    }

    private suspend fun exportFile(exitAfter: Boolean) = executeWithProceedLock(needSkipAnother = false) {
        val deviceName = deviceModel() ?: return@executeWithProceedLock
        _state.update { it.copy(isSaving = true, dialog = null) }
        try {
            when (val response = withContext(dispatchers.io) { repository.snapshot(source, deviceName) }) {
                is Response.Error -> {
                    logger.w(response.exception) { "Settings export failed: ${response.message}" }
                    _state.update { it.copy(dialog = SettingsDialog.SaveError(SaveRetry.File)) }
                }

                is Response.Success -> {
                    export.update { Export(response.value, exitAfter) }
                    _effect.tryEmit(
                        DeviceSettingsEffect.ExportFile(
                            response.value.path,
                            settingsFileName(deviceName, exportTitle())
                        )
                    )
                }
            }
        } finally {
            // Снимок отдан системному сохранению — флаг снимет onFileExported; иначе (ошибка, отмена) — здесь.
            if (export.value == null) _state.update { it.copy(isSaving = false) }
        }
    }

    /** Результат системного сохранения файла (после [DeviceSettingsEffect.ExportFile]). */
    fun onFileExported(result: SettingsFileSaveResult) {
        val done = export.getAndUpdate { null } ?: return
        _state.update { it.copy(isSaving = false) }
        discard(done.snapshot)
        when (result) {
            is SettingsFileSaveResult.Saved -> safeLaunch {
                if (savesAsNew()) commit()
                _effect.tryEmit(DeviceSettingsEffect.FileSaved(result.name))
                if (done.exitAfter) _effect.tryEmit(DeviceSettingsEffect.Exit(ExitTarget.BACK))
            }

            SettingsFileSaveResult.Failed -> _state.update {
                it.copy(dialog = SettingsDialog.SaveError(SaveRetry.File))
            }

            SettingsFileSaveResult.Cancelled -> Unit
        }
    }

    /** «Сохранить» конфигурации: файл — с подтверждением перезаписи. */
    fun onSaveRequest(exitAfter: Boolean) {
        if (!_state.value.source.canSave || source == SettingsSource.Device) return
        if (source is SettingsSource.File) {
            _state.update { it.copy(dialog = SettingsDialog.OverwriteConfirm(exitAfter)) }
        } else {
            saving { saveSource(exitAfter) }
        }
    }

    fun onOverwriteConfirm() {
        val dialog = _state.value.dialog as? SettingsDialog.OverwriteConfirm ?: return
        _state.update { it.copy(dialog = null) }
        saving { saveSource(dialog.exitAfter) }
    }

    private suspend fun saveSource(exitAfter: Boolean) = executeWithProceedLock(needSkipAnother = false) {
        val error = saveToSource { state, inProgress -> state.copy(isSaving = inProgress) }
        if (error != null) {
            logger.w(error.exception) { "Settings save failed: ${error.message}" }
            _state.update { it.copy(dialog = SettingsDialog.SaveError(SaveRetry.Source(exitAfter))) }
            return@executeWithProceedLock
        }
        _effect.tryEmit(DeviceSettingsEffect.Saved)
        if (exitAfter) _effect.tryEmit(DeviceSettingsEffect.Exit(ExitTarget.BACK))
    }

    /** «Повторить» в ошибке сохранения. */
    fun onSaveRetry() {
        val retry = (_state.value.dialog as? SettingsDialog.SaveError)?.retry ?: return
        _state.update { it.copy(dialog = null) }
        when (retry) {
            is SaveRetry.Mine -> saving { saveToMine(retry.form) }
            SaveRetry.File -> saving { exportFile(exitAfter = false) }
            is SaveRetry.Source -> saving { saveSource(retry.exitAfter) }
        }
    }

    /** «Сохранить» в диалоге выхода из конфигурации. */
    fun onExitSave() {
        _state.update { it.copy(dialog = null) }
        when (_state.value.barAction) {
            BarAction.SAVE -> onSaveRequest(exitAfter = true)
            else -> onSaveToRequest(exitAfter = true)
        }
    }

    /** «Создать копию» «моей» конфигурации: [name] — «Копия — …». */
    fun onCopyRequest(name: String) {
        val deviceName = deviceModel() ?: return
        saving { saveToMine(SaveForm(name.take(LocalConfiguration.NAME_MAX_LENGTH), "", deviceName)) }
    }

    /** «Записать в блок» из конфигурации: другая модель — предупреждение. */
    fun onWriteToDeviceRequest() {
        val connected = currentDevice?.systemName ?: return
        val model = source.deviceNameOrNull
        if (model != null && !model.equals(connected, ignoreCase = true)) {
            _state.update {
                it.copy(dialog = SettingsDialog.ModelMismatch(source, model, connected, forWrite = true))
            }
        } else {
            _effect.tryEmit(DeviceSettingsEffect.WriteToDevice(source))
        }
    }

    /** Источник не умеет сохранять себя: после «как…» правки считаются сохранёнными. */
    private fun savesAsNew(): Boolean = _state.value.barAction == BarAction.SAVE_AS

    /** Имя новой конфигурации по умолчанию: имя файла — без расширения и с пробелами вместо `_`. */
    private fun defaultConfigurationName(): String = when (source) {
        is SettingsSource.File -> source.name.withoutSettingsExtension().replace('_', ' ')
        else -> source.displayTitle
    }.take(LocalConfiguration.NAME_MAX_LENGTH)

    /** Часть имени экспортируемого файла после модели: у блока — его номер, у прочих — название источника. */
    private fun exportTitle(): String? = when (source) {
        SettingsSource.Device -> _state.value.header?.deviceId?.toString()
        else -> source.displayTitle.withoutSettingsExtension().ifEmpty { null }
    }

    /** Модель блока для снимков и «Моих»: у блока — из сессии, у конфигураций — из источника. */
    private fun deviceModel(): String? = source.deviceNameOrNull ?: _state.value.header?.deviceName

    /** Меню «⋯» → «Сбросить к заводским»: сначала подтверждение. */
    fun onResetRequest() {
        if (!_state.value.canResetToFactory) return
        _state.update { it.copy(dialog = SettingsDialog.ResetConfirm) }
    }

    /** Подтверждён сброс (или «Повторить» после ошибки): после успеха меню перечитывается из блока. */
    fun onResetConfirm() {
        val dialog = _state.value.dialog
        if (dialog != SettingsDialog.ResetConfirm && dialog != SettingsDialog.ResetError) return
        // Диалог закрывается сразу: повторный тап не поставит в очередь второй сброс.
        _state.update { it.copy(dialog = null) }
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
                is Response.Error -> {
                    logger.w(response.exception) { "Factory reset failed: ${response.message}" }
                    _state.update { it.copy(dialog = SettingsDialog.ResetError) }
                }

                is Response.Success -> done = true
            }
        }
        if (done) {
            load()
            _effect.tryEmit(DeviceSettingsEffect.ResetDone)
        }
    }

    /** Новое меню вместе с изменёнными настройками — одним обновлением state (без кадра со старой подсветкой). */
    private suspend fun showMenu(root: SettingsSection) {
        val settings = withContext(dispatchers.default) { root.settings() }
        val changed = changesOf(root, settings)
        _state.update {
            it.copy(menu = LCEState.Content(root), changedIds = changed.ids, sectionChanges = changed.bySection)
        }
        loadCarNames(settings)
    }

    /** Пересчитать изменённые настройки и счётчики разделов. */
    private suspend fun refreshChanged() {
        val root = currentRoot() ?: return
        val changed = changesOf(root, withContext(dispatchers.default) { root.settings() })
        _state.update { it.copy(changedIds = changed.ids, sectionChanges = changed.bySection) }
    }

    /** Изменённые настройки [root] (id) и их число по разделам. */
    private suspend fun changesOf(root: SettingsSection, settings: List<Setting>): SettingsChangeTracker.Changes {
        val unsaved = withContext(dispatchers.io) { repository.unsavedIds(source) }.getValueIfSuccess()
        return withContext(dispatchers.default) { changes.changesOf(root, settings, unsaved) }
    }

    /** Подписи автомобилей к CAN-кодам меню (с сервера, без связи — без подписи). */
    private fun loadCarNames(settings: List<Setting>) {
        val known = _state.value.cars.keys
        val wanted = settings.filterIsInstance<CanCodeSetting>()
            .mapNotNullTo(mutableSetOf()) { it.value.value.carId } - known
        if (wanted.isEmpty()) return
        val alreadyRequested = requestedCarIds.getAndUpdate { it + wanted }
        (wanted - alreadyRequested)
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

    private fun currentRoot(): SettingsSection? = _state.value.menu.content

    /**
     * Сохранение (в «Мои», в файл, источника): одно за раз. Флаг «сохраняем» ставится сразу и атомарно — повторный тап
     * до первой приостановки работы его застанет; снимается по окончании, при отмене и если работа не стартовала.
     * Экспорт держит флаг до ответа системного сохранения ([onFileExported]).
     */
    private fun saving(block: suspend () -> Unit) {
        if (!_state.updateIf({ !it.isSaving }) { it.copy(isSaving = true) }) return
        val job = work {
            try {
                block()
            } finally {
                if (export.value == null) _state.update { it.copy(isSaving = false) }
            }
        }
        if (job == null) _state.update { it.copy(isSaving = false) }
    }

    /** Работа с источником: для блока — только при связи (отменяется при обрыве), иначе — в скоупе VM. */
    private fun work(block: suspend CoroutineScope.() -> Unit): Job? =
        if (followsDevice) launchOnDevice(block) else safeLaunch(block = block)

    override fun onCleared() {
        val leftovers = listOfNotNull(preview.value?.snapshot, export.value?.snapshot)
        appScope.launch(dispatchers.io) {
            repository.close(source)
            leftovers.forEach { repository.discardSnapshot(it) }
        }
        super.onCleared()
    }

    private data class Preview(val from: SettingsSource, val snapshot: SettingsSource.File)

    private data class Export(val snapshot: SettingsSource.File, val exitAfter: Boolean)
}

/** Шапка источника; название заводских настроек и подписи — в UI. */
private fun SettingsSource.header(): SourceHeader {
    val kind = when (this) {
        SettingsSource.Device -> return SourceHeader.Device
        is SettingsSource.File -> SourceKind.FILE
        is SettingsSource.LocalConfiguration -> SourceKind.MINE
        is SettingsSource.RemoteConfiguration -> SourceKind.CLOUD
        is SettingsSource.Defaults -> SourceKind.DEFAULTS
    }
    val capabilities = capabilities
    return SourceHeader(kind, displayTitle, deviceNameOrNull.orEmpty(), capabilities.isReadOnly, capabilities.canSave)
}

/** Название источника: файл — имя файла, конфигурации — название; у блока и заводских — пусто (подпись в UI). */
private val SettingsSource.displayTitle: String
    get() = when (this) {
        is SettingsSource.File -> name
        is SettingsSource.LocalConfiguration -> title
        is SettingsSource.RemoteConfiguration -> title
        SettingsSource.Device, is SettingsSource.Defaults -> ""
    }
