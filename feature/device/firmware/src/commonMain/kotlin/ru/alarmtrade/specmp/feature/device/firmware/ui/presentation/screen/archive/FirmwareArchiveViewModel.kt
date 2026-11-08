package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.archive

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
import ru.alarmtrade.specmp.core.ui.viewmodel.LegacyBaseViewModel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionNumber
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.VersionRelation
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.relationTo
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model.FirmwareError
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareFileExporter
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveResult
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.platform.FirmwareSaveTarget
import ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.screen.update.UpdateProfile

/**
 * «Архив прошивок»: версии модуля в выбранном канале, страницами по [PAGE_SIZE]; выбранная версия уходит экрану
 * обновления через эффект [FirmwareArchiveEffect.Picked].
 *
 * Страница 1 грузится при создании; следующая — когда список докручен до конца. Версии уникальны по номеру и CAN
 * ([archiveRowKey]: у единой CAN-прошивки один номер выходит под разные CAN-коды) и держатся от новой к старой
 * (числами: `3.10` новее `3.9`). Источник не отдаёт признак «страниц больше нет», поэтому
 * конец списка — страница без новых версий (эвристика). Ошибка первой страницы — полноэкранная, ошибка догрузки
 * оставляет список.
 * Версия старее установленной выбирается через подтверждение, равная установленной и новее — сразу.
 *
 * CAN-фильтр (только основная программа): список грузится заново, когда в поле набран полный CAN-код или поле
 * очищено; код из каталога автомобилей применяется сразу. С подключённым блоком (единая CAN-прошивка) фильтр при
 * открытии — CAN-код и версия CAN-профиля блока; версия уточняет фильтр, только пока в нём код блока. Версии под
 * код блока подсвечиваются при любом фильтре; поставить версию под другой код можно — это не ошибка.
 * Модуль без [FirmwareArchiveRequest.moduleId] — основная программа устройства [FirmwareArchiveRequest.deviceName],
 * определяется перед первой страницей. В режиме просмотра версия не выбирается. CAN-фильтр — только у основной
 * программы сигнализации ([FirmwareArchiveRequest.canFilterAllowed]).
 *
 * Без блока ([FirmwareArchiveRequest.noUnit]): канал меняется на экране (список начинается заново), у подсистемы
 * без прошивок — «Выбрать другую подсистему»; «Скачать» сохраняет файл версии через [exporter], «Поделиться»
 * отдаёт его системному меню. Одна загрузка файла за раз.
 */
