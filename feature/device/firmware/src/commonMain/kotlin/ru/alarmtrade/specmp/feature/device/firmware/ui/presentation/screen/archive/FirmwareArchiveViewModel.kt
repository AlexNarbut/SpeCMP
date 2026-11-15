package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.core.ui.viewmodel.BaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult

/**
 * Источник не отдаёт признак «страниц больше нет»: конец списка — страница без новых версий (эвристика). Версии
 * уникальны по номеру и CAN ([archiveRowKey]); версия CAN-профиля уточняет фильтр, только пока в нём код блока.
 */
class FirmwareArchiveViewModel(
    private val args: FirmwareArchiveRequest,
    private val repository: FirmwareRepository,
    private val dispatchers: AppDispatchers,
    private val exporter: FirmwareFileExporter? = null,
) : BaseViewModel<FirmwareArchiveState, FirmwareArchiveEffect>(args.initialState(exporter)) {

    private val currentSource: ArchiveSource get() = currentState.source

    // Первая страница — при первой подписке на state, один раз за жизнь VM: возврат на экран список не сбрасывает.
    override suspend fun IntentScope.onStart() {
        loadFirstPage()
    }

    /** Поля экрана считаются в том же reduce, что и их источник, — без кадра рассинхрона. */
    private suspend fun reduceSource(transform: ArchiveSource.() -> ArchiveSource) = subIntent {
        reduce { withSource(source.transform(), args) }
    }

    fun onRefresh() {
        intent {
            loadFirstPage()
        }
    }

    private suspend fun loadFirstPage() = subIntent {
        reduceSource {
            copy(
                load = LCEState.Loading,
                items = emptyList(),
                page = 0,
                hasMore = false,
                loadingMore = false,
                moreError = null,
                rollback = null,
                noModule = false,
            )
        }
        // Серийно: обновление не теряется, если подгрузка ещё идёт; её ответ придёт уже после сброса и отбросится
        // (loadPage: список ждёт другую страницу).
        intent { executeWithProceedLock(needSkipAnother = false) { loadPage(FIRST_PAGE) } }
    }

    fun onEndReached() {
        intent {
            if (!currentSource.canLoadMore) return@intent
            reduceSource { copy(loadingMore = true) }
            val next = currentSource.page + 1
            intent { executeWithProceedLock { loadPage(next) } }
        }
    }

    fun onRetryMore() {
        intent {
            if (currentSource.moreError == null) return@intent
            reduceSource { copy(moreError = null, loadingMore = true) }
            val next = currentSource.page + 1
            intent { executeWithProceedLock { loadPage(next) } }
        }
    }

    private suspend fun loadPage(page: Int) = subIntent {
        val start = currentSource
        val channel = currentState.channel
        val filter = start.canApplied.takeIf { args.canFilterOn }
        val moduleId = when (val module = moduleId()) {
            is Response.Error -> {
                reduceSource { copy(load = LCEState.Error(FirmwareError.from(module.exception)), loadingMore = false) }
                return@subIntent
            }

            is Response.Success -> module.value ?: run {
                reduceSource {
                    copy(load = LCEState.Content(Unit), hasMore = false, loadingMore = false, noModule = true)
                }
                return@subIntent
            }
        }
        val response = withContext(dispatchers.io) {
            repository.archivePage(moduleId, channel, page, PAGE_SIZE, can = args.canFilterOf(filter))
        }
        // Пока страница грузилась, фильтр, модуль или канал сменили или список начали заново: ответ не нужен.
        if (currentSource.canApplied != start.canApplied || currentState.channel != channel) return@subIntent
        if (!currentSource.awaits(page)) return@subIntent
        when (response) {
            is Response.Success -> reduceSource {
                val known = items.mapTo(mutableSetOf()) { it.archiveKey }
                val fresh = response.value.items.filter { known.add(it.archiveKey) }
                copy(
                    load = LCEState.Content(Unit),
                    items = items + fresh,
                    page = page,
                    // Нет новых версий — конец (источник признак не отдаёт; см. KDoc класса).
                    hasMore = fresh.isNotEmpty(),
                    loadingMore = false,
                    moreError = null,
                )
            }

            is Response.Error -> reduceSource {
                if (page == FIRST_PAGE) {
                    copy(load = LCEState.Error(FirmwareError.from(response.exception)), loadingMore = false)
                } else {
                    copy(loadingMore = false, moreError = FirmwareError.from(response.exception))
                }
            }
        }
    }

    /** Список ждёт [page]: первую — после сброса, следующую — во время догрузки; иначе ответ устарел. */
    private fun ArchiveSource.awaits(page: Int): Boolean =
        if (page == FIRST_PAGE) this.page == 0 else loadingMore && this.page == page - 1

    private suspend fun moduleId(): Response<Long?> = subIntent {
        currentSource.moduleId?.let { return@subIntent Response.Success(it) }
        val deviceName = args.deviceName ?: return@subIntent Response.Success(null)
        val response = withContext(dispatchers.io) { repository.moduleId(deviceName, FirmwareModuleKind.AlarmMain) }
        if (response is Response.Success) response.value?.let { id -> reduceSource { copy(moduleId = id) } }
        return@subIntent response
    }

    fun onChannelSelect(channel: FirmwareChannel) {
        intent {
            if (!args.noUnit || currentState.channel == channel) return@intent
            reduce { copy(channel = channel).withSource(source.copy(selected = null), args) }
            loadFirstPage()
        }
    }

    fun onDownload(key: String) {
        export(key) { item, bytes ->
            val exporter = exporter ?: return@export
            val fileName = args.fileNameOf(item)
            when (val result = exporter.save(fileName, bytes)) {
                is FirmwareSaveResult.Saved -> reduce {
                    copy(saved = ArchiveSavedUi(key, fileName, result.path, result.folder))
                }

                FirmwareSaveResult.Cancelled -> Unit

                FirmwareSaveResult.PermissionDenied -> reduce { copy(exportError = ArchiveExportError.Permission) }

                FirmwareSaveResult.Failed -> reduce { copy(exportError = ArchiveExportError.Save) }
            }
        }
    }

    fun onShare(key: String) {
        export(key) { item, bytes ->
            val exporter = exporter ?: return@export
            if (!exporter.share(args.fileNameOf(item), bytes)) {
                reduce { copy(exportError = ArchiveExportError.Share) }
            }
        }
    }

    fun onSavedShare() {
        intent {
            val saved = currentState.saved ?: return@intent
            reduce { copy(saved = null) }
            onShare(saved.key)
        }
    }

    fun onSavedOpenFolder() {
        intent {
            val saved = currentState.saved ?: return@intent
            reduce { copy(saved = null) }
            val exporter = exporter ?: return@intent
            if (!exporter.openFolder(saved.path)) reduce { copy(exportError = ArchiveExportError.Save) }
        }
    }

    fun onSavedDismiss() {
        intent {
            reduce { copy(saved = null) }
        }
    }

    fun onExportErrorDismiss() {
        intent {
            reduce { copy(exportError = null) }
        }
    }

    /** Одна загрузка за раз; ошибка скачивания — баннер. */
    private fun export(key: String, use: suspend IntentScope.(FirmwareVersion, ByteArray) -> Unit) {
        intent {
            if (!args.noUnit) return@intent
            val item = itemOf(key) ?: return@intent
            if (currentState.busy != null) return@intent
            reduce { copy(busy = key, exportError = null) }
            try {
                when (val response = withContext(dispatchers.io) { repository.download(item.url) }) {
                    is Response.Success -> use(item, response.value)

                    is Response.Error -> reduce {
                        copy(exportError = ArchiveExportError.Download(FirmwareError.from(response.exception)))
                    }
                }
            } finally {
                reduce { copy(busy = null) }
            }
        }
    }

    fun onCanResetToDevice() {
        intent {
            val code = args.deviceCanCode ?: return@intent
            reduceSource { copy(canInput = code, carTitle = null) }
            applyCanFilter(code)
        }
    }

    fun onCanInput(text: String) {
        intent {
            val code = text.filter(Char::isDigit).take(CAN_CODE_DIGITS)
            reduceSource { copy(canInput = code, carTitle = null) }
            when (code.length) {
                0 -> applyCanFilter(null)
                CAN_CODE_DIGITS -> applyCanFilter(code)
                else -> Unit
            }
        }
    }

    fun onVehiclePickerOpen() {
        intent {
            reduce { copy(vehiclePicker = true) }
        }
    }

    fun onVehiclePickerClose() {
        intent {
            reduce { copy(vehiclePicker = false) }
        }
    }

    fun onVehiclePicked(canCode: String, carTitle: String) {
        intent {
            val code = canCode.filter(Char::isDigit).take(CAN_CODE_DIGITS)
            reduce { copy(vehiclePicker = false).withSource(source.copy(canInput = code, carTitle = carTitle), args) }
            applyCanFilter(code.ifEmpty { null })
        }
    }

    private suspend fun applyCanFilter(code: String?) = subIntent {
        if (currentSource.canApplied == code) return@subIntent
        reduceSource { copy(canApplied = code) }
        loadFirstPage()
    }

    fun onSelectRow(key: String) {
        intent {
            reduceSource { copy(selected = key) }
        }
    }

    fun onPick(key: String) {
        intent {
            if (args.viewOnly) return@intent
            val item = itemOf(key) ?: return@intent
            if (args.kindOf(item) == ArchiveRowKind.Older && args.installedVersion != null) {
                reduceSource { copy(rollback = key) }
            } else {
                pick(item)
            }
        }
    }

    fun onRollbackConfirm() {
        intent {
            val key = currentSource.rollback ?: return@intent
            reduceSource { copy(rollback = null) }
            itemOf(key)?.let { pick(it) }
        }
    }

    fun onRollbackDismiss() {
        intent {
            reduceSource { copy(rollback = null) }
        }
    }

    private fun itemOf(key: String): FirmwareVersion? = currentSource.items.firstOrNull { it.archiveKey == key }

    private suspend fun pick(version: FirmwareVersion) = subIntent {
        postSideEffect(FirmwareArchiveEffect.Picked(version))
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 10
    }
}
