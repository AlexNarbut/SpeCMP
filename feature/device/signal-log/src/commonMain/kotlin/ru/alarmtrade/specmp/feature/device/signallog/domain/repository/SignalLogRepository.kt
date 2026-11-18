package ru.alarmtrade.specmp.feature.device.signallog.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogEmptyException
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSendResult
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary

/**
 * Служебный лог блока, который SDK пишет сам всю BLE-сессию. [serial] — серийный номер из BLE manufacturer data
 * (`DeviceSession.readBluetoothModule()`), по нему SDK хранит лог.
 */
interface SignalLogRepository {
    /** Только строки, записанные после подписки. */
    fun newLines(serial: String): Flow<Response<SignalLogLine>>

    /** Новые первыми: последние [limit] строк или [limit] строк до [beforeId]. */
    suspend fun page(serial: String, beforeId: Long?, limit: Int): Response<List<SignalLogLine>>

    /** Все строки после [afterId] по возрастанию id. */
    suspend fun linesAfter(serial: String, afterId: Long): Response<List<SignalLogLine>>

    suspend fun summary(serial: String): Response<SignalLogSummary>

    /** Первые [limit] строк с [query] без учёта регистра, по возрастанию id. */
    suspend fun search(serial: String, query: String, limit: Int): Response<List<SignalLogLine>>

    suspend fun clear(serial: String): Response<Unit>

    /** Текст для файла; пустой лог — `Response.Error` с [SignalLogEmptyException]. */
    suspend fun buildReport(serial: String, problemTitle: String, problem: String): Response<String>

    suspend fun sendToSupport(serial: String, problemTitle: String, problem: String): Response<SignalLogSendResult>
}
