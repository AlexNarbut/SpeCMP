package ru.alarmtrade.specmp.feature.techrequests.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.userRequests.CreateUserRequestProgress
import ru.alarmtrade.specialist.domain.userRequests.TechSupportDeviceReadException
import ru.alarmtrade.specialist.domain.userRequests.TechSupportRequestLimitException
import ru.alarmtrade.specialist.domain.userRequests.TechSupportRequestTopic
import ru.alarmtrade.specialist.domain.userRequests.UserRequest
import ru.alarmtrade.specialist.domain.userRequests.UserRequestStatus
import ru.alarmtrade.specialist.domain.userRequests.UserRequestsPage
import ru.alarmtrade.specialist.interactors.techSupport.requests.CloseTechSupportRequest
import ru.alarmtrade.specialist.interactors.techSupport.requests.CreateTechSupportRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.techrequests.data.datasource.TechRequestsDataSource
import ru.alarmtrade.specmp.feature.techrequests.data.repository.SpecialistTechRequestsRepository
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestException
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestFailure
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestStatus
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestSubmitStep

class SpecialistTechRequestsRepositoryTest {

    private val source = FakeTechRequestsDataSource()
    private val repository = SpecialistTechRequestsRepository(source)

    @Test
    fun pageIsMappedWithUtcDate() = runTest {
        source.firstPage =
            SpecialistResponse.Success(UserRequestsPage(listOf(sdkRequest(5)), total = 1, hasMore = false))

        val page = assertIs<Response.Success<*>>(repository.loadFirstPage()).value
        val request = (page as ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestPage).items.single()

        assertEquals(5, request.id)
        assertEquals(TechRequestStatus.IN_PROCESS, request.status)
        assertEquals(Instant.parse("2026-11-13T14:05:00Z"), request.createdAt)
        assertEquals(false, page.hasMore)
    }

    @Test
    fun networkFailureIsCommonError() = runTest {
        source.firstPage = SpecialistResponse.Error.Network(NetworkIsNotAvailableException())

        val error = assertIs<Response.Error>(repository.loadFirstPage())

        assertEquals(ApiError.NoConnection, error.exception.appError)
    }

    @Test
    fun submitSendsTrimmedValuesAndE164Phone() = runTest {
        source.createSteps = flowOf(
            SpecialistResponse.Success(CreateUserRequestProgress.ReadingDevice),
            SpecialistResponse.Success(CreateUserRequestProgress.Sending),
            SpecialistResponse.Success(CreateUserRequestProgress.Created(sdkRequest(9))),
        )

        val steps = repository.submit(TechRequestDraft(" Иван ", "79001234567", " тест ")).toList()

        assertEquals("+79001234567", source.created?.phoneNumber)
        assertEquals("Иван", source.created?.userName)
        assertEquals("тест", source.created?.issue)
        assertEquals(
            listOf(TechRequestSubmitStep.ReadingDevice, TechRequestSubmitStep.Sending),
            steps.take(2).map { (it as Response.Success).value },
        )
        assertEquals(9, ((steps.last() as Response.Success).value as TechRequestSubmitStep.Created).request.id)
    }

    @Test
    fun sdkFailuresBecomeFeatureFailures() = runTest {
        source.createSteps = flowOf(
            SpecialistResponse.Error.General(TechSupportDeviceReadException(IllegalStateException("ble"))),
        )
        val deviceRead = repository.submit(TechRequestDraft("Иван", "79001234567", "тест")).toList().single()
        assertEquals(TechRequestFailure.DeviceRead, failureOf(deviceRead))

        source.createSteps = flowOf(SpecialistResponse.Error.General(TechSupportRequestLimitException("limit")))
        val limit = repository.submit(TechRequestDraft("Иван", "79001234567", "тест")).toList().single()
        assertEquals(TechRequestFailure.Limit, failureOf(limit))
    }

    @Test
    fun cancelAndCloseCallTheirUseCases() = runTest {
        repository.cancel(3)
        repository.close(4)

        assertEquals(listOf("cancel" to 3L, "close" to 4L), source.closed)
    }

    private fun failureOf(response: Response<*>): TechRequestFailure =
        assertIs<TechRequestException>(assertIs<Response.Error>(response).exception).failure
}

private fun sdkRequest(id: Long) = UserRequest(
    id = id,
    status = UserRequestStatus.IN_PROCESS,
    userName = "Иван",
    userPhone = "+79001234567",
    issue = "тест",
    creationDate = LocalDateTime(2026, 11, 13, 14, 5),
    topic = TechSupportRequestTopic,
)

private class FakeTechRequestsDataSource : TechRequestsDataSource {
    val updatesFlow = MutableSharedFlow<SpecialistResponse<List<UserRequest>>>()
    var firstPage: SpecialistResponse<UserRequestsPage> =
        SpecialistResponse.Success(UserRequestsPage(emptyList(), 0, false))
    var createSteps: Flow<SpecialistResponse<CreateUserRequestProgress>> = flowOf()
    var created: CreateTechSupportRequest? = null
    val closed = mutableListOf<Pair<String, Long>>()

    override fun updates(): Flow<SpecialistResponse<List<UserRequest>>> = updatesFlow

    override suspend fun loadFirstPage(): SpecialistResponse<UserRequestsPage> = firstPage

    override suspend fun loadNextPage(): SpecialistResponse<UserRequestsPage> = firstPage

    override fun create(request: CreateTechSupportRequest): Flow<SpecialistResponse<CreateUserRequestProgress>> {
        created = request
        return createSteps
    }

    override suspend fun cancel(request: CloseTechSupportRequest): SpecialistResponse<Unit> {
        closed += "cancel" to request.requestId
        return SpecialistResponse.Success(Unit)
    }

    override suspend fun close(request: CloseTechSupportRequest): SpecialistResponse<Unit> {
        closed += "close" to request.requestId
        return SpecialistResponse.Success(Unit)
    }
}
