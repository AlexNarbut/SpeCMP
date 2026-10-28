package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
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
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private val _state = MutableStateFlow(MineState(modelFilter = pickDeviceName, pickMode = pickDeviceName != null))
    val state: StateFlow<MineState> = _state.asStateFlow()

    private val _effect = sideEffectFlow<MineEffect>()
    val effect: SharedFlow<MineEffect> = _effect.asSharedFlow()

    /** Снимок «Сохранить в файл», отданный системному сохранению. */
    private val export = MutableStateFlow<SettingsSource.File?>(null)

    init {
        safeLaunch { load() }
        configurations.changes.onEach { load() }.launchIn(viewModelScope)
        deviceSession.state
            .map { (it as? DeviceSessionState.Ready)?.device?.systemName }
            .distinctUntilChanged()
            .onEach { name -> _state.update { it.copy(connectedDeviceName = name) } }
            .launchIn(viewModelScope)
    }

    private suspend fun load() {
        if (_state.value.list !is MineList.Ready) _state.update { it.copy(list = MineList.Loading) }
        when (val response = withContext(dispatchers.io) { configurations.all() }) {
            is Response.Error -> {
                logger.w(response.exception) { "Configurations read failed: ${response.message}" }
                _state.update { it.copy(list = MineList.Error) }
            }

            is Response.Success -> _state.update { state ->
                val items = response.value
                state.copy(
                    list = MineList.Ready(items),
                    selectedId = state.selectedId?.takeIf { id -> items.any { it.id == id } }
                )
            }
        }
    }

    fun onRetry() {
        safeLaunch { load() }
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
            configurations.create(
                name.take(LocalConfiguration.NAME_MAX_LENGTH),
                item.description,
                item.deviceName,
                from = source
            ).also { settings.close(source) }
        }
    }

    fun onDeleteRequest(item: LocalConfiguration) = _state.update { it.copy(dialog = MineDialog.Delete(item)) }

    fun onDeleteConfirm() {
        val item = (_state.value.dialog as? MineDialog.Delete)?.item ?: return
        run(MineMessage.DELETED) { configurations.delete(item.id) }
    }

    /** «Сохранить в файл»: снимок записи во временный `.dxs` → системное сохранение. */
    fun onExport(item: LocalConfiguration) {
        _state.update { it.copy(dialog = null, busy = true) }
        safeLaunch {
            val source = item.toSource()
            val response = withContext(dispatchers.io) {
                settings.snapshot(source, item.deviceName).also { settings.close(source) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Configuration export failed: ${response.message}" }
                    _state.update { it.copy(busy = false) }
                    _effect.tryEmit(MineEffect.Message(MineMessage.FAILED))
                }

                is Response.Success -> {
                    export.update { response.value }
                    _effect.tryEmit(
                        MineEffect.ExportFile(response.value.path, settingsFileName(item.deviceName, item.name))
                    )
                }
            }
        }
    }

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
        _state.update { it.copy(dialog = null, busy = true) }
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
                        }
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
    }
}

@Immutable
internal data class MineState(
    val list: MineList = MineList.Loading,
    val query: String = "",
    /** Фильтр по модели блока; `null` — все модели. */
    val modelFilter: String? = null,
    /** Сколько показать (порции при прокрутке). */
    val shown: Int = MineViewModel.PAGE_SIZE,
    /** Выбранная (широкое окно — карточка справа). */
    val selectedId: Long? = null,
    val dialog: MineDialog? = null,
    val busy: Boolean = false,
    /** Модель подключённого блока: «Записать в блок» только при связи. */
    val connectedDeviceName: String? = null,
    val pickMode: Boolean = false
) {
    val all: List<LocalConfiguration> get() = (list as? MineList.Ready)?.items.orEmpty()

    /** Модели, которые есть в списке, — варианты фильтра. */
    val models: List<String> get() = all.map { it.deviceName }.distinct().sortedBy { it.lowercase() }

    val filtered: List<LocalConfiguration>
        get() {
            val q = query.trim()
            return all.filter { item ->
                (modelFilter == null || item.deviceName.equals(modelFilter, ignoreCase = true)) &&
                    (
                        q.isEmpty() ||
                            item.name.contains(q, ignoreCase = true) ||
                            item.description.contains(q, ignoreCase = true)
                        )
            }
        }

    val selected: LocalConfiguration? get() = selectedId?.let { id -> all.firstOrNull { it.id == id } }
}

internal sealed interface MineList {
    data object Loading : MineList
    data object Error : MineList
    data class Ready(val items: List<LocalConfiguration>) : MineList
}

internal sealed interface MineDialog {
    /** «+ Создать»: с нуля / из облака / из файла. */
    data object Create : MineDialog

    /** Меню ⋮ карточки (шторка на телефоне). */
    data class Menu(val item: LocalConfiguration) : MineDialog

    data class Rename(val item: LocalConfiguration, val name: String, val description: String) : MineDialog

    data class Delete(val item: LocalConfiguration) : MineDialog
}

internal enum class MineMessage { COPIED, DELETED, RENAMED, FAILED }

internal sealed interface MineEffect {
    data class Message(val message: MineMessage) : MineEffect

    /** Копия создана — тост с «Открыть». */
    data class Copied(val configuration: SettingsSource.LocalConfiguration) : MineEffect

    data class ExportFile(val path: String, val fileName: String) : MineEffect

    data class FileSaved(val name: String) : MineEffect
}
