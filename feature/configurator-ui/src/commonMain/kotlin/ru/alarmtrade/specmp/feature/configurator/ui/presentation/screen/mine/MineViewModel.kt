package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.reusing
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.viewmodel.LegacyBaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.settingsFileName

/**
 * K1 «Мои конфигурации»: список (новые сверху, показ порциями), поиск по названию и описанию и фильтр по модели —
 * по загруженному списку; действия карточки: название и описание, копия, сохранение в файл, удаление.
 *
 * [pickDeviceName] — режим выбора для загрузки в редактор блока: фильтр по модели блока включён сразу (его можно
 * снять — другая модель загрузится с предупреждением).
 */
internal class MineViewModel(
    pickDeviceName: String?,
    private val configurations: LocalConfigurationsRepository,
    private val settings: DeviceSettingsRepository,
    deviceSession: DeviceSession,
    private val appScope: CoroutineScope,
    private val dispatchers: AppDispatchers,
) : LegacyBaseDeviceViewModel(deviceSession) {

    private val _state = MutableStateFlow(MineState(modelFilter = pickDeviceName, pickMode = pickDeviceName != null))

    private val retryRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Список — одним потоком загрузок: первая при подписке на state, затем на каждое изменение хранилища и «Повторить».
    // collectLatest отменяет устаревшую загрузку — старый ответ не затрёт новый.
    val state: StateFlow<MineState> = combine(_state, connectedDeviceName) { state, connected ->
        state.copy(connectedDeviceName = connected).withLists()
    }
        .launchOnFirstStart {
            merge(configurations.changes, retryRequests).onStart { emit(Unit) }.collectLatest { load() }
        }
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), _state.value)

    private val _effect = sideEffectFlow<MineEffect>()
    val effect: SharedFlow<MineEffect> = _effect.asSharedFlow()

    /** Снимок «Сохранить в файл», отданный системному сохранению. */
    private val export = MutableStateFlow<SettingsSource.File?>(null)

    private suspend fun load() {
        if (_state.value.list !is LCEState.Content) _state.update { it.copy(list = LCEState.Loading) }
        when (val response = withContext(dispatchers.io) { configurations.all() }) {
            is Response.Error -> _state.update {
                it.copy(list = LCEState.Error(ConfiguratorError.from(response.exception)))
            }

            is Response.Success -> _state.update { state ->
                // Список перечитывается на каждое изменение хранилища: неизменённые карточки — прежние экземпляры.
                val items = response.value.reusing(state.list.content) { it.id }
                state.copy(
                    list = LCEState.Content(items),
                    selectedId = state.selectedId?.takeIf { id -> items.any { it.id == id } },
                )
            }
        }
    }

    fun onRetry() {
        retryRequests.tryEmit(Unit)
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query, shown = PAGE_SIZE) }

    fun onModelFilter(deviceName: String?) = _state.update { it.copy(modelFilter = deviceName, shown = PAGE_SIZE) }

    fun onResetFilter() = _state.update { it.copy(query = "", modelFilter = null, shown = PAGE_SIZE) }

    /** Прокрутка к концу показанного — следующая порция. */
    fun onLoadMore() = _state.update { it.copy(shown = it.shown + PAGE_SIZE) }

    /** Выбор в списке (широкое окно — карточка справа). */
    fun onSelect(id: Long) = _state.update { it.copy(selectedId = id) }

    fun onMenu(item: LocalConfiguration) = _state.update { it.copy(dialog = MineDialog.Menu(item)) }

    fun onCreateRequest() = _state.update { it.copy(dialog = MineDialog.Create) }

    fun onDialogDismiss() = _state.update { it.copy(dialog = null) }

    fun onRenameRequest(item: LocalConfiguration) = _state.update {
        it.copy(dialog = MineDialog.Rename(item, item.name, item.description))
    }

    fun onRenameChange(name: String, description: String) = _state.update { state ->
        val dialog = state.dialog as? MineDialog.Rename ?: return@update state
        state.copy(dialog = dialog.copy(name = name, description = description))
    }

    fun onRenameSubmit() {
        val dialog = _state.value.dialog as? MineDialog.Rename ?: return
        if (dialog.name.isBlank()) return
        run(MineMessage.RENAMED) {
            configurations.updateInfo(dialog.item, dialog.name.trim(), dialog.description.trim()).also { response ->
                if (response is Response.Success) _state.update { it.copy(selectedId = response.value.id) }
            }
        }
    }

    /** «Создать копию»: [name] — «Копия — …». */
    fun onCopy(item: LocalConfiguration, name: String) {
        run(MineMessage.COPIED) {
            val source = item.toSource()
            try {
                configurations.create(
                    name.take(LocalConfiguration.NAME_MAX_LENGTH),
                    item.description,
                    item.deviceName,
                    from = source,
                )
            } finally {
                settings.close(source)
            }
        }
    }

    fun onDeleteRequest(item: LocalConfiguration) = _state.update { it.copy(dialog = MineDialog.Delete(item)) }

    fun onDeleteConfirm() {
        val item = (_state.value.dialog as? MineDialog.Delete)?.item ?: return
        run(MineMessage.DELETED) { configurations.delete(item.id) }
    }

    /** «Сохранить в файл»: снимок записи во временный `.dxs` → системное сохранение. */
    fun onExport(item: LocalConfiguration) {
        if (!startBusy()) return
        safeLaunch {
            try {
                export(item)
            } finally {
                // Снимок отдан системному сохранению — флаг снимет onFileExported; иначе (ошибка, отмена) — здесь.
                if (export.value == null) _state.update { it.copy(busy = false) }
            }
        }
    }

    private suspend fun export(item: LocalConfiguration) {
        val source = item.toSource()
        val response = withContext(dispatchers.io) {
            try {
                settings.snapshot(source, item.deviceName)
            } finally {
                settings.close(source)
            }
        }
        when (response) {
            is Response.Error -> {
                logger.w(response.exception) { "Configuration export failed: ${response.message}" }
                _effect.tryEmit(MineEffect.Message(MineMessage.FAILED))
            }

            is Response.Success -> {
                export.update { response.value }
                _effect.tryEmit(
                    MineEffect.ExportFile(response.value.path, settingsFileName(item.deviceName, item.name)),
                )
            }
        }
    }

    /**
     * Действие с записью — одно за раз: «занято» ставится сразу и атомарно, повторный тап (копия, экспорт, удаление)
     * его застанет и ничего не сделает. `false` — уже занято.
     */
    private fun startBusy(): Boolean = _state.updateIf({ !it.busy }) { it.copy(dialog = null, busy = true) }

    fun onFileExported(result: SettingsFileSaveResult) {
        val snapshot = export.getAndUpdate { null } ?: return
        _state.update { it.copy(busy = false) }
        appScope.launch(dispatchers.io) { settings.discardSnapshot(snapshot) }
        when (result) {
            is SettingsFileSaveResult.Saved -> _effect.tryEmit(MineEffect.FileSaved(result.name))
            SettingsFileSaveResult.Failed -> _effect.tryEmit(MineEffect.Message(MineMessage.FAILED))
            SettingsFileSaveResult.Cancelled -> Unit
        }
    }

    private fun run(success: MineMessage, block: suspend () -> Response<*>) {
        if (!startBusy()) return
        safeLaunch {
            val response = try {
                withContext(dispatchers.io) { block() }
            } finally {
                _state.update { it.copy(busy = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Configuration action failed: ${response.message}" }
                    _effect.tryEmit(MineEffect.Message(MineMessage.FAILED))
                }

                is Response.Success -> {
                    val created = response.value as? SettingsSource.LocalConfiguration
                    _effect.tryEmit(
                        if (success == MineMessage.COPIED && created != null) {
                            MineEffect.Copied(created)
                        } else {
                            MineEffect.Message(success)
                        },
                    )
                }
            }
        }
    }

    override fun onCleared() {
        export.value?.let { snapshot -> appScope.launch(dispatchers.io) { settings.discardSnapshot(snapshot) } }
        super.onCleared()
    }

    companion object {
        const val PAGE_SIZE = 20
        private val STOP_TIMEOUT = 5.seconds
    }
}
