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
    override suspend fun onStart() {
        loadFirstPage()
    }

    /** Поля экрана считаются в том же reduce, что и их источник, — без кадра рассинхрона. */
    private fun reduceSource(transform: ArchiveSource.() -> ArchiveSource) {
        reduce { withSource(source.transform(), args) }
    }

    fun onRefresh() {
        loadFirstPage()
    }

    private fun loadFirstPage() {
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
        if (!currentSource.canLoadMore) return
        reduceSource { copy(loadingMore = true) }
        val next = currentSource.page + 1
        intent { executeWithProceedLock { loadPage(next) } }
    }

    fun onRetryMore() {
        if (currentSource.moreError == null) return
        reduceSource { copy(moreError = null, loadingMore = true) }
        val next = currentSource.page + 1
        intent { executeWithProceedLock { loadPage(next) } }
    }

    private suspend fun loadPage(page: Int) {
        val start = currentSource
        val channel = currentState.channel
        val filter = start.canApplied.takeIf { args.canFilterOn }
        val moduleId = when (val module = moduleId()) {
            is Response.Error -> {
                reduceSource { copy(load = LCEState.Error(FirmwareError.from(module.exception)), loadingMore = false) }
                return
            }

            is Response.Success -> module.value ?: run {
                reduceSource {
                    copy(load = LCEState.Content(Unit), hasMore = false, loadingMore = false, noModule = true)
                }
                return
            }
        }
        val response = withContext(dispatchers.io) {
            repository.archivePage(moduleId, channel, page, PAGE_SIZE, can = args.canFilterOf(filter))
        }
        // Пока страница грузилась, фильтр, модуль или канал сменили или список начали заново: ответ не нужен.
        if (currentSource.canApplied != start.canApplied || currentState.channel != channel) return
        if (!currentSource.awaits(page)) return
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

    private suspend fun moduleId(): Response<Long?> {
        currentSource.moduleId?.let { return Response.Success(it) }
        val deviceName = args.deviceName ?: return Response.Success(null)
        val response = withContext(dispatchers.io) { repository.moduleId(deviceName, FirmwareModuleKind.AlarmMain) }
        if (response is Response.Success) response.value?.let { id -> reduceSource { copy(moduleId = id) } }
        return response
    }

    fun onChannelSelect(channel: FirmwareChannel) {
        if (!args.noUnit || currentState.channel == channel) return
        reduce { copy(channel = channel).withSource(source.copy(selected = null), args) }
        loadFirstPage()
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
        val saved = currentState.saved ?: return
        reduce { copy(saved = null) }
        onShare(saved.key)
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
        reduce { copy(saved = null) }
    }

    fun onExportErrorDismiss() {
        reduce { copy(exportError = null) }
    }

    /** Одна загрузка за раз; ошибка скачивания — баннер. */
    private fun export(key: String, use: suspend (FirmwareVersion, ByteArray) -> Unit) {
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
        val code = args.deviceCanCode ?: return
        reduceSource { copy(canInput = code, carTitle = null) }
        applyCanFilter(code)
    }

    fun onCanInput(text: String) {
        val code = text.filter(Char::isDigit).take(CAN_CODE_DIGITS)
        reduceSource { copy(canInput = code, carTitle = null) }
        when (code.length) {
            0 -> applyCanFilter(null)
            CAN_CODE_DIGITS -> applyCanFilter(code)
            else -> Unit
        }
    }

    fun onVehiclePickerOpen() {
        reduce { copy(vehiclePicker = true) }
    }

    fun onVehiclePickerClose() {
        reduce { copy(vehiclePicker = false) }
    }

    fun onVehiclePicked(canCode: String, carTitle: String) {
        val code = canCode.filter(Char::isDigit).take(CAN_CODE_DIGITS)
        reduce { copy(vehiclePicker = false).withSource(source.copy(canInput = code, carTitle = carTitle), args) }
        applyCanFilter(code.ifEmpty { null })
    }

    private fun applyCanFilter(code: String?) {
        if (currentSource.canApplied == code) return
        reduceSource { copy(canApplied = code) }
        loadFirstPage()
    }

    fun onSelectRow(key: String) {
        reduceSource { copy(selected = key) }
    }

    fun onPick(key: String) {
        if (args.viewOnly) return
        val item = itemOf(key) ?: return
        if (args.kindOf(item) == ArchiveRowKind.Older && args.installedVersion != null) {
            reduceSource { copy(rollback = key) }
        } else {
            pick(item)
        }
    }

    fun onRollbackConfirm() {
        val key = currentSource.rollback ?: return
        reduceSource { copy(rollback = null) }
        itemOf(key)?.let(::pick)
    }

    fun onRollbackDismiss() {
        reduceSource { copy(rollback = null) }
    }

    private fun itemOf(key: String): FirmwareVersion? = currentSource.items.firstOrNull { it.archiveKey == key }

    private fun pick(version: FirmwareVersion) {
        postSideEffect(FirmwareArchiveEffect.Picked(version))
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 10
    }
}
