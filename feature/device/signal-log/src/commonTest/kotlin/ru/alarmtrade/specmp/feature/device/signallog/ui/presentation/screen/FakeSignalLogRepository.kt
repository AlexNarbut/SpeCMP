package ru.alarmtrade.specmp.feature.device.signallog.ui.presentation.screen

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSendResult
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary
import ru.alarmtrade.specmp.feature.device.signallog.domain.repository.SignalLogRepository

/** Лог блока — [log] (по возрастанию id); страницы, догрузка и поиск считаются по нему, как в SDK. */
internal class FakeSignalLogRepository(var log: List<SignalLogLine> = emptyList()) : SignalLogRepository {
    val live = MutableSharedFlow<Response<SignalLogLine>>(extraBufferCapacity = 64)
    var pageError: Throwable? = null
    var clearResult: Response<Unit> = Response.Success(Unit)
    var sendResult: Response<SignalLogSendResult> = Response.Success(SignalLogSendResult.Sent)
    var reportResult: Response<String>? = null

    val pages = mutableListOf<Pair<Long?, Int>>()
    val afterRequests = mutableListOf<Long>()
    val searches = mutableListOf<String>()
    val sends = mutableListOf<Pair<String, String>>()
    val reports = mutableListOf<Pair<String, String>>()
    var clears = 0
        private set

    fun record(line: SignalLogLine) {
        log = log + line
        live.tryEmit(Response.Success(line))
    }

    override fun newLines(serial: String): Flow<Response<SignalLogLine>> = live

    override suspend fun page(serial: String, beforeId: Long?, limit: Int): Response<List<SignalLogLine>> {
        pages += beforeId to limit
        pageError?.let { return Response.Error(it) }
        return Response.Success(
            log.filter { beforeId == null || it.id < beforeId }.sortedByDescending { it.id }.take(limit),
        )
    }

    override suspend fun linesAfter(serial: String, afterId: Long): Response<List<SignalLogLine>> {
        afterRequests += afterId
        return Response.Success(log.filter { it.id > afterId })
    }

    override suspend fun summary(serial: String): Response<SignalLogSummary> =
        Response.Success(SignalLogSummary(log.size, log.firstOrNull()?.timeMs, log.lastOrNull()?.timeMs))

    override suspend fun search(serial: String, query: String, limit: Int): Response<List<SignalLogLine>> {
        searches += query
        return Response.Success(log.filter { it.text.contains(query, ignoreCase = true) }.take(limit))
    }

    override suspend fun clear(serial: String): Response<Unit> {
        clears++
        if (clearResult is Response.Success) log = emptyList()
        return clearResult
    }

    override suspend fun buildReport(serial: String, problemTitle: String, problem: String): Response<String> {
        reports += problemTitle to problem
        return reportResult ?: Response.Success("$problemTitle: $problem")
    }

    override suspend fun sendToSupport(
        serial: String,
        problemTitle: String,
        problem: String,
    ): Response<SignalLogSendResult> {
        sends += problemTitle to problem
        return sendResult
    }
}
