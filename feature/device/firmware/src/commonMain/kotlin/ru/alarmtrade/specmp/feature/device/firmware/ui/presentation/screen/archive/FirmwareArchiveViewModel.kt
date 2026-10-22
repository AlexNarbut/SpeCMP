package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionNumber
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.session.FirmwareArchivePicker

/**
 * «Архив прошивок»: версии модуля в выбранном канале, страницами по [PAGE_SIZE]; выбранная версия уходит экрану
 * обновления через [FirmwareArchivePicker].
 *
 * Страница 1 грузится при создании; следующая — когда список докручен до конца. Версии уникальны по номеру и
 * держатся от новой к старой (числами: `3.10` новее `3.9`). Источник не отдаёт признак «страниц больше нет», поэтому
 * конец списка — страница без новых версий (эвристика). Ошибка первой страницы — полноэкранная, ошибка догрузки
 * оставляет список.
 * Версия старее установленной выбирается через подтверждение, равная установленной и новее — сразу.
 */
class FirmwareArchiveViewModel(
    private val args: FirmwareArchiveArgs,
    private val repository: FirmwareRepository,
    private val picker: FirmwareArchivePicker,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private enum class Load { Loading, Error, Ready }

    private data class Core(
        val load: Load = Load.Loading,
        val items: List<FirmwareVersion> = emptyList(),
        val page: Int = 0,
        val hasMore: Boolean = false,
        val loadingMore: Boolean = false,
        val moreError: Boolean = false,
        val selected: String? = null,
        val rollback: String? = null
    ) {
        val canLoadMore: Boolean get() = load == Load.Ready && hasMore && !loadingMore && !moreError
    }

    private val core = MutableStateFlow(Core())

    val state: StateFlow<FirmwareArchiveState> = core
        .map(::toState)
        .flowOn(dispatchers.default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), toState(core.value))

    private val _effect = sideEffectFlow<FirmwareArchiveEffect>()
    val effect: SharedFlow<FirmwareArchiveEffect> = _effect.asSharedFlow()

    init {
        loadFirstPage()
    }

    private fun toState(core: Core): FirmwareArchiveState {
        val rows = core.items
            .sortedByDescending { FirmwareVersionNumber.parse(it.version) }
            .map { version ->
                ArchiveRowUi(
                    version = version.version,
                    createdOn = version.createdOn,
                    description = version.description,
                    kind = kindOf(version.version)
                )
            }
        val content = when (core.load) {
            Load.Loading -> ArchiveContent.Loading

            Load.Error -> ArchiveContent.Error

            Load.Ready -> if (rows.isEmpty()) {
                ArchiveContent.Empty
            } else {
                ArchiveContent.Ready(rows, loadingMore = core.loadingMore, moreError = core.moreError)
            }
        }
        val installed = args.installedVersion
        return FirmwareArchiveState(
            content = content,
            selected = core.selected ?: rows.firstOrNull()?.version,
            rollback = core.rollback?.let { version -> installed?.let { RollbackUi(version, it) } },
            channel = args.channel,
            installedVersion = installed,
            deviceTitle = args.deviceTitle,
            profile = args.profile
        )
    }

    private fun kindOf(version: String): ArchiveRowKind {
        val installed = args.installedVersion ?: return ArchiveRowKind.New
        return when (version.relationTo(installed)) {
            VersionRelation.Newer -> ArchiveRowKind.New
            VersionRelation.Same -> ArchiveRowKind.Installed
            VersionRelation.Older -> ArchiveRowKind.Older
        }
    }

    // ───── загрузка ─────

    /** Начать список заново (иконка «обновить», «Повторить» при ошибке первой страницы, «Обновить» при пустом). */
    fun onRefresh() = loadFirstPage()

    private fun loadFirstPage() {
        core.update { Core(selected = it.selected) }
        // Серийно: обновление не теряется, если подгрузка ещё идёт; её результат придёт до сброса и будет перезаписан.
        safeLaunch { executeWithProceedLock(needSkipAnother = false) { loadPage(FIRST_PAGE) } }
    }

    /** Список докручен до конца: следующая страница. */
    fun onEndReached() {
        if (transition({ it.canLoadMore }) { it.copy(loadingMore = true) }) {
            val next = core.value.page + 1
            safeLaunch { executeWithProceedLock { loadPage(next) } }
        }
    }

    /** «Повторить» под списком после ошибки догрузки. */
    fun onRetryMore() {
        if (transition({ it.moreError }) { it.copy(moreError = false, loadingMore = true) }) {
            val next = core.value.page + 1
            safeLaunch { executeWithProceedLock { loadPage(next) } }
        }
    }

    private suspend fun loadPage(page: Int) {
        val response = withContext(dispatchers.io) {
            repository.archivePage(args.moduleId, args.channel, page, PAGE_SIZE)
        }
        when (response) {
            is Response.Success -> core.update { state ->
                val known = state.items.mapTo(mutableSetOf()) { it.version }
                val fresh = response.value.items.filter { known.add(it.version) }
                state.copy(
                    load = Load.Ready,
                    items = state.items + fresh,
                    page = page,
                    // Нет новых версий — конец (источник признак не отдаёт; см. KDoc класса).
                    hasMore = fresh.isNotEmpty(),
                    loadingMore = false,
                    moreError = false
                )
            }

            is Response.Error -> core.update { state ->
                if (page == FIRST_PAGE) {
                    state.copy(load = Load.Error, loadingMore = false)
                } else {
                    state.copy(loadingMore = false, moreError = true)
                }
            }
        }
    }

    // ───── выбор ─────

    fun onSelectRow(version: String) {
        core.update { it.copy(selected = version) }
    }

    /** «Выбрать» / «Установить»: старее установленной — через подтверждение, иначе сразу. */
    fun onPick(version: String) {
        val item = core.value.items.firstOrNull { it.version == version } ?: return
        if (kindOf(version) == ArchiveRowKind.Older && args.installedVersion != null) {
            core.update { it.copy(rollback = version) }
        } else {
            pick(item)
        }
    }

    fun onRollbackConfirm() {
        val version = core.value.rollback ?: return
        core.update { it.copy(rollback = null) }
        core.value.items.firstOrNull { it.version == version }?.let(::pick)
    }

    fun onRollbackDismiss() {
        core.update { it.copy(rollback = null) }
    }

    private fun pick(version: FirmwareVersion) {
        picker.pick(version)
        _effect.tryEmit(FirmwareArchiveEffect.Close)
    }

    /** Атомарный переход: [change] применяется, только если [from] истинно для текущего состояния. */
    private inline fun transition(from: (Core) -> Boolean, change: (Core) -> Core): Boolean {
        while (true) {
            val current = core.value
            if (!from(current)) return false
            if (core.compareAndSet(current, change(current))) return true
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 10
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
