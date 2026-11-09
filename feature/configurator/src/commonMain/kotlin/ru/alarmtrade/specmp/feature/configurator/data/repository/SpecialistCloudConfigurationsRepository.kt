package ru.alarmtrade.specmp.feature.configurator.data.repository

import ru.alarmtrade.specialist.interactors.configuration.ConfigurationInteractor
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationInfoListRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.configurator.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.configurator.data.mapper.toSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository

/** [CloudConfigurationsRepository] над `configurationInteractor`. Интерактор — лениво. */
internal class SpecialistCloudConfigurationsRepository(private val interactor: () -> ConfigurationInteractor) :
    CloudConfigurationsRepository {

    override suspend fun byCar(carId: Int, deviceName: String): Response<List<CloudConfigurationSummary>> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        return sdk.getConfigurationListByCarUseCase
            .execute(ConfigurationInfoListRequest(carId = carId, alarmDeviceName = deviceName))
            .toResponse()
            .map { list -> list.map { it.toSummary() } }
    }

    override suspend fun card(id: Int): Response<CloudConfiguration> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        return sdk.getConfigurationByIdUseCase.execute(ConfigurationRequest(id)).toResponse().map { it.toDomain() }
    }
}