class FirmwareArchiveViewModel(
    private val args: FirmwareArchiveRequest,
    private val repository: FirmwareRepository,
    private val dispatchers: AppDispatchers,
    private val exporter: FirmwareFileExporter? = null,
) : LegacyBaseViewModel() {

    private data class Core(
        /** Первая страница: `Content(Unit)` — загружена (строки — в [items]). */
        val load: LCEState<Unit, FirmwareError> = LCEState.Loading,
        val items: List<FirmwareVersion> = emptyList(),
        val page: Int = 0,
        val hasMore: Boolean = false,
        val loadingMore: Boolean = false,
        val moreError: FirmwareError? = null,
        /** Ключ выбранной строки ([archiveRowKey]). */
        val selected: String? = null,
        /** Ключ версии, ждущей подтверждения отката. */
        val rollback: String? = null,
        val moduleId: Long? = null,
        val channel: FirmwareChannel,
        val noModule: Boolean = false,
        val canInput: String = "",
        val canApplied: String? = null,
        val carTitle: String? = null,
        val vehiclePicker: Boolean = false,
        val busy: String? = null,
        val saved: ArchiveSavedUi? = null,
        val exportError: ArchiveExportError? = null,
    ) {
        val canLoadMore: Boolean get() = load is LCEState.Content && hasMore && !loadingMore && moreError == null
    }

    private val core = (args.canCode ?: args.deviceCanCode).let { initial ->
        MutableStateFlow(
            Core(
                moduleId = args.moduleId,
                channel = args.channel,
                canInput = initial.orEmpty(),
                canApplied = initial,
            ),
        )
    }

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
            toState(core.value, rowsOf(core.value.items)),
        )

    // Текст поля CAN — отдельно и синхронно: state считается в фоне, и поле с ним отставало бы от ввода (курсор
    // прыгает, цифры встают не на место).
    private val canText = MutableStateFlow(core.value.canInput)

    /** Текст поля CAN-кода — для самого поля (остальной фильтр — в [state]). */
    val canInput: StateFlow<String> = canText.asStateFlow()

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
                kind = kindOf(version),
                canCode = version.canCode,
                canVersion = version.canVersion,
                canMatchesDevice = args.deviceCanCode != null && version.canCode == args.deviceCanCode,
                canProfileDiffers = args.deviceCanCode != null && version.canCode == args.deviceCanCode &&
                    version.canVersionCode != null && args.deviceCanVersion != null &&
                    version.canVersionCode != args.deviceCanVersion,
            )
        }

    private val canFilterOn: Boolean get() = args.canFilterAllowed && args.profile == UpdateProfile.Main

    private fun toState(core: Core, rows: List<ArchiveRowUi>): FirmwareArchiveState {
        val content = when (val load = core.load) {
            LCEState.None, LCEState.Loading -> LCEState.Loading
            is LCEState.Error -> load
            is LCEState.Content -> LCEState.Content(rows, isUpdating = core.loadingMore, updateError = core.moreError)
        }
        val installed = args.installedVersion
        return FirmwareArchiveState(
            content = content,
            selected = core.selected ?: rows.firstOrNull()?.key,
            rollback = core.rollback?.let(::itemOf)?.let { item -> installed?.let { RollbackUi(item.version, it) } },
            channel = core.channel,
            installedVersion = installed,
            deviceTitle = args.deviceTitle,
            profile = args.profile,
            canFilter = if (canFilterOn) {
                CanFilterUi(
                    input = core.canInput,
                    applied = core.canApplied,
                    carTitle = core.carTitle,
                    deviceCode = args.deviceCanCode,
                    deviceProfile = args.deviceCanProfile,
                    deviceFactory = args.deviceCanFactory,
                    noUnit = args.noUnit,
                )
            } else {
                null
            },
            viewOnly = args.viewOnly || args.noUnit,
            vehiclePicker = core.vehiclePicker,
            deviceName = args.deviceName,
            noUnit = args.noUnit,
            moduleName = args.moduleName,
            noModelFirmware = args.noUnit && core.load is LCEState.Content &&
                (core.noModule || (rows.isEmpty() && core.canApplied == null)),
            busy = core.busy,
            saved = core.saved,
            exportError = core.exportError,
            saveTarget = exporter?.target ?: FirmwareSaveTarget.Downloads,
            canShare = exporter?.canShare == true,
            canOpenFolder = exporter?.canOpenFolder == true,
        )
    }

    /**
     * Отношение к установленной версии. «Установлена» у сборки под CAN — только под CAN-код и профиль блока: тот же
     * номер под другой CAN — другой файл, его установка не переустановка.
     */
    private fun kindOf(version: FirmwareVersion): ArchiveRowKind {
        val installed = args.installedVersion ?: return ArchiveRowKind.New
        return when (version.version.relationTo(installed)) {
            VersionRelation.Newer -> ArchiveRowKind.New
            VersionRelation.Same -> if (isDeviceBuild(version)) ArchiveRowKind.Installed else ArchiveRowKind.New
            VersionRelation.Older -> ArchiveRowKind.Older
        }
    }

    /** Сборка без CAN — любая; под CAN — под код блока и (если обе известны) его версию профиля. */
    private fun isDeviceBuild(version: FirmwareVersion): Boolean {
        val code = version.canCode ?: return true
        val deviceCode = args.deviceCanCode ?: return !args.deviceCanFactory
        val profile = version.canVersionCode
        val deviceProfile = args.deviceCanVersion
        return code == deviceCode && (profile == null || deviceProfile == null || profile == deviceProfile)
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
                rollback = null,
                noModule = false,
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
        val start = core.value
        val filter = start.canApplied.takeIf { canFilterOn }
        val moduleId = when (val module = moduleId()) {
            is Response.Error -> {
                core.update {
                    it.copy(load = LCEState.Error(FirmwareError.from(module.exception)), loadingMore = false)
                }
                return
            }

            // У устройства нет основной программы — версий нет.
            is Response.Success -> module.value ?: run {
                core.update {
                    it.copy(load = LCEState.Content(Unit), hasMore = false, loadingMore = false, noModule = true)
                }
                return
            }
        }
        val response = withContext(dispatchers.io) {
            repository.archivePage(moduleId, start.channel, page, PAGE_SIZE, can = canFilterOf(filter))
        }
        // Пока страница грузилась, фильтр, модуль или канал сменили или список начали заново: ответ не нужен.
        val now = core.value
        if (now.canApplied != start.canApplied || now.channel != start.channel) return
        if (!now.awaits(page)) return
        when (response) {
            is Response.Success -> core.update { state ->
                val known = state.items.mapTo(mutableSetOf()) { it.key }
                val fresh = response.value.items.filter { known.add(it.key) }
                state.copy(
                    load = LCEState.Content(Unit),
                    items = state.items + fresh,
                    page = page,
                    // Нет новых версий — конец (источник признак не отдаёт; см. KDoc класса).
                    hasMore = fresh.isNotEmpty(),
                    loadingMore = false,
                    moreError = null,
                )
            }

            is Response.Error -> core.update { state ->
                if (page == FIRST_PAGE) {
                    state.copy(
                        load = LCEState.Error(FirmwareError.from(response.exception)),
                        loadingMore = false,
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

    /** Модуль архива: из запроса или основная программа устройства [FirmwareArchiveRequest.deviceName] (один раз). */
    private suspend fun moduleId(): Response<Long?> {
        core.value.moduleId?.let { return Response.Success(it) }
        val deviceName = args.deviceName ?: return Response.Success(null)
        val response = withContext(dispatchers.io) { repository.moduleId(deviceName, FirmwareModuleKind.AlarmMain) }
        if (response is Response.Success) response.value?.let { id -> core.update { it.copy(moduleId = id) } }
        return response
    }

    // ───── без блока: канал, файл ─────

    /** Канал в шапке (только без блока): список заново. */
    fun onChannelSelect(channel: FirmwareChannel) {
        if (!args.noUnit) return
        if (!core.updateIf({ it.channel != channel }) { it.copy(channel = channel, selected = null) }) return
        loadFirstPage()
    }

    /** «Скачать»: файл версии [key] — в «Загрузки» / папку платформы, затем диалог «Прошивка скачана». */
    fun onDownload(key: String) = export(key) { item, bytes ->
        val exporter = exporter ?: return@export
        val fileName = fileNameOf(item)
        when (val result = exporter.save(fileName, bytes)) {
            is FirmwareSaveResult.Saved -> core.update {
                it.copy(saved = ArchiveSavedUi(key, fileName, result.path, result.folder))
            }

            FirmwareSaveResult.Cancelled -> Unit

            FirmwareSaveResult.PermissionDenied -> core.update { it.copy(exportError = ArchiveExportError.Permission) }

            FirmwareSaveResult.Failed -> core.update { it.copy(exportError = ArchiveExportError.Save) }
        }
    }

    /** «Поделиться»: файл версии [key] — системному меню. */
    fun onShare(key: String) = export(key) { item, bytes ->
        val exporter = exporter ?: return@export
        if (!exporter.share(fileNameOf(item), bytes)) {
            core.update { it.copy(exportError = ArchiveExportError.Share) }
        }
    }

    /** «Поделиться» в диалоге «Прошивка скачана». */
    fun onSavedShare() {
        val saved = core.value.saved ?: return
        core.update { it.copy(saved = null) }
        onShare(saved.key)
    }

    /** «Открыть папку» в диалоге «Прошивка скачана» (desktop). */
    fun onSavedOpenFolder() {
        val saved = core.value.saved ?: return
        core.update { it.copy(saved = null) }
        val exporter = exporter ?: return
        safeLaunch {
            if (!exporter.openFolder(saved.path)) core.update { it.copy(exportError = ArchiveExportError.Save) }
        }
    }

    fun onSavedDismiss() {
        core.update { it.copy(saved = null) }
    }

    fun onExportErrorDismiss() {
        core.update { it.copy(exportError = null) }
    }

    /** Скачать файл версии [key] и отдать [use]; одна загрузка за раз, ошибка скачивания — баннер. */
    private fun export(key: String, use: suspend (FirmwareVersion, ByteArray) -> Unit) {
        if (!args.noUnit) return
        val item = itemOf(key) ?: return
        if (!core.updateIf({ it.busy == null }) { it.copy(busy = key, exportError = null) }) return
        safeLaunch {
            try {
                when (val response = withContext(dispatchers.io) { repository.download(item.url) }) {
                    is Response.Success -> use(item, response.value)

                    is Response.Error -> core.update {
                        it.copy(exportError = ArchiveExportError.Download(FirmwareError.from(response.exception)))
                    }
                }
            } finally {
                core.update { it.copy(busy = null) }
            }
        }
    }

    /** `<имя подсистемы>_3.61[_CAN2154-1.03].<расширение файла с сервера>`. */
    private fun fileNameOf(item: FirmwareVersion): String {
        val extension = item.url.substringBefore('?').substringAfterLast('/').substringAfterLast('.', "")
            .takeIf { it.isNotBlank() && it.length <= MAX_EXTENSION } ?: DEFAULT_EXTENSION
        val can = item.canCode?.let { code -> "_CAN$code" + item.canVersion?.let { "-$it" }.orEmpty() }.orEmpty()
        // Ревизии сервера — с пробелом («3.57 C»): в имени файла без пробелов.
        val version = item.version.filterNot(Char::isWhitespace)
        val base = (args.moduleName ?: args.deviceName.orEmpty()).filterNot(Char::isWhitespace)
        return "${base}_$version$can.$extension"
    }

    // ───── CAN-фильтр ─────

    /** Фильтр запроса: код из поля; версия CAN-профиля блока — только к коду блока (к другому не относится). */
    private fun canFilterOf(code: String?): FirmwareCan? =
        code?.let { FirmwareCan(it, if (it == args.deviceCanCode) args.deviceCanVersion else null) }

    /** «Вернуть CAN блока»: фильтр снова по коду и версии CAN-профиля блока. */
    fun onCanResetToDevice() {
        val code = args.deviceCanCode ?: return
        canText.update { code }
        core.update { it.copy(canInput = code, carTitle = null) }
        applyCanFilter(code)
    }

    /** Ввод в поле CAN-кода: список перезапрашивается на полном коде и при очистке поля. */
    fun onCanInput(text: String) {
        val code = text.filter(Char::isDigit).take(CAN_CODE_DIGITS)
        canText.update { code }
        core.update { it.copy(canInput = code, carTitle = null) }
        when (code.length) {
            0 -> applyCanFilter(null)
            CAN_CODE_DIGITS -> applyCanFilter(code)
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
        val code = canCode.filter(Char::isDigit).take(CAN_CODE_DIGITS)
        canText.update { code }
        core.update { it.copy(canInput = code, carTitle = carTitle, vehiclePicker = false) }
        applyCanFilter(code.ifEmpty { null })
    }

    private fun applyCanFilter(code: String?) {
        if (core.value.canApplied == code) return
        core.update { it.copy(canApplied = code) }
        loadFirstPage()
    }

    // ───── выбор ─────

    /** [key] — [ArchiveRowUi.key]. */
    fun onSelectRow(key: String) {
        core.update { it.copy(selected = key) }
    }

    /** «Выбрать» / «Установить» строки [key]: старее установленной — через подтверждение, иначе сразу. */
    fun onPick(key: String) {
        if (args.viewOnly) return
        val item = itemOf(key) ?: return
        if (kindOf(item) == ArchiveRowKind.Older && args.installedVersion != null) {
            core.update { it.copy(rollback = key) }
        } else {
            pick(item)
        }
    }

    fun onRollbackConfirm() {
        val key = core.value.rollback ?: return
        core.update { it.copy(rollback = null) }
        itemOf(key)?.let(::pick)
    }

    private fun itemOf(key: String): FirmwareVersion? = core.value.items.firstOrNull { it.key == key }

    private val FirmwareVersion.key: String get() = archiveRowKey(version, canCode, canVersion)

    fun onRollbackDismiss() {
        core.update { it.copy(rollback = null) }
    }

    private fun pick(version: FirmwareVersion) {
        _effect.tryEmit(FirmwareArchiveEffect.Picked(version))
    }

    private companion object {
        const val FIRST_PAGE = 1
        const val PAGE_SIZE = 10
        const val MAX_EXTENSION = 5
        const val DEFAULT_EXTENSION = "bin"
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
