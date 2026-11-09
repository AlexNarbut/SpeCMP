package ru.alarmtrade.specmp.feature.configurator.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.alarmDevice.settings.LocalSettingsConfigurationInfo
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.CreateLocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationsPageRequest
import ru.alarmtrade.specmp.feature.configurator.data.datasource.LocalConfigurationsDataSource

internal class FakeLocalConfigurationsDataSource : LocalConfigurationsDataSource {
    var available = true
    var pages: List<SpecialistResponse<List<LocalSettingsConfigurationInfo>>> = emptyList()
    var createdId = 100L
    var createResult: SpecialistResponse<LongWrapper>? = null
    val deleteResults = mutableMapOf<Long, SpecialistResponse<Unit>>()

    val pageRequests = mutableListOf<LocalConfigurationsPageRequest>()
    val created = mutableListOf<CreateLocalConfigurationRequest>()
    val deleted = mutableListOf<Long>()

    override suspend fun page(
        request: LocalConfigurationsPageRequest,
    ): SpecialistResponse<List<LocalSettingsConfigurationInfo>>? = ifAvailable {
        pageRequests += request
        pages.getOrElse(request.page) { SpecialistResponse.Success(emptyList()) }
    }

    override suspend fun create(request: CreateLocalConfigurationRequest): SpecialistResponse<LongWrapper>? =
        ifAvailable {
            created += request
            createResult ?: SpecialistResponse.Success(LongWrapper(createdId++))
        }

    override suspend fun delete(request: LocalConfigurationRequest): SpecialistResponse<Unit>? = ifAvailable {
        deleted += request.configurationId
        deleteResults[request.configurationId] ?: SpecialistResponse.Success(Unit)
    }

    private inline fun <T> ifAvailable(block: () -> T): T? = if (available) block() else null
}
