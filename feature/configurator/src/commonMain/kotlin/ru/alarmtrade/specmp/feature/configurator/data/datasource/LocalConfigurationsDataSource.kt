package ru.alarmtrade.specmp.feature.configurator.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.LongWrapper
import ru.alarmtrade.specialist.domain.alarmDevice.settings.LocalSettingsConfigurationInfo
import ru.alarmtrade.specialist.interactors.localConfigurations.LocalSettingsConfigurationInteractor
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.CreateLocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationsPageRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface LocalConfigurationsDataSource {
    suspend fun page(request: LocalConfigurationsPageRequest): SpecialistResponse<List<LocalSettingsConfigurationInfo>>?

    suspend fun create(request: CreateLocalConfigurationRequest): SpecialistResponse<LongWrapper>?

    suspend fun delete(request: LocalConfigurationRequest): SpecialistResponse<Unit>?
}

internal class SpecialistLocalConfigurationsDataSource(
    private val interactor: () -> LocalSettingsConfigurationInteractor,
) : LocalConfigurationsDataSource {
    override suspend fun page(
        request: LocalConfigurationsPageRequest,
    ): SpecialistResponse<List<LocalSettingsConfigurationInfo>>? =
        interactor.resolveOrNull()?.getLocalConfigurationListUseCase?.execute(request)

    override suspend fun create(request: CreateLocalConfigurationRequest): SpecialistResponse<LongWrapper>? =
        interactor.resolveOrNull()?.createLocalConfigurationUseCase?.execute(request)

    override suspend fun delete(request: LocalConfigurationRequest): SpecialistResponse<Unit>? =
        interactor.resolveOrNull()?.deleteLocalConfigurationUseCase?.execute(request)
}
