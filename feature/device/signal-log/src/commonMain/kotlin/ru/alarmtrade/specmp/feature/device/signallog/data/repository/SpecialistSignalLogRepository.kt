package ru.alarmtrade.specmp.feature.device.signallog.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.common.core.response.Response as SdkResponse
import ru.alarmtrade.specialist.domain.errors.ServiceLogEmptyException
import ru.alarmtrade.specialist.domain.errors.ServiceLogQueuedException
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogLinesAfterRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogPageRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogReportRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSearchRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSerialRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.signallog.data.datasource.SignalLogDataSource
import ru.alarmtrade.specmp.feature.device.signallog.data.mapper.toSignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.data.mapper.toSignalLogLines
import ru.alarmtrade.specmp.feature.device.signallog.data.mapper.toSignalLogSummary
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogEmptyException
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSendResult
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary
import ru.alarmtrade.specmp.feature.device.signallog.domain.repository.SignalLogRepository

internal class SpecialistSignalLogRepository(private val dataSource: SignalLogDataSource) : SignalLogRepository {

    override fun newLines(serial: String): Flow<Response<SignalLogLine>> =
        dataSource.newLines(ServiceLogSerialRequest(serial)).map { line ->
            line.toResponse().map { it.toSignalLogLine() }
        }

    override suspend fun page(serial: String, beforeId: Long?, limit: Int): Response<List<SignalLogLine>> =
        dataSource.page(ServiceLogPageRequest(serial, beforeId, limit)).toResponse().map { it.toSignalLogLines() }

    override suspend fun linesAfter(serial: String, afterId: Long): Response<List<SignalLogLine>> =
        dataSource.linesAfter(ServiceLogLinesAfterRequest(serial, afterId)).toResponse().map { it.toSignalLogLines() }

    override suspend fun summary(serial: String): Response<SignalLogSummary> =
        dataSource.summary(ServiceLogSerialRequest(serial)).toResponse().map { it.toSignalLogSummary() }

    override suspend fun search(serial: String, query: String, limit: Int): Response<List<SignalLogLine>> =
        dataSource.search(ServiceLogSearchRequest(serial, query, limit)).toResponse().map { it.toSignalLogLines() }

    override suspend fun clear(serial: String): Response<Unit> =
        dataSource.clear(ServiceLogSerialRequest(serial)).toResponse()

    override suspend fun buildReport(serial: String, problemTitle: String, problem: String): Response<String> {
        val report = dataSource.buildReport(ServiceLogReportRequest(serial, problemTitle, problem))
        return if (report is SdkResponse.Error && report.exception is ServiceLogEmptyException) {
            Response.Error(SignalLogEmptyException())
        } else {
            report.toResponse()
        }
    }

    override suspend fun sendToSupport(
        serial: String,
        problemTitle: String,
        problem: String,
    ): Response<SignalLogSendResult> =
        when (val sent = dataSource.sendToSupport(ServiceLogReportRequest(serial, problemTitle, problem))) {
            is SdkResponse.Success -> Response.Success(SignalLogSendResult.Sent)

            is SdkResponse.Error -> when (sent.exception) {
                is ServiceLogQueuedException -> Response.Success(SignalLogSendResult.Queued)
                is ServiceLogEmptyException -> Response.Success(SignalLogSendResult.Empty)
                else -> sent.toResponse()
            }
        }
}
