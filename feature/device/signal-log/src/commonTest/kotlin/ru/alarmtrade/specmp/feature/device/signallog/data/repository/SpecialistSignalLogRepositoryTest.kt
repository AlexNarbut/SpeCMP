package ru.alarmtrade.specmp.feature.device.signallog.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.errors.ServiceLogEmptyException
import ru.alarmtrade.specialist.domain.errors.ServiceLogQueuedException
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogLine
import ru.alarmtrade.specialist.domain.serviceLog.ServiceLogSummary
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogPageRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogReportRequest
import ru.alarmtrade.specialist.interactors.serviceLog.requests.ServiceLogSearchRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogEmptyException
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogLine
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSendResult
import ru.alarmtrade.specmp.feature.device.signallog.domain.model.SignalLogSummary

class SpecialistSignalLogRepositoryTest {
    private val dataSource = FakeSignalLogDataSource()
    private val repository = SpecialistSignalLogRepository(dataSource)

    @Test
    fun `new lines are mapped and errors pass through the stream`() = runTest {
        val failure = IllegalStateException("db")
        dataSource.newLines = flowOf(
            SpecialistResponse.Success(ServiceLogLine(id = 7, timeMs = 1_000, text = "ARM")),
            SpecialistResponse.Error.General(failure),
        )

        val values = repository.newLines(SERIAL).toList()

        assertEquals(Response.Success(SignalLogLine(id = 7, timeMs = 1_000, text = "ARM")), values[0])
        assertSame(failure, assertIs<Response.Error>(values[1]).exception)
    }

    @Test
    fun `page keeps the sdk order and sends the cursor`() = runTest {
        dataSource.lines = SpecialistResponse.Success(listOf(ServiceLogLine(9, 20, "b"), ServiceLogLine(8, 10, "a")))

        val page = repository.page(SERIAL, beforeId = 10, limit = 2)

        assertEquals(Response.Success(listOf(SignalLogLine(9, 20, "b"), SignalLogLine(8, 10, "a"))), page)
        val request = assertIs<ServiceLogPageRequest>(dataSource.requests.single())
        assertEquals(Triple(SERIAL, 10L, 2), Triple(request.serial, request.beforeId, request.limit))
    }

    @Test
    fun `search sends the query`() = runTest {
        dataSource.lines = SpecialistResponse.Success(listOf(ServiceLogLine(3, 5, "Тревога")))

        assertEquals(Response.Success(listOf(SignalLogLine(3, 5, "Тревога"))), repository.search(SERIAL, "тре", 50))
        val request = assertIs<ServiceLogSearchRequest>(dataSource.requests.single())
        assertEquals(Triple(SERIAL, "тре", 50), Triple(request.serial, request.query, request.limit))
    }

    @Test
    fun `summary is mapped`() = runTest {
        dataSource.summary = SpecialistResponse.Success(ServiceLogSummary(count = 2, firstTimeMs = 1, lastTimeMs = 5))

        assertEquals(
            Response.Success(SignalLogSummary(count = 2, firstTimeMs = 1, lastTimeMs = 5)),
            repository.summary(SERIAL),
        )
    }

    @Test
    fun `sdk network error becomes app error`() = runTest {
        dataSource.summary = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val error = assertIs<Response.Error>(repository.summary(SERIAL))

        assertEquals(ApiError.NoConnection, assertIs<AppException>(error.exception).error)
    }

    @Test
    fun `report passes the problem title from the caller`() = runTest {
        dataSource.report = SpecialistResponse.Success("Проблема: не ставится\n\n18-11-2026 10:15:02 ARM")

        val report = repository.buildReport(SERIAL, problemTitle = "Проблема", problem = "не ставится")

        assertEquals(Response.Success("Проблема: не ставится\n\n18-11-2026 10:15:02 ARM"), report)
        val request = assertIs<ServiceLogReportRequest>(dataSource.requests.single())
        assertEquals(
            Triple(SERIAL, "Проблема", "не ставится"),
            Triple(request.serial, request.problemTitle, request.problem),
        )
    }

    @Test
    fun `report of an empty log is a typed error`() = runTest {
        dataSource.report = SpecialistResponse.Error.General(ServiceLogEmptyException())

        assertIs<SignalLogEmptyException>(assertIs<Response.Error>(repository.buildReport(SERIAL, "P", "x")).exception)
    }

    @Test
    fun `send success is sent`() = runTest {
        dataSource.send = SpecialistResponse.Success(Unit)

        assertEquals(Response.Success(SignalLogSendResult.Sent), repository.sendToSupport(SERIAL, "P", "x"))
    }

    @Test
    fun `send without server is queued`() = runTest {
        dataSource.send = SpecialistResponse.Error.Network(ServiceLogQueuedException())

        assertEquals(Response.Success(SignalLogSendResult.Queued), repository.sendToSupport(SERIAL, "P", "x"))
    }

    @Test
    fun `send of an empty log is empty`() = runTest {
        dataSource.send = SpecialistResponse.Error.General(ServiceLogEmptyException())

        assertEquals(Response.Success(SignalLogSendResult.Empty), repository.sendToSupport(SERIAL, "P", "x"))
    }

    @Test
    fun `other send error fails`() = runTest {
        val failure = IllegalStateException("server")
        dataSource.send = SpecialistResponse.Error.General(failure)

        assertSame(failure, assertIs<Response.Error>(repository.sendToSupport(SERIAL, "P", "x")).exception)
    }

    private companion object {
        const val SERIAL = "1234567"
    }
}
