package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.updateIf
import ru.alarmtrade.specmp.core.ui.effect.sideEffectFlow
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionNumber
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError

/**
 * «Архив прошивок»: версии модуля в выбранном канале, страницами по [PAGE_SIZE]; выбранная версия уходит экрану
 * обновления через эффект [FirmwareArchiveEffect.Picked].
 *
 * Страница 1 грузится при создании; следующая — когда список докручен до конца. Версии уникальны по номеру и
 * держатся от новой к старой (числами: `3.10` новее `3.9`). Источник не отдаёт признак «страниц больше нет», поэтому
 * конец списка — страница без новых версий (эвристика). Ошибка первой страницы — полноэкранная, ошибка догрузки
 * оставляет список.
 * Версия старее установленной выбирается через подтверждение, равная установленной и новее — сразу.
 *
 * CAN-фильтр (только основная программа): список грузится заново, когда в поле набран полный CAN-код или поле
 * очищено; код из каталога автомобилей применяется сразу. Модуль без [FirmwareArchiveRequest.moduleId] — основной
 * модуль модели блока, определяется перед первой страницей. В режиме просмотра версия не выбирается.
 */
class FirmwareArchiveViewModel(
    private val args: FirmwareArchiveRequest,
    private val repository: FirmwareRepository,
    private val dispatchers: AppDispatchers
) : BaseViewModel() {

    private data class Core(
        /** Первая страница: `Content(Unit)` — загружена (строки — в [items]). */
        val load: LCEState<Unit, FirmwareError> = LCEState.Loading,
        val items: List<FirmwareVersion> = emptyList(),
        val page: Int = 0,
        val hasMore: Boolean = false,
        val loadingMore: Boolean = false,
        val moreError: FirmwareError? = null,
        val selected: String? = null,
        val rollback: String? = null,
        val moduleId: Long? = null,
        val canInput: String = "",
        val canApplied: String? = null,
        val carTitle: String? = null,
        val vehiclePicker: Boolean = false
    ) {
        val canLoadMore: Boolean get() = load is LCEState.Content && hasMore && !loadingMore && moreError == null
    }

    private val core = MutableStateFlow(
        Core(moduleId = args.moduleId, canInput = args.canCode.orEmpty(), canApplied = args.canCode)
    )

    // Первая страница — при первой подписке на state, один раз за жизнь VM: возврат на экран список не сбрасывает.
    // Строки пересобираются только при новом списке версий (загрузка страницы), а не на каждый тап, цифру CAN-кода
    // или диалог: сортировка разбирает номера версий.
    val state: StateFlow<FirmwareArchiveState> = core
        .launchOnFirstStart { loadFirstPage() }
        .runningFold(null as Pair<Core, List<ArchiveRowUi>>?) { previous, current ->
            val rows = previous?.takeIf { it.first.items === current.items }?.second ?: rowsOf(current.items)
            current to rows
        }
        .filterNotNull()
        .map { (current, rows) -> toState(current, rows) }
        .flowOn(dispatchers.default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            toState(core.value, rowsOf(core.value.items))
        )

    private val _effect = sideEffectFlow<FirmwareArchiveEffect>()
    val effect: SharedFlow<FirmwareArchiveEffect> = _effect.asSharedFlow()

    /** Строки списка от новой версии к старой; номер каждой версии разбирается один раз. */
    private fun rowsOf(items: List<FirmwareVersion>): List<ArchiveRowUi> = items
        .map { FirmwareVersionNumber.parse(it.version) to it }
        .sortedByDescending { it.first }
        .map { (_, version) ->
            ArchiveRowUi(
                version = version.version,
                createdOn = version.createdOn,
                description = version.description,
                kind = kindOf(version.version),
                canCode = version.canCode,
                canVersion = version.canVersion
            )
        }

    private fun toState(core: Core, rows: List<ArchiveRowUi>): FirmwareArchiveState {
        val content = when (val load = core.load) {
            LCEState.None, LCEState.Loading -> LCEState.Loading
            is LCEState.Error -> load
            is LCEState.Content -> LCEState.Content(rows, isUpdating = core.loadingMore, updateError = core.moreError)
        }
        val installed = args.installedVersion
        return FirmwareArchiveState(
            content = content,
            selected = core.selected ?: rows.firstOrNull()?.version,
            rollback = core.rollback?.let { version -> installed?.let { RollbackUi(version, it) } },
            channel = args.channel,
            installedVersion = installed,
            deviceTitle = args.deviceTitle,
            profile = args.profile,
            canFilter = if (args.canFilterEnabled) {
                CanFilterUi(core.canInput, core.canApplied, core.carTitle)
            } else {
                null
            },
            viewOnly = args.viewOnly,
            vehiclePicker = core.vehiclePicker,
            deviceName = args.deviceName
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
        core.update {
            it.copy(
                load = LCEState.Loading,
                items = emptyList(),
                page = 0,
                hasMore = false,
                loadingMore = false,
                moreError = null,
                rollback = null
            )
        }
        // Серийно: обновление не теряется, если подгрузка ещё идёт; её ответ придёт уже после сброса и отбросится
        // (loadPage: список ждёт другую страницу).
        safeLaunch { executeWithProceedLock(needSkipAnother = false) { loadPage(FIRST_PAGE) } }
    }

    /** Список докручен до конца: следующая страница. */
    fun onEndReached() {
        if (core.updateIf({ it.canLoadMore }) { it.copy(loadingMore = true) }) {
            val next = core.value.page + 1
            safeLaunch { executeWithProceedLock { loadPage(next) } }
        }
    }

    /** «Повторить» под списком после ошибки догрузки. */
    fun onRetryMore() {
        if (core.updateIf({ it.moreError != null }) { it.copy(moreError = null, loadingMore = true) }) {
            val next = core.value.page + 1
            safeLaunch { executeWithProceedLock { loadPage(next) } }
        }
    }

    private suspend fun loadPage(page: Int) {
        val filter = core.value.canApplied
        val moduleId = when (val module = moduleId()) {
            is Response.Error -> {
                core.update {
                    it.copy(load = LCEState.Error(FirmwareError.from(module.exception)), loadingMore = false)
                }
                return
            }

            // У модели нет основного модуля — версий нет.
            is Response.Success -> module.value ?: run {
                core.update { it.copy(load = LCEState.Content(Unit), hasMore = false, loadingMore = false) }
                return
            }
        }
        val response = withContext(dispatchers.io) {
            repository.archivePage(moduleId, args.channel, page, PAGE_SIZE, canCode = filter)
        }
        // Пока страница грузилась, фильтр сменили или список начали заново: этот ответ не нужен.
        if (core.value.canApplied != filter || !core.value.awaits(page)) return
        when (response) {
            is Response.Success -> core.update { state ->
                val known = state.items.mapTo(mutableSetOf()) { it.version }
                val fresh = response.value.items.filter { known.add(it.version) }
                state.copy(
                    load = LCEState.Content(Unit),
                    items = state.items + fresh,
                    page = page,
                    // Нет новых версий — конец (источник признак не отдаёт; см. KDoc класса).
                    hasMore = fresh.isNotEmpty(),
                    loadingMore = false,
                    moreError = null
                )
            }

            is Response.Error -> core.update { state ->
                if (page == FIRST_PAGE) {
                    state.copy(
                        load = LCEState.Error(FirmwareError.from(response.exception)),
                        loadingMore = false
                    )
                } else {
                    state.copy(loadingMore = false, moreError = FirmwareError.from(response.exception))
                }
            }
        }
    }

    /**
     * Список ждёт [page]: первую — после сброса (страниц ещё нет), следующую — пока идёт догрузка за последней
     * показанной. Иначе ответ устарел (обновление во время догрузки) и не должен попасть в новый список.
     */
    private fun Core.awaits(page: Int): Boolean =
        if (page == FIRST_PAGE) this.page == 0 else loadingMore && this.page == page - 1

    /** Модуль архива: из запроса или основной модуль модели блока (один раз за экран). */
    private suspend fun moduleId(): Response<Long?> {
        core.value.moduleId?.let { return Response.Success(it) }
        val deviceName = args.deviceName ?: return Response.Success(null)
        val response = withContext(dispatchers.io) { repository.mainModuleId(deviceName) }
        if (response is Response.Success) response.value?.let { id -> core.update { it.copy(moduleId = id) } }
        return response
    }

    // ───── CAN-фильтр ─────

    /** Ввод в поле CAN-кода: список перезапрашивается на полном коде и при очистке поля. */
    fun onCanInput(text: String) {
        val code = text.filter(Char::isDigit).take(CAN_CODE_LENGTH)
        core.update { it.copy(canInput = code, carTitle = null) }
        when (code.length) {
            0 -> applyCanFilter(null)
            CAN_CODE_LENGTH -> applyCanFilter(code)
            else -> Unit
        }
    }

    /** Кнопка каталога есть, только когда каталог подключён (слот точки входа). */
    fun onVehiclePickerOpen() {
        core.update { it.copy(vehiclePicker = true) }
    }

    fun onVehiclePickerClose() {
        core.update { it.copy(vehiclePicker = false) }
    }

    /** Автомобиль из каталога: его CAN-код — фильтр списка. */
    fun onVehiclePicked(canCode: String, carTitle: String) {
        val code = canCode.filter(Char::isDigit).take(CAN_CODE_LENGTH)
        core.update { it.copy(canInput = code, carTitle = carTitle, vehiclePicker = false) }
        applyCanFilter(code.ifEmpty { null })
    }

    private fun applyCanFilter(code: String?) {
        if (core.value.canApplied == code) return
        core.update { it.copy(canApplied = code) }
        loadFirstPage()
    }

    // ───── выбор ─────

    fun onSelectRow(version: String) {
        core.update { it.copy(selected = version) }
    }

    /** «Выбрать» / «Установить»: старее установленной — через подтверждение, иначе сразу. */
    fun onPick(version: String) {
        if (args.viewOnly) return
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
        _effect.tryEmit(FirmwareArchiveEffect.Picked(version))
    }

    private companion object {
        const val CAN_CODE_LENGTH = 4
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 10
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
