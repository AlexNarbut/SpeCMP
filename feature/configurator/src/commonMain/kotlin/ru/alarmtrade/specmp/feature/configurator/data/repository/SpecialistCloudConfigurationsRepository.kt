package ru.alarmtrade.specmp.feature.configurator.data.repository

import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationInfoListRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.configurator.data.datasource.CloudConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.configurator.data.mapper.toSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository

/** [CloudConfigurationsRepository] над `configurationInteractor` (через [CloudConfigurationsDataSource]). */
internal class SpecialistCloudConfigurationsRepository(private val dataSource: CloudConfigurationsDataSource) :
    CloudConfigurationsRepository {

    override suspend fun byCar(carId: Int, deviceName: String): Response<List<CloudConfigurationSummary>> =
        dataSource.configurationsByCar(ConfigurationInfoListRequest(carId = carId, alarmDeviceName = deviceName))
            .toResponseOrUnavailable()
            .map { list -> list.map { it.toSummary() } }

    override suspend fun card(id: Int): Response<CloudConfiguration> =
        dataSource.configuration(ConfigurationRequest(id)).toResponseOrUnavailable().map { it.toDomain() }
}
