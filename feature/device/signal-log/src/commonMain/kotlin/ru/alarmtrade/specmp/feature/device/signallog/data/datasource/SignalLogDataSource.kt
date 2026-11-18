package ru.alarmtrade.specmp.feature.device.signallog.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogLine
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogSummary
import ru.alarmtrade.specialist.interactors.serviceLog.AlarmDeviceServiceLogInteractor
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogLinesAfterRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogPageRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogReportRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSearchRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSerialRequest

internal interface SignalLogDataSource {
    fun newLines(request: ServiceLogSerialRequest): Flow<SpecialistResponse<ServiceLogLine>>
    suspend fun page(request: ServiceLogPageRequest): SpecialistResponse<List<ServiceLogLine>>
    suspend fun linesAfter(request: ServiceLogLinesAfterRequest): SpecialistResponse<List<ServiceLogLine>>
    suspend fun summary(request: ServiceLogSerialRequest): SpecialistResponse<ServiceLogSummary>
    suspend fun search(request: ServiceLogSearchRequest): SpecialistResponse<List<ServiceLogLine>>
    suspend fun clear(request: ServiceLogSerialRequest): SpecialistResponse<Unit>
    suspend fun buildReport(request: ServiceLogReportRequest): SpecialistResponse<String>
    suspend fun sendToSupport(request: ServiceLogReportRequest): SpecialistResponse<Unit>
}

internal class SpecialistSignalLogDataSource(private val interactor: AlarmDeviceServiceLogInteractor) :
    SignalLogDataSource {
    override fun newLines(request: ServiceLogSerialRequest): Flow<SpecialistResponse<ServiceLogLine>> =
        flow { emitAll(interactor.observeServiceLogUseCase.execute(request)) }

    override suspend fun page(request: ServiceLogPageRequest): SpecialistResponse<List<ServiceLogLine>> =
        interactor.getServiceLogPageUseCase.execute(request)

    override suspend fun linesAfter(request: ServiceLogLinesAfterRequest): SpecialistResponse<List<ServiceLogLine>> =
        interactor.getServiceLogLinesAfterUseCase.execute(request)

    override suspend fun summary(request: ServiceLogSerialRequest): SpecialistResponse<ServiceLogSummary> =
        interactor.getServiceLogSummaryUseCase.execute(request)

    override suspend fun search(request: ServiceLogSearchRequest): SpecialistResponse<List<ServiceLogLine>> =
        interactor.searchServiceLogUseCase.execute(request)

    override suspend fun clear(request: ServiceLogSerialRequest): SpecialistResponse<Unit> =
        interactor.clearServiceLogUseCase.execute(request)

    override suspend fun buildReport(request: ServiceLogReportRequest): SpecialistResponse<String> =
        interactor.buildServiceLogReportUseCase.execute(request)

    override suspend fun sendToSupport(request: ServiceLogReportRequest): SpecialistResponse<Unit> =
        interactor.sendServiceLogToSupportUseCase.execute(request)
}
