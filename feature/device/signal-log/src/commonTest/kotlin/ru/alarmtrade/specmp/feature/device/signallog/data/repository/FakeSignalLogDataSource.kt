package ru.alarmtrade.specmp.feature.device.signallog.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogLine
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogSummary
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogLinesAfterRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogPageRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogReportRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSearchRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSerialRequest
import ru.alarmtrade.specmp.feature.device.signallog.data.datasource.SignalLogDataSource

internal class FakeSignalLogDataSource : SignalLogDataSource {
    var newLines: Flow<SpecialistResponse<ServiceLogLine>> = emptyFlow()
    var lines: SpecialistResponse<List<ServiceLogLine>> = SpecialistResponse.Success(emptyList())
    var summary: SpecialistResponse<ServiceLogSummary> = SpecialistResponse.Success(ServiceLogSummary(0, null, null))
    var clear: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)
    var report: SpecialistResponse<String> = SpecialistResponse.Success("")
    var send: SpecialistResponse<Unit> = SpecialistResponse.Success(Unit)

    val requests = mutableListOf<Any>()

    override fun newLines(request: ServiceLogSerialRequest): Flow<SpecialistResponse<ServiceLogLine>> {
        requests += request
        return newLines
    }

    override suspend fun page(request: ServiceLogPageRequest): SpecialistResponse<List<ServiceLogLine>> {
        requests += request
        return lines
    }

    override suspend fun linesAfter(request: ServiceLogLinesAfterRequest): SpecialistResponse<List<ServiceLogLine>> {
        requests += request
        return lines
    }

    override suspend fun summary(request: ServiceLogSerialRequest): SpecialistResponse<ServiceLogSummary> {
        requests += request
        return summary
    }

    override suspend fun search(request: ServiceLogSearchRequest): SpecialistResponse<List<ServiceLogLine>> {
        requests += request
        return lines
    }

    override suspend fun clear(request: ServiceLogSerialRequest): SpecialistResponse<Unit> {
        requests += request
        return clear
    }

    override suspend fun buildReport(request: ServiceLogReportRequest): SpecialistResponse<String> {
        requests += request
        return report
    }

    override suspend fun sendToSupport(request: ServiceLogReportRequest): SpecialistResponse<Unit> {
        requests += request
        return send
    }
}
