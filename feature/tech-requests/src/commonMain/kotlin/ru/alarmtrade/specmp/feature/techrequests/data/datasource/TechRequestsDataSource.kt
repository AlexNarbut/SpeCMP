package ru.alarmtrade.specmp.feature.techrequests.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.userRequests.CreateUserRequestProgress
import ru.alarmtrade.specialist.domain.userRequests.UserRequest
import ru.alarmtrade.specialist.domain.userRequests.UserRequestsPage
import ru.alarmtrade.specialist.interactors.techSupport.TechSupportInteractor
import ru.alarmtrade.specialist.interactors.techSupport.requests.CloseTechSupportRequest
import ru.alarmtrade.specialist.interactors.techSupport.requests.CreateTechSupportRequest

internal interface TechRequestsDataSource {
    fun updates(): Flow<SpecialistResponse<List<UserRequest>>>
    suspend fun loadFirstPage(): SpecialistResponse<UserRequestsPage>
    suspend fun loadNextPage(): SpecialistResponse<UserRequestsPage>
    fun create(request: CreateTechSupportRequest): Flow<SpecialistResponse<CreateUserRequestProgress>>
    suspend fun cancel(request: CloseTechSupportRequest): SpecialistResponse<Unit>
    suspend fun close(request: CloseTechSupportRequest): SpecialistResponse<Unit>
}

internal class SpecialistTechRequestsDataSource(private val interactor: TechSupportInteractor) :
    TechRequestsDataSource {
    override fun updates(): Flow<SpecialistResponse<List<UserRequest>>> = flow {
        emitAll(interactor.getUserRequestsFlowableUseCase.execute())
    }

    override suspend fun loadFirstPage(): SpecialistResponse<UserRequestsPage> =
        interactor.loadFirstRequestsPageUseCase.execute()

    override suspend fun loadNextPage(): SpecialistResponse<UserRequestsPage> =
        interactor.loadNextRequestsPageUseCase.execute()

    override fun create(request: CreateTechSupportRequest): Flow<SpecialistResponse<CreateUserRequestProgress>> = flow {
        emitAll(interactor.createRequestFlowUseCase.execute(request))
    }

    override suspend fun cancel(request: CloseTechSupportRequest): SpecialistResponse<Unit> =
        interactor.cancelUserRequestUseCase.execute(request)

    override suspend fun close(request: CloseTechSupportRequest): SpecialistResponse<Unit> =
        interactor.closeUserRequestUseCase.execute(request)
}
