package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen.mine

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.reusing
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.content
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.configurator.ui.presentation.model.ConfiguratorError
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.settings.ui.presentation.platform.settingsFileName

/**
 * K1 «Мои конфигурации»: список (новые сверху, показ порциями), поиск по названию и описанию и фильтр по модели —
 * по загруженному списку; действия карточки: название и описание, копия, сохранение в файл, удаление.
 *
 * `pickDeviceName` — режим выбора для загрузки в редактор блока: фильтр по модели блока включён сразу (его можно
 * снять — другая модель загрузится с предупреждением).
 */
internal class MineViewModel(
    pickDeviceName: String?,
    private val configurations: LocalConfigurationsRepository,
    private val settings: DeviceSettingsRepository,
    private val sessions: DeviceSettingsSessionController,
    deviceSession: DeviceSession,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<MineState, MineEffect>(
    deviceSession,
    MineState(modelFilter = pickDeviceName, pickMode = pickDeviceName != null),
) {

    override suspend fun onStart() {
        connectedDeviceName.collectIntoState { connected -> copy(connectedDeviceName = connected) }
        observeList()
    }

    // Список — одним потоком загрузок на всю жизнь VM: первая сразу, затем на каждое изменение хранилища.
    // collectLatest отменяет устаревшую загрузку — старый ответ не затрёт новый; «Повторить» перезапускает поток.
    private fun observeList() {
        launchReplacing(LIST_KEY) {
            configurations.changes.onStart { emit(Response.Success(Unit)) }.collectLatest { load() }
        }
    }

    private suspend fun load() {
        if (currentState.list !is LCEState.Content) reduceLists { copy(list = LCEState.Loading) }
        when (val response = withContext(dispatchers.io) { configurations.all() }) {
            is Response.Error -> reduceLists {
                copy(list = LCEState.Error(ConfiguratorError.from(response.exception)))
            }

            is Response.Success -> reduceLists {
                // Список перечитывается на каждое изменение хранилища: неизменённые карточки — прежние экземпляры.
                val items = response.value.reusing(list.content) { it.id }
                copy(
                    list = LCEState.Content(items),
                    selectedId = selectedId?.takeIf { id -> items.any { it.id == id } },
                )
            }
        }
    }

    /** Правка входов производных списков: список и производные меняются одним `reduce`. */
    private fun reduceLists(transform: MineState.() -> MineState) {
        reduce { transform().withLists() }
    }

    fun onRetry() {
        observeList()
    }

    fun onQueryChange(query: String) {
        reduceLists { copy(query = query, shown = MineState.PAGE_SIZE) }
    }

    fun onModelFilter(deviceName: String?) {
        reduceLists { copy(modelFilter = deviceName, shown = MineState.PAGE_SIZE) }
    }

    fun onResetFilter() {
        reduceLists { copy(query = "", modelFilter = null, shown = MineState.PAGE_SIZE) }
    }

    /** Прокрутка к концу показанного — следующая порция. */
    fun onLoadMore() {
        reduce { copy(shown = shown + MineState.PAGE_SIZE) }
    }

    /** Выбор в списке (широкое окно — карточка справа). */
    fun onSelect(id: Long) {
        reduce { copy(selectedId = id) }
    }

    fun onMenu(item: LocalConfiguration) {
        reduce { copy(dialog = MineDialog.Menu(item)) }
    }

    fun onCreateRequest() {
        reduce { copy(dialog = MineDialog.Create) }
    }

    fun onDialogDismiss() {
        reduce { copy(dialog = null) }
    }

    fun onRenameRequest(item: LocalConfiguration) {
        reduce { copy(dialog = MineDialog.Rename(item, item.name, item.description)) }
    }

    fun onRenameChange(name: String, description: String) {
        reduce {
            val rename = dialog as? MineDialog.Rename ?: return@reduce this
            copy(dialog = rename.copy(name = name, description = description))
        }
    }

    fun onRenameSubmit() {
        val dialog = currentState.dialog as? MineDialog.Rename ?: return
        if (dialog.name.isBlank()) return
        runAction(MineMessage.RENAMED) {
            configurations.updateInfo(dialog.item, dialog.name.trim(), dialog.description.trim()).also { response ->
                if (response is Response.Success) reduce { copy(selectedId = response.value.id) }
            }
        }
    }

    /** «Создать копию»: [name] — «Копия — …». */
    fun onCopy(item: LocalConfiguration, name: String) {
        runAction(MineMessage.COPIED) {
            val source = item.toSource()
            try {
                configurations.create(
                    name.take(LocalConfiguration.NAME_MAX_LENGTH),
                    item.description,
                    item.deviceName,
                    from = source,
                )
            } finally {
                settings.close(source).onError { logger.w(it) { "Failed to close the settings session" } }
            }
        }
    }

    fun onDeleteRequest(item: LocalConfiguration) {
        reduce { copy(dialog = MineDialog.Delete(item)) }
    }

    fun onDeleteConfirm() {
        val item = (currentState.dialog as? MineDialog.Delete)?.item ?: return
        runAction(MineMessage.DELETED) { configurations.delete(item.id) }
    }

    /** «Сохранить в файл»: снимок записи во временный `.dxs` → системное сохранение. */
    fun onExport(item: LocalConfiguration) {
        if (!startBusy()) return
        intent {
            try {
                export(item)
            } finally {
                // Снимок отдан системному сохранению — флаг снимет onFileExported; иначе (ошибка, отмена) — здесь.
                if (currentState.exportSnapshot == null) reduce { copy(busy = false) }
            }
        }
    }

    private suspend fun export(item: LocalConfiguration) {
        val source = item.toSource()
        val response = withContext(dispatchers.io) {
            try {
                settings.snapshot(source, item.deviceName)
            } finally {
                settings.close(source).onError { logger.w(it) { "Failed to close the settings session" } }
            }
        }
        when (response) {
            is Response.Error -> {
                logger.w(response.exception) { "Configuration export failed: ${response.message}" }
                postSideEffect(MineEffect.Message(MineMessage.FAILED))
            }

            is Response.Success -> {
                reduce { copy(exportSnapshot = response.value) }
                postSideEffect(MineEffect.ExportFile(response.value.path, settingsFileName(item.deviceName, item.name)))
            }
        }
    }

    /**
     * Действие с записью — одно за раз: «занято» ставится сразу, повторный тап (копия, экспорт, удаление) его застанет
     * и ничего не сделает. `false` — уже занято. Интенты приходят с Main — проверка и установка не разделяются.
     */
    private fun startBusy(): Boolean {
        if (currentState.busy) return false
        reduce { copy(dialog = null, busy = true) }
        return true
    }

    fun onFileExported(result: SettingsFileSaveResult) {
        val snapshot = currentState.exportSnapshot ?: return
        reduce { copy(exportSnapshot = null, busy = false) }
        sessions.discardSnapshot(snapshot)
        when (result) {
            is SettingsFileSaveResult.Saved -> postSideEffect(MineEffect.FileSaved(result.name))
            SettingsFileSaveResult.Failed -> postSideEffect(MineEffect.Message(MineMessage.FAILED))
            SettingsFileSaveResult.Cancelled -> Unit
        }
    }

    private fun runAction(success: MineMessage, block: suspend () -> Response<*>) {
        if (!startBusy()) return
        intent {
            val response = try {
                withContext(dispatchers.io) { block() }
            } finally {
                reduce { copy(busy = false) }
            }
            when (response) {
                is Response.Error -> {
                    logger.w(response.exception) { "Configuration action failed: ${response.message}" }
                    postSideEffect(MineEffect.Message(MineMessage.FAILED))
                }

                is Response.Success -> {
                    val created = response.value as? SettingsSource.LocalConfiguration
                    postSideEffect(
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
        currentState.exportSnapshot?.let(sessions::discardSnapshot)
        super.onCleared()
    }

    private companion object {
        const val LIST_KEY = "list"
    }
}
