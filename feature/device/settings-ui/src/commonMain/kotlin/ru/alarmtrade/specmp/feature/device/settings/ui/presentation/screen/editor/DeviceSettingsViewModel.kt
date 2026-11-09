package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFilePickResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.settingsFileName
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
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
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.SettingsError
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.model.opensEditor
import ru.alarmtrade.specmp.feature.vehicle.domain.model.CarSummary
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.repository.VehicleCatalogRepository

/**
 * Изменённые — пересечение `unsavedIds` SDK с отличающимися от снимка «как было» ([SettingsChangeTracker]). Одна VM:
 * правки, запись, сброс, загрузка и сохранение идут через одну сессию SDK под общим `executeWithProceedLock`.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class DeviceSettingsViewModel(
    private val source: SettingsSource,
    options: SettingsEditorOptions,
    private val repository: DeviceSettingsRepository,
    private val sessions: DeviceSettingsSessionController,
    private val localConfigurations: LocalConfigurationsRepository,
    private val preferences: DeviceSettingsPreferences,
    private val carsRepository: VehicleCatalogRepository,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<DeviceSettingsState, DeviceSettingsEffect>(
    deviceSession,
    initialSettingsState(source, options, preferences.treeWidthDp.value.getValueIfSuccess()),
) {

    private val followsDevice = source.capabilities.followsDeviceConnection

    // Блок перечитывается на каждое (пере)подключение (живёт всю жизнь VM); конфигурация/файл — одно чтение.
    override suspend fun onStart() {
        connectedDeviceName.collectIntoState { copy(connectedDeviceName = it) }
        searchResults().collectIntoState { (query, hits) -> copy(searchQuery = query, searchHits = hits) }
        if (followsDevice) onEachDeviceReady(::onDeviceReady) else load()
    }

    private fun searchResults(): Flow<Pair<String, List<SettingsSearchHit>>> =
        combine(select { searchInput }, select { menu.content }) { query, root -> query to root }
            .debounce(SEARCH_DEBOUNCE_MILLIS)
            .mapLatest { (query, root) -> query to root?.search(query).orEmpty() }
            .flowOn(dispatchers.default)

    private suspend fun onDeviceReady(device: AlarmDevice) {
        reduce { copy(header = SettingsHeader(device.systemName, device.systemId, device.can?.code)) }
        load()
    }

    private suspend fun load() {
        readMenu()
        if (currentState.menu !is LCEState.Content) return
        val pending = currentState.pendingLoad ?: return
        reduce { copy(pendingLoad = null) }
        requestLoad(pending, closeAfter = false)
    }

    private suspend fun readMenu() = executeWithProceedLock(needSkipAnother = false) {
        discardPreview()
        reduce { withMenuLoading() }
        when (val response = withContext(dispatchers.io) { repository.open(source, reload = true) }) {
            is Response.Error -> {
                logger.w(response.exception) { "Settings menu read failed: ${response.message}" }
                reduce { copy(menu = LCEState.Error(SettingsError.from(response.exception))) }
            }

            is Response.Success -> {
                val root = response.value
                updateChanges { withBaseline(root) }
                showMenu(root)
            }
        }
    }

    fun onRetry() {
        work { load() }
    }

    fun onValueChange(settingId: Int, value: SettingValue) {
        work { applyValue(settingId, value) }
    }

    private suspend fun applyValue(settingId: Int, value: SettingValue) = executeWithProceedLock(
        needSkipAnother = false,
    ) {
        when (val response = withContext(dispatchers.io) { repository.setValue(source, settingId, value) }) {
            is Response.Error -> {
                logger.w(response.exception) { "Setting $settingId change failed: ${response.message}" }
                postSideEffect(DeviceSettingsEffect.SetValueFailed)
            }

            is Response.Success -> {
                // Правка может поменять соседние настройки: перечитываем меню из кэша сессии SDK (без опроса блока);
                // не вышло — заменяем только эту. Неизменённые узлы — из текущего меню, их строки не перерисовываются.
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
        if (setting is CanCodeSetting && currentState.header?.singleCanCode != null) {
            reduce { copy(dialog = SettingsDialog.SingleCanFirmware) }
            return
        }
        val original = currentState.changes.original(settingId) ?: setting.value
        reduce { copy(editing = EditingSetting(setting, original = original, draft = setting.value)) }
    }

    fun onDraftChange(draft: SettingValue) {
        reduce { withDraft(draft) }
    }

    fun onCarSearch() {
        val editing = currentState.editing ?: return
        val code = (editing.draft as? SettingValue.Can)?.value?.code ?: return
        if (!editing.canApply) return
        reduce { copy(editing = editing.copy(carSearch = LCEState.Loading)) }
        intent {
            val response = withContext(dispatchers.io) { carsRepository.findByCanCode(code, vehicleDevice()) }
            reduce { withCarSearch(code, response) }
        }
    }

    fun onCarPick(carId: Int) {
        reduce { withCarPicked(carId) }
    }

    fun onVehiclePickerOpen() {
        if (currentState.editing?.draft is SettingValue.Can) reduce { copy(vehiclePicker = true) }
    }

    fun onVehiclePickerClose() {
        reduce { copy(vehiclePicker = false) }
    }

    fun onCarPicked(car: CarSummary) {
        reduce { withCatalogCar(car) }
    }

    fun onEditReset() {
        reduce { copy(editing = editing?.let { it.copy(draft = it.original) }) }
    }

    fun onEditApply() {
        val editing = currentState.editing ?: return
        if (!editing.canApply) return
        reduce { copy(editing = null) }
        if (editing.draft != editing.setting.value) onValueChange(editing.setting.id, editing.draft)
    }

    fun onEditDismiss() {
        reduce { copy(editing = null) }
    }

    fun onDescriptionClick(nodeId: Int) {
        val node = currentRoot()?.findNode(nodeId) ?: return
        reduce { copy(dialog = SettingsDialog.Description(node)) }
    }

    fun onDialogDismiss() {
        reduce { copy(dialog = null) }
    }

    fun onTreeWidthChange(widthDp: Int) {
        reduce { copy(treeWidthDp = widthDp) }
        intent {
            withContext(dispatchers.io) { preferences.setTreeWidthDp(widthDp) }
                .onError { logger.w(it) { "Tree width was not saved" } }
        }
    }

    fun onSearchQueryChange(query: String) {
        reduce { copy(searchInput = query) }
    }

    fun onExitRequest(target: ExitTarget = ExitTarget.BACK) {
        val dialog = currentState.exitDialog(target)
        reduce { copy(dialog = dialog) }
        if (dialog == null) postSideEffect(DeviceSettingsEffect.Exit(target))
    }

    fun onSingleCanUpdate() {
        onExitRequest(ExitTarget.FIRMWARE_UPDATE)
    }

    fun onExitDiscard() {
        val target = takeExitTarget() ?: return
        postSideEffect(DeviceSettingsEffect.Exit(target))
    }

    /** Диалог выхода забирается сразу: повторный тап его уже не застанет и не запишет/не выйдет второй раз. */
    private fun takeExitTarget(): ExitTarget? {
        val target = currentState.dialog.exitTarget() ?: return null
        reduce { copy(dialog = null) }
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
        val count = currentState.changedCount
        if (count == 0 && exitTo == null) return@executeWithProceedLock
        val error = saveToSource { state, inProgress -> state.copy(isWriting = inProgress) }
        if (error != null) {
            logger.w(error.exception) { "Settings write failed: ${error.message}" }
            reduce { copy(dialog = SettingsDialog.WriteError) }
            return@executeWithProceedLock
        }
        postSideEffect(DeviceSettingsEffect.Written(count))
        exitTo?.let { postSideEffect(DeviceSettingsEffect.Exit(it)) }
    }

    private suspend fun saveToSource(progress: (DeviceSettingsState, Boolean) -> DeviceSettingsState): Response.Error? {
        reduce { progress(this, true).copy(dialog = null) }
        val response = try {
            withContext(dispatchers.io) { repository.save(source) }
        } finally {
            reduce { progress(this, false) }
        }
        if (response is Response.Success) commit()
        return response as? Response.Error
    }

    private suspend fun commit() {
        val root = currentRoot()
        updateChanges { (if (root != null) withBaseline(root) else this).withoutLoaded() }
        refreshChanged()
    }

    fun onLoadRequest() {
        if (source != SettingsSource.Device || currentRoot() == null) return
        reduce { copy(dialog = SettingsDialog.LoadSources) }
    }

    fun onLoadSourcePick(kind: LoadSourceKind) {
        val deviceName = deviceModel() ?: return
        reduce { copy(dialog = null) }
        when (kind) {
            LoadSourceKind.MINE, LoadSourceKind.CLOUD -> reduce { withConfigurationPicker(kind, deviceName) }
            LoadSourceKind.FILE -> postSideEffect(DeviceSettingsEffect.PickFile)
            LoadSourceKind.DEFAULTS -> requestLoad(SettingsSource.Defaults(deviceName), closeAfter = true)
        }
    }

    fun onConfigurationPicked(picked: SettingsSource) {
        reduce { copy(configurationPicker = null) }
        requestLoad(picked, closeAfter = true)
    }

    fun onConfigurationPickerClose() {
        reduce { copy(configurationPicker = null) }
    }

    fun onFilePicked(result: SettingsFilePickResult) {
        when (result) {
            is SettingsFilePickResult.Picked -> {
                val deviceName = deviceModel() ?: return
                requestLoad(
                    SettingsSource.File(result.path, deviceName, name = result.name, inPlace = result.inPlace),
                    closeAfter = true,
                )
            }

            SettingsFilePickResult.Failed -> reduce { copy(dialog = SettingsDialog.LoadError(LoadErrorKind.FILE)) }

            SettingsFilePickResult.Cancelled -> Unit
        }
    }

    private fun requestLoad(from: SettingsSource, closeAfter: Boolean) {
        val deviceName = deviceModel() ?: return
        val sourceDeviceName = from.deviceNameOrNull
        if (sourceDeviceName != null && !sourceDeviceName.equals(deviceName, ignoreCase = true)) {
            reduce {
                copy(
                    dialog = SettingsDialog.ModelMismatch(
                        from,
                        sourceDeviceName,
                        deviceName,
                        forWrite = false,
                        closeAfter = closeAfter,
                    ),
                )
            }
            return
        }
        startPreview(from, closeAfter, notStarted = null)
    }

    /**
     * «Сравниваем…» — сразу, работа — в очереди за правками (не пропуск: иначе выбор, пока применяется значение, молча
     * терялся). Без связи работа не стартует — показываем [notStarted].
     */
    private fun startPreview(from: SettingsSource, closeAfter: Boolean, notStarted: DiffState?) {
        if (currentState.diff is DiffState.Busy) return
        reduce { copy(diff = DiffState.Busy(from.displayTitle), dialog = null) }
        work { preview(from, closeAfter) } ?: reduce { copy(diff = notStarted) }
    }

    fun onMismatchContinue() {
        val dialog = currentState.dialog as? SettingsDialog.ModelMismatch ?: return
        reduce { copy(dialog = null) }
        if (dialog.forWrite) {
            postSideEffect(DeviceSettingsEffect.WriteToDevice(source))
        } else {
            startPreview(dialog.source, dialog.closeAfter, notStarted = null)
        }
    }

    fun onDiffRetry() {
        val error = currentState.diff as? DiffState.Error ?: return
        startPreview(error.from, error.closeAfter, notStarted = error)
    }

    /** «Сравниваем…» снимается и при отмене (обрыв связи, в т.ч. пока работа ждёт lock) и раннем выходе. */
    private suspend fun preview(from: SettingsSource, closeAfter: Boolean) {
        try {
            executeWithProceedLock(needSkipAnother = false) { previewLocked(from, closeAfter) }
        } finally {
            reduce { if (diff is DiffState.Busy) copy(diff = null) else this }
        }
    }

    private suspend fun previewLocked(from: SettingsSource, closeAfter: Boolean) {
        val deviceName = deviceModel() ?: return
        val current = currentRoot() ?: return
        val title = from.displayTitle
        discardPreview()
        val snapshot = withContext(dispatchers.io) { repository.snapshot(from, deviceName) }
        if (closeAfter && from != source) {
            withContext(dispatchers.io) {
                repository.close(from).onError { logger.w(it) { "Failed to close the source session" } }
            }
        }
        val next = (snapshot as? Response.Success)?.let {
            withContext(dispatchers.io) { repository.open(it.value, reload = true) }
        }
        if (snapshot !is Response.Success || next !is Response.Success) {
            (snapshot as? Response.Error)?.let {
                logger.w(it.exception) { "Configuration snapshot failed: ${it.message}" }
            }
            (next as? Response.Error)?.let { logger.w(it.exception) { "Configuration read failed: ${it.message}" } }
            (snapshot as? Response.Success)?.let { sessions.discardSnapshot(it.value) }
            reduce { withPreviewFailed(from, closeAfter) }
            return
        }
        val diff = withContext(dispatchers.default) { current.diffTo(next.value) }
        reduce {
            copy(previewSnapshot = snapshot.value, diff = DiffState.Ready(title, diff, pendingChanges = changedCount))
        }
    }

    fun onDiffConfirm() {
        val ready = currentState.diff as? DiffState.Ready ?: return
        // Снимок забирается и флаг ставится уже в запущенной работе: без связи work не стартует — сравнение остаётся.
        work {
            val snapshot = currentState.previewSnapshot ?: return@work
            reduce { copy(previewSnapshot = null) }
            apply(snapshot, ready.diff.total)
        }
    }

    /** Флаг и снимок снимаются в finally: работу с блоком отменяет обрыв связи (в т.ч. пока ждёт lock). */
    private suspend fun apply(snapshot: SettingsSource.File, total: Int) {
        reduce { copy(diff = null, isApplying = true) }
        try {
            executeWithProceedLock(needSkipAnother = false) { copyIntoSource(snapshot, total) }
        } finally {
            sessions.discardSnapshot(snapshot)
            reduce { copy(isApplying = false) }
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
                reduce { copy(dialog = SettingsDialog.LoadError(kind)) }
            }

            is Response.Success -> {
                val current = currentRoot()
                val reread = withContext(dispatchers.io) { repository.open(source, reload = false) }.getValueIfSuccess()
                val root = reread ?: response.value
                updateChanges { withLoaded(root) }
                showMenu(current?.let { root.reusing(it) } ?: root)
                postSideEffect(DeviceSettingsEffect.Loaded(total))
            }
        }
    }

    fun onDiffDismiss() {
        reduce { copy(diff = null) }
        discardPreview()
    }

    /** Снимок удаляет репозиторий в фоне приложения: удаление переживает уход с экрана. */
    private fun discardPreview() {
        val snapshot = currentState.previewSnapshot ?: return
        reduce { copy(previewSnapshot = null) }
        sessions.discardSnapshot(snapshot)
    }

    fun onBarAction() {
        when (currentState.barAction) {
            BarAction.WRITE -> onWrite()
            BarAction.SAVE -> onSaveRequest(exitAfter = false)
            BarAction.SAVE_AS -> onSaveToRequest(exitAfter = false)
        }
    }

    fun onSaveToRequest(exitAfter: Boolean = false) {
        if (currentRoot() == null) return
        reduce { copy(dialog = SettingsDialog.SaveTargets(exitAfter)) }
    }

    fun onSaveTargetPick(target: SaveTarget) {
        val exitAfter = (currentState.dialog as? SettingsDialog.SaveTargets)?.exitAfter ?: false
        val deviceName = deviceModel() ?: return
        reduce { copy(dialog = null) }
        when (target) {
            SaveTarget.MINE -> {
                val form = SaveForm(source.defaultConfigurationName(), "", deviceName, exitAfter)
                reduce { copy(saveForm = form) }
            }

            SaveTarget.FILE -> saving { exportFile(exitAfter) }
        }
    }

    fun onSaveToMineRequest() {
        val deviceName = deviceModel() ?: return
        val form = SaveForm(source.defaultConfigurationName(), "", deviceName)
        reduce { copy(saveForm = form) }
    }

    fun onExportRequest() {
        saving { exportFile(exitAfter = false) }
    }

    fun onSaveFormChange(name: String, description: String) {
        reduce { withSaveForm(name, description) }
    }

    fun onSaveFormDismiss() {
        reduce { copy(saveForm = null) }
    }

    fun onSaveFormSubmit() {
        val form = currentState.saveForm ?: return
        if (form.name.isBlank()) return
        reduce { copy(saveForm = null) }
        saving { saveToMine(form) }
    }

    private suspend fun saveToMine(form: SaveForm) = executeWithProceedLock(needSkipAnother = false) {
        reduce { copy(isSaving = true, dialog = null) }
        val response = try {
            withContext(dispatchers.io) {
                localConfigurations.create(form.name.trim(), form.description.trim(), form.deviceName, from = source)
            }
        } finally {
            reduce { copy(isSaving = false) }
        }
        when (response) {
            is Response.Error -> {
                logger.w(response.exception) { "Save to configurations failed: ${response.message}" }
                reduce { copy(dialog = SettingsDialog.SaveError(SaveRetry.Mine(form))) }
            }

            is Response.Success -> {
                if (savesAsNew()) commit()
                postSideEffect(DeviceSettingsEffect.SavedToMine(response.value))
                if (form.exitAfter) postSideEffect(DeviceSettingsEffect.Exit(ExitTarget.BACK))
            }
        }
    }

    private suspend fun exportFile(exitAfter: Boolean) = executeWithProceedLock(needSkipAnother = false) {
        val deviceName = deviceModel() ?: return@executeWithProceedLock
        reduce { copy(isSaving = true, dialog = null) }
        try {
            when (val response = withContext(dispatchers.io) { repository.snapshot(source, deviceName) }) {
                is Response.Error -> {
                    logger.w(response.exception) { "Settings export failed: ${response.message}" }
                    reduce { copy(dialog = SettingsDialog.SaveError(SaveRetry.File)) }
                }

                is Response.Success -> {
                    reduce { copy(pendingExport = PendingExport(response.value, exitAfter)) }
                    val fileName = settingsFileName(deviceName, source.exportTitle(currentState.header))
                    postSideEffect(DeviceSettingsEffect.ExportFile(response.value.path, fileName))
                }
            }
        } finally {
            // Снимок отдан системному сохранению — флаг снимет onFileExported; иначе (ошибка, отмена) — здесь.
            if (currentState.pendingExport == null) reduce { copy(isSaving = false) }
        }
    }

    fun onFileExported(result: SettingsFileSaveResult) {
        val done = currentState.pendingExport ?: return
        reduce { copy(pendingExport = null, isSaving = false) }
        sessions.discardSnapshot(done.snapshot)
        when (result) {
            is SettingsFileSaveResult.Saved -> intent {
                if (savesAsNew()) commit()
                postSideEffect(DeviceSettingsEffect.FileSaved(result.name))
                if (done.exitAfter) postSideEffect(DeviceSettingsEffect.Exit(ExitTarget.BACK))
            }

            SettingsFileSaveResult.Failed -> reduce { copy(dialog = SettingsDialog.SaveError(SaveRetry.File)) }

            SettingsFileSaveResult.Cancelled -> Unit
        }
    }

    fun onSaveRequest(exitAfter: Boolean) {
        if (!currentState.source.canSave || source == SettingsSource.Device) return
        if (source is SettingsSource.File) {
            reduce { copy(dialog = SettingsDialog.OverwriteConfirm(exitAfter)) }
        } else {
            saving { saveSource(exitAfter) }
        }
    }

    fun onOverwriteConfirm() {
        val dialog = currentState.dialog as? SettingsDialog.OverwriteConfirm ?: return
        reduce { copy(dialog = null) }
        saving { saveSource(dialog.exitAfter) }
    }

    private suspend fun saveSource(exitAfter: Boolean) = executeWithProceedLock(needSkipAnother = false) {
        val error = saveToSource { state, inProgress -> state.copy(isSaving = inProgress) }
        if (error != null) {
            logger.w(error.exception) { "Settings save failed: ${error.message}" }
            reduce { copy(dialog = SettingsDialog.SaveError(SaveRetry.Source(exitAfter))) }
            return@executeWithProceedLock
        }
        postSideEffect(DeviceSettingsEffect.Saved)
        if (exitAfter) postSideEffect(DeviceSettingsEffect.Exit(ExitTarget.BACK))
    }

    fun onSaveRetry() {
        val retry = (currentState.dialog as? SettingsDialog.SaveError)?.retry ?: return
        reduce { copy(dialog = null) }
        when (retry) {
            is SaveRetry.Mine -> saving { saveToMine(retry.form) }
            SaveRetry.File -> saving { exportFile(exitAfter = false) }
            is SaveRetry.Source -> saving { saveSource(retry.exitAfter) }
        }
    }

    fun onExitSave() {
        reduce { copy(dialog = null) }
        when (currentState.barAction) {
            BarAction.SAVE -> onSaveRequest(exitAfter = true)
            else -> onSaveToRequest(exitAfter = true)
        }
    }

    fun onCopyRequest(name: String) {
        val deviceName = deviceModel() ?: return
        saving { saveToMine(SaveForm(name.take(LocalConfiguration.NAME_MAX_LENGTH), "", deviceName)) }
    }

    fun onWriteToDeviceRequest() {
        val connected = currentDevice?.systemName ?: return
        val model = source.deviceNameOrNull
        if (model != null && !model.equals(connected, ignoreCase = true)) {
            val dialog = SettingsDialog.ModelMismatch(source, model, connected, forWrite = true)
            reduce { copy(dialog = dialog) }
        } else {
            postSideEffect(DeviceSettingsEffect.WriteToDevice(source))
        }
    }

    /** Источник не умеет сохранять себя: после «как…» правки считаются сохранёнными. */
    private fun savesAsNew(): Boolean = currentState.barAction == BarAction.SAVE_AS

    private fun deviceModel(): String? = source.deviceNameOrNull ?: currentState.header?.deviceName

    fun onResetRequest() {
        if (!currentState.canResetToFactory) return
        reduce { copy(dialog = SettingsDialog.ResetConfirm) }
    }

    fun onResetConfirm() {
        val dialog = currentState.dialog
        if (dialog != SettingsDialog.ResetConfirm && dialog != SettingsDialog.ResetError) return
        // Диалог закрывается сразу: повторный тап не поставит в очередь второй сброс.
        reduce { copy(dialog = null) }
        work { reset() }
    }

    private suspend fun reset() {
        var done = false
        executeWithProceedLock(needSkipAnother = false) {
            reduce { copy(isResetting = true, dialog = null) }
            val response = try {
                withContext(dispatchers.io) { repository.resetDeviceToFactory() }
            } finally {
                reduce { copy(isResetting = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Factory reset failed: ${response.message}" }
                    reduce { copy(dialog = SettingsDialog.ResetError) }
                }

                is Response.Success -> done = true
            }
        }
        if (done) {
            load()
            postSideEffect(DeviceSettingsEffect.ResetDone)
        }
    }

    /** Новое меню вместе с изменёнными настройками — одним обновлением state (без кадра со старой подсветкой). */
    private suspend fun showMenu(root: SettingsSection) {
        val settings = withContext(dispatchers.default) { root.settings() }
        val changed = changesOf(root, settings)
        reduce { copy(menu = LCEState.Content(root), changedIds = changed.ids, sectionChanges = changed.bySection) }
        loadCarNames(settings)
    }

    private suspend fun refreshChanged() {
        val root = currentRoot() ?: return
        val changed = changesOf(root, withContext(dispatchers.default) { root.settings() })
        reduce { copy(changedIds = changed.ids, sectionChanges = changed.bySection) }
    }

    private suspend fun changesOf(root: SettingsSection, settings: List<Setting>): SettingsChangeTracker.Changes {
        val unsaved = withContext(dispatchers.io) { repository.unsavedIds(source) }.getValueIfSuccess()
        val tracker = currentState.changes
        return withContext(dispatchers.default) { tracker.changesOf(root, settings, unsaved) }
    }

    /** Каждый `carId` — раз за чтение меню: без сети запрос не повторяется на каждую правку. */
    private fun loadCarNames(settings: List<Setting>) {
        val known = currentState.cars.keys + currentState.requestedCarIds
        val wanted = settings.filterIsInstance<CanCodeSetting>().mapNotNull { it.value.value.carId }.toSet() - known
        if (wanted.isEmpty()) return
        reduce { copy(requestedCarIds = requestedCarIds + wanted) }
        wanted.forEach { carId ->
            safeLaunch {
                withContext(dispatchers.io) { carsRepository.summary(carId) }.getValueIfSuccess()?.let { car ->
                    reduce { copy(cars = cars + (car.id to car)) }
                }
            }
        }
    }

    private fun vehicleDevice(): VehicleDeviceFilter? =
        currentState.header?.let { VehicleDeviceFilter.Device(it.deviceName) }

    private fun currentRoot(): SettingsSection? = currentState.menu.content

    private suspend fun updateChanges(transform: SettingsChangeTracker.() -> SettingsChangeTracker) {
        val tracker = currentState.changes
        val next = withContext(dispatchers.default) { tracker.transform() }
        reduce { copy(changes = next) }
    }

    /**
     * Одно сохранение за раз: флаг ставится сразу — повторный тап его застанет; снимается по окончании, при отмене
     * и если работа не стартовала. Экспорт держит флаг до ответа системного сохранения ([onFileExported]).
     */
    private fun saving(block: suspend () -> Unit) {
        if (currentState.isSaving) return
        reduce { copy(isSaving = true) }
        val job = work {
            try {
                block()
            } finally {
                if (currentState.pendingExport == null) reduce { copy(isSaving = false) }
            }
        }
        if (job == null) reduce { copy(isSaving = false) }
    }

    /** Работа с источником: для блока — только при связи (отменяется при обрыве), иначе — в скоупе VM. */
    private fun work(block: suspend CoroutineScope.() -> Unit): Job? =
        if (followsDevice) launchOnDevice(block) else intent(block)

    override fun onCleared() {
        sessions.discardSession(source)
        listOfNotNull(currentState.previewSnapshot, currentState.pendingExport?.snapshot)
            .forEach(sessions::discardSnapshot)
        super.onCleared()
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 150L
    }
}
