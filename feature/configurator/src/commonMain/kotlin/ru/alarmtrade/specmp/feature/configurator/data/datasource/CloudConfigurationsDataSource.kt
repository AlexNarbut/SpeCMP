package ru.alarmtrade.specmp.feature.configurator.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.configuration.CarConfigurationInfo
import ru.alarmtrade.specialist.interactors.configuration.ConfigurationInteractor
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationInfoListRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface CloudConfigurationsDataSource {
    suspend fun configurationsByCar(
        request: ConfigurationInfoListRequest,
    ): SpecialistResponse<List<CarConfigurationInfo>>?

    suspend fun configuration(request: ConfigurationRequest): SpecialistResponse<CarConfiguration>?
}

internal class SpecialistCloudConfigurationsDataSource(private val interactor: () -> ConfigurationInteractor) :
    CloudConfigurationsDataSource {
    override suspend fun configurationsByCar(
        request: ConfigurationInfoListRequest,
    ): SpecialistResponse<List<CarConfigurationInfo>>? =
        interactor.resolveOrNull()?.getConfigurationListByCarUseCase?.execute(request)

    override suspend fun configuration(request: ConfigurationRequest): SpecialistResponse<CarConfiguration>? =
        interactor.resolveOrNull()?.getConfigurationByIdUseCase?.execute(request)
}
