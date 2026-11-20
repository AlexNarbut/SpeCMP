package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.lce.asLCEState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassportSummary
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportTransferRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

/** Поиск — в SDK; новый запрос отменяет прежний (`flatMapLatest`), при изменении архива поиск повторяется. */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class PassportListViewModel(
    mode: PassportListMode,
    deviceSession: DeviceSession,
    private val repository: MountPassportRepository,
    private val transfer: PassportTransferRepository,
    private val dispatchers: AppDispatchers,
) : BaseDeviceViewModel<PassportListState, PassportListEffect>(deviceSession, PassportListState(mode)) {

    override suspend fun IntentScope.onStart() {
        select { query.trim() }
            .debounce { if (it.isEmpty()) 0 else SEARCH_DEBOUNCE_MS }
            .flatMapLatest(::passports)
            .flowOn(dispatchers.io)
            .collectIntoState { list -> copy(list = list) }
        if (currentState.isArchive) {
            launch {
                val due = withContext(dispatchers.io) { transfer.shouldRemindBackup() }.getValueIfSuccess() == true
                reduce { copy(backupReminder = due) }
            }
            repository.observeAll()
                .mapLatest { repository.storageUsage().getValueIfSuccess() }
                .flowOn(dispatchers.io)
                .collectIntoState { usage -> copy(storage = usage) }
        }
        connectedDevice
            .map { device -> device?.let { ConnectedUnit(it.systemId.toString(), it.systemName) } }
            .collectIntoState { unit -> copy(connected = unit) }
    }

    fun onQueryChange(text: String) {
        intent { reduce { copy(query = text.take(MAX_QUERY_LENGTH)) } }
    }

    fun onRetry() {
        reload()
    }

    fun onSelect(id: String) {
        intent { reduce { copy(selectedId = id) } }
    }

    fun onBackupLater() {
        intent {
            reduce { copy(backupReminder = false) }
            withContext(dispatchers.io) { transfer.snoozeBackupReminder() }
                .onError { logger.w(it) { "Backup reminder snooze failed" } }
        }
    }

    /** Архив собран: выбор для экспорта закончен; копия всего архива снимает напоминание. */
    fun onArchiveBuilt(fullBackup: Boolean) {
        intent {
            reduce {
                copy(
                    picking = false,
                    picked = persistentSetOf(),
                    backupReminder = backupReminder && !fullBackup,
                )
            }
        }
    }

    fun onPickStart() {
        intent { reduce { copy(picking = true, picked = persistentSetOf()) } }
    }

    fun onPickCancel() {
        intent { reduce { copy(picking = false, picked = persistentSetOf()) } }
    }

    fun onPickToggle(id: String) {
        intent {
            reduce {
                when {
                    !picking || id !in exportable -> this
                    id in picked -> copy(picked = picked.remove(id))
                    else -> copy(picked = picked.add(id))
                }
            }
        }
    }

    fun onPickAll() {
        intent {
            reduce {
                val all = exportable
                if (picked.containsAll(all)) copy(picked = persistentSetOf()) else copy(picked = all.toPersistentSet())
            }
        }
    }

    fun onDeleteRequest(item: MountPassportSummary) {
        intent { reduce { copy(deleteCandidate = item) } }
    }

    fun onDeleteDismiss() {
        intent { reduce { copy(deleteCandidate = null) } }
    }

    fun onDeleteConfirm() {
        intent {
            val item = currentState.deleteCandidate ?: return@intent
            reduce { copy(deleteCandidate = null) }
            when (val response = withContext(dispatchers.io) { repository.delete(item.id) }) {
                is Response.Success -> {
                    reduce { if (selectedId == item.id) copy(selectedId = null) else this }
                    postSideEffect(PassportListEffect.Deleted)
                }

                is Response.Error -> postSideEffect(
                    PassportListEffect.DeleteFailed(PassportError.from(response.exception)),
                )
            }
        }
    }

    private fun passports(query: String): Flow<LCEState<PassportListContent, PassportError>> {
        val mode = currentState.mode
        val source = when (mode) {
            PassportListMode.Archive -> repository.observeAll()
            is PassportListMode.Unit -> repository.observeUnit(mode.systemId.toString())
        }
        val found = if (query.isEmpty() || mode !is PassportListMode.Archive) {
            source
        } else {
            source.mapLatest { repository.search(query) }
        }
        return found.map { response ->
            response
                .map { items -> PassportListContent(query, items.toImmutableList()) }
                .asLCEState(PassportError::from)
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
        const val MAX_QUERY_LENGTH = 60
    }
}
