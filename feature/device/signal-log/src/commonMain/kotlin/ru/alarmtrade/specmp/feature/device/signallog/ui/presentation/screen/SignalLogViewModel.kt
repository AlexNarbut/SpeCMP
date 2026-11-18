package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.time.TimeZoneSource
import ru.alarmtrade.specmp.core.files.TextFileSaveResult
import ru.alarmtrade.specmp.core.files.TextFileSaver
import ru.alarmtrade.specmp.core.files.TextFileType
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel.BaseDeviceViewModel
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogEmptyException
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSendResult
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.signalLogFileName
import ru.alarmtrade.specmp.feature.device.signallog.domain.repository.SignalLogRepository

/** Лог читается из БД SDK и без связи: только serial блока требует подключения (читается один раз). */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
internal class SignalLogViewModel(
    deviceSession: DeviceSession,
    private val repository: SignalLogRepository,
    private val fileSaver: TextFileSaver,
    private val dispatchers: AppDispatchers,
    private val clock: Clock,
    private val timeZones: TimeZoneSource,
) : BaseDeviceViewModel<SignalLogState, SignalLogEffect>(deviceSession, initialState(deviceSession)) {

    override suspend fun IntentScope.onStart() {
        onEachDeviceReady { device ->
            subIntent { reduce { copy(deviceName = device.systemName, connected = true) } }
            if (currentState.serial == null) open()
        }
        onDeviceLost(::onConnectionLost)
        select { search?.query?.trim().orEmpty() }
            .debounce(SEARCH_DEBOUNCE)
            .mapLatest { query -> query to findMatches(query) }
            .flowOn(dispatchers.io)
            .collectIntoState { (query, matches) -> withMatches(query, matches) }
        intent {
            select { search?.takeUnless { it.onlyMatches }?.currentLine?.id }
                .filterNotNull()
                .collectLatest { id -> reveal(id) }
        }
    }

    // Без связи новых строк нет, а кнопки «Продолжить» в полосе «Нет связи» нет — пауза снимается сама.
    private suspend fun onConnectionLost() = subIntent {
        reduce {
            val noSerial = serial == null && load !is LCEState.Content
            copy(connected = false, load = if (noSerial) LCEState.Error(SignalLogError.NO_SERIAL) else load)
        }
        resume()
    }

    fun onRetry() {
        intent {
            val serial = currentState.serial
            if (serial == null) open() else launchReplacing(LOAD) { loadInitial(serial) }
        }
    }

    private fun open() {
        launchReplacing(LOAD) {
            reduce { copy(load = LCEState.Loading) }
            val module = withContext(dispatchers.io) { deviceSession.readBluetoothModule() }
            val serial = module.getValueIfSuccess()?.manufacturer?.serialNumber?.takeIf { it.isNotBlank() }
            if (serial == null) {
                reduce { copy(load = LCEState.Error(SignalLogError.NO_SERIAL)) }
                return@launchReplacing
            }
            reduce { copy(serial = serial) }
            // Подписка — до первой страницы: строка, записанная между ними, не потеряется (дубли отсекает слияние).
            launchReplacing(LIVE) {
                repository.newLines(serial).flowOn(dispatchers.io).collect { line ->
                    if (line is Response.Success) reduce { withLiveLine(line.value) }
                }
            }
            loadInitial(serial)
        }
    }

    private suspend fun loadInitial(serial: String) = subIntent {
        reduce { copy(load = LCEState.Loading) }
        val (page, summary) = withContext(dispatchers.io) {
            coroutineScope {
                val page = async { repository.page(serial, beforeId = null, limit = PAGE_SIZE) }
                val summary = async { repository.summary(serial) }
                page.await() to summary.await()
            }
        }
        if (page is Response.Success && summary is Response.Success) {
            reduce {
                withLines(lines.mergeNewestFirst(page.value)).copy(
                    load = LCEState.Content(Unit),
                    hasOlder = page.value.size >= PAGE_SIZE,
                    summary = summary.value,
                )
            }
        } else {
            reduce { copy(load = LCEState.Error(SignalLogError.LOAD_FAILED)) }
        }
    }

    fun onLoadOlder() {
        intent { executeWithProceedLock { loadOlder(PAGE_SIZE) } }
    }

    /** `false` — страница не пришла: дальше не грузим. */
    private suspend fun loadOlder(limit: Int): Boolean = subIntent {
        val state = currentState.takeIf { it.hasOlder && it.loaded }
        val serial = state?.serial
        val oldest = state?.lines?.lastOrNull()?.id
        if (serial == null || oldest == null) return@subIntent false
        reduce { copy(loadingOlder = true) }
        val page = withContext(dispatchers.io) { repository.page(serial, beforeId = oldest, limit = limit) }
        if (page is Response.Success) {
            reduce {
                withLines(lines.mergeNewestFirst(page.value))
                    .copy(hasOlder = page.value.size >= limit, loadingOlder = false)
            }
            true
        } else {
            reduce { copy(loadingOlder = false) }
            postSideEffect(SignalLogEffect.Failed(SignalLogOperation.OLDER))
            false
        }
    }

    /** Совпадение раньше загруженного окна: догружаем страницы до него, чтобы лента могла к нему перейти. */
    private suspend fun reveal(lineId: Long) = subIntent {
        executeWithProceedLock(needSkipAnother = false) {
            while (currentState.needsOlderFor(lineId)) {
                if (!loadOlder(REVEAL_PAGE_SIZE)) break
            }
        }
    }

    fun onPauseToggle() {
        intent {
            val state = currentState
            when {
                state.paused -> resume()
                state.hasLines && state.connected -> reduce { copy(paused = true, pendingCount = 0) }
            }
        }
    }

    /** Строки, пришедшие после снятия паузы, добавит подписка; запрос догружает только пропущенное. */
    private suspend fun resume() = subIntent {
        val state = currentState
        if (!state.paused) return@subIntent
        val shownUpTo = state.lines.firstOrNull()?.id ?: 0L
        reduce { copy(paused = false, pendingCount = 0) }
        val serial = state.serial ?: return@subIntent
        val missed = withContext(dispatchers.io) { repository.linesAfter(serial, shownUpTo) }
        if (missed is Response.Success) reduce { withLines(lines.mergeNewestFirst(missed.value)) }
    }

    fun onClearRequest() {
        intent {
            val state = currentState
            val serial = state.serial
            if (!state.hasLines || serial == null) return@intent
            val summary = withContext(dispatchers.io) { repository.summary(serial) }.getValueIfSuccess()
                ?: state.summary
            reduce { copy(summary = summary, dialog = SignalLogDialog.ConfirmClear(summary.count)) }
        }
    }

    fun onDialogDismiss() {
        intent { reduce { copy(dialog = null) } }
    }

    fun onClearConfirm() {
        intent {
            val serial = currentState.serial ?: return@intent
            if (currentState.dialog !is SignalLogDialog.ConfirmClear) return@intent
            reduce { copy(dialog = null) }
            when (withContext(dispatchers.io) { repository.clear(serial) }) {
                is Response.Success -> {
                    reduce { cleared() }
                    postSideEffect(SignalLogEffect.Cleared)
                }

                is Response.Error -> postSideEffect(SignalLogEffect.Failed(SignalLogOperation.CLEAR))
            }
        }
    }

    fun onShareOpen() {
        intent {
            val serial = currentState.serial
            if (!currentState.hasLines || serial == null) return@intent
            reduce { copy(sheetOpen = true) }
            withContext(dispatchers.io) { repository.summary(serial) }.getValueIfSuccess()
                ?.let { summary -> reduce { copy(summary = summary) } }
        }
    }

    /** Закрытие листа не отменяет начатую отправку: итог придёт снекбаром. */
    fun onShareDismiss() {
        intent { reduce { copy(sheetOpen = false, share = share.copy(problemError = false, sendFailed = false)) } }
    }

    fun onProblemChange(text: String) {
        intent {
            val problem = text.take(PROBLEM_MAX_LENGTH)
            reduce {
                copy(share = share.copy(problem = problem, problemError = share.problemError && problem.isBlank()))
            }
        }
    }

    /** [problemTitle] — локализованное «Проблема»: первая строка отчёта. */
    fun onSend(problemTitle: String) {
        submit(SignalLogShareAction.SEND, problemTitle)
    }

    fun onSave(problemTitle: String) {
        submit(SignalLogShareAction.SAVE, problemTitle)
    }

    private fun submit(action: SignalLogShareAction, problemTitle: String) {
        intent {
            val state = currentState
            val serial = state.serial
            if (state.share.busy != null || !state.hasLines || serial == null) return@intent
            val problem = state.share.problem.trim()
            if (problem.isEmpty()) {
                reduce { copy(share = share.copy(problemError = true)) }
                return@intent
            }
            reduce { copy(share = share.copy(busy = action, problemError = false, sendFailed = false)) }
            when (action) {
                SignalLogShareAction.SEND -> send(serial, problemTitle, problem)
                SignalLogShareAction.SAVE -> save(serial, problemTitle, problem)
            }
        }
    }

    private suspend fun send(serial: String, problemTitle: String, problem: String) = subIntent {
        when (val sent = withContext(dispatchers.io) { repository.sendToSupport(serial, problemTitle, problem) }) {
            is Response.Success -> when (sent.value) {
                SignalLogSendResult.Sent -> closeShare(SignalLogEffect.Sent)

                SignalLogSendResult.Queued -> closeShare(SignalLogEffect.Queued)

                SignalLogSendResult.Empty -> {
                    reduce { copy(share = share.copy(busy = null)) }
                    postSideEffect(SignalLogEffect.Empty)
                }
            }

            is Response.Error -> reduce { copy(share = share.copy(busy = null, sendFailed = true)) }
        }
    }

    private suspend fun closeShare(effect: SignalLogEffect) = subIntent {
        reduce { copy(sheetOpen = false, share = SignalLogShareForm()) }
        postSideEffect(effect)
    }

    private suspend fun save(serial: String, problemTitle: String, problem: String) = subIntent {
        val report = withContext(dispatchers.io) { repository.buildReport(serial, problemTitle, problem) }
        if (report !is Response.Success) {
            reduce { copy(share = share.copy(busy = null)) }
            val empty = (report as? Response.Error)?.exception is SignalLogEmptyException
            postSideEffect(if (empty) SignalLogEffect.Empty else SignalLogEffect.Failed(SignalLogOperation.SAVE))
            return@subIntent
        }
        val name = signalLogFileName(serial, clock.now().toEpochMilliseconds(), timeZones.current())
        when (val saved = fileSaver.save(name, TextFileType.Txt, report.value)) {
            is TextFileSaveResult.Saved -> {
                reduce { copy(sheetOpen = false, share = share.copy(busy = null)) }
                postSideEffect(SignalLogEffect.Saved(saved.name))
            }

            TextFileSaveResult.Cancelled -> reduce { copy(share = share.copy(busy = null)) }

            TextFileSaveResult.Failed -> {
                reduce { copy(share = share.copy(busy = null)) }
                postSideEffect(SignalLogEffect.Failed(SignalLogOperation.SAVE))
            }
        }
    }

    fun onSearchOpen() {
        intent { reduce { if (hasLines && search == null) copy(search = SignalLogSearch()) else this } }
    }

    fun onSearchClose() {
        intent { reduce { copy(search = null) } }
    }

    fun onSearchQuery(query: String) {
        intent {
            reduce {
                val search = search ?: return@reduce this
                val changed = search.query.trim() != query.trim()
                copy(
                    search = if (changed) {
                        SignalLogSearch(query = query, onlyMatches = search.onlyMatches && query.isNotBlank())
                    } else {
                        search.copy(query = query)
                    },
                )
            }
        }
    }

    fun onSearchStep(older: Boolean) {
        intent {
            reduce {
                val search = search ?: return@reduce this
                if (search.matches.isEmpty()) return@reduce this
                val next = (search.current + if (older) -1 else 1).coerceIn(0, search.matches.lastIndex)
                copy(search = search.copy(current = next))
            }
        }
    }

    fun onOnlyMatchesToggle() {
        intent {
            reduce {
                val search = search?.takeIf { it.query.isNotBlank() } ?: return@reduce this
                copy(search = search.copy(onlyMatches = !search.onlyMatches))
            }
        }
    }

    private suspend fun findMatches(query: String): List<SignalLogLine> {
        val serial = currentState.serial
        if (query.isEmpty() || serial == null) return emptyList()
        return repository.search(serial, query, SEARCH_LIMIT).getValueIfSuccess().orEmpty()
    }

    private companion object {
        const val PAGE_SIZE = 100
        const val REVEAL_PAGE_SIZE = 500
        const val SEARCH_LIMIT = MAX_LINES
        const val PROBLEM_MAX_LENGTH = 1000
        const val LOAD = "load"
        const val LIVE = "live"
        val SEARCH_DEBOUNCE = 300.milliseconds
    }
}

private fun initialState(deviceSession: DeviceSession): SignalLogState {
    val device = (deviceSession.state.value as? DeviceSessionState.Ready)?.device
    return SignalLogState(deviceName = device?.systemName, connected = device != null)
}

private fun SignalLogState.cleared(): SignalLogState = copy(
    lines = persistentListOf(),
    hasOlder = false,
    summary = EmptySummary,
    paused = false,
    pendingCount = 0,
    search = null,
    sheetOpen = false,
    share = SignalLogShareForm(),
)
