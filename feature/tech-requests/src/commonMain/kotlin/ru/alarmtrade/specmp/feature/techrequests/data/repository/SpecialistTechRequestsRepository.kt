package ru.alarmtrade.specmp.feature.techrequests.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.interactors.techSupport.requests.CloseTechSupportRequest
import ru.alarmtrade.specialist.interactors.techSupport.requests.CreateTechSupportRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.phone.PhoneNumbers
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.techrequests.data.datasource.TechRequestsDataSource
import ru.alarmtrade.specmp.feature.techrequests.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.techrequests.data.mapper.toTechRequestException
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequest
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestDraft
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestPage
import ru.alarmtrade.specmp.feature.techrequests.domain.model.TechRequestSubmitStep
import ru.alarmtrade.specmp.feature.techrequests.domain.repository.TechRequestsRepository

internal class SpecialistTechRequestsRepository(private val dataSource: TechRequestsDataSource) :
    TechRequestsRepository {

    override fun updates(): Flow<Response<List<TechRequest>>> =
        dataSource.updates().map { response -> response.toFeatureResponse().map { list -> list.map { it.toDomain() } } }

    override suspend fun loadFirstPage(): Response<TechRequestPage> =
        dataSource.loadFirstPage().toFeatureResponse().map { it.toDomain() }

    override suspend fun loadNextPage(): Response<TechRequestPage> =
        dataSource.loadNextPage().toFeatureResponse().map { it.toDomain() }

    override fun submit(draft: TechRequestDraft): Flow<Response<TechRequestSubmitStep>> {
        val request = CreateTechSupportRequest(
            issue = draft.normalizedIssue,
            userName = draft.normalizedUserName,
            phoneNumber = PhoneNumbers.toE164(draft.phoneDigits),
        )
        return dataSource.create(request).map { response -> response.toFeatureResponse().map { it.toDomain() } }
    }

    override suspend fun cancel(id: Long): Response<Unit> =
        dataSource.cancel(CloseTechSupportRequest(id)).toFeatureResponse()

    override suspend fun close(id: Long): Response<Unit> =
        dataSource.close(CloseTechSupportRequest(id)).toFeatureResponse()

    private fun <T> SpecialistResponse<T>.toFeatureResponse(): Response<T> = when (val response = toResponse()) {
        is Response.Success -> response
        is Response.Error -> Response.Error(response.exception.toTechRequestException(), response.message)
    }
}
