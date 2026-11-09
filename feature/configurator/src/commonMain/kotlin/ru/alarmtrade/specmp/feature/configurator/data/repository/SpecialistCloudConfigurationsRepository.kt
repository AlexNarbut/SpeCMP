package ru.alarmtrade.specmp.feature.configurator.data.repository

import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.configuration.CarConfigurationInfo
import ru.alarmtrade.specialist.interactors.configuration.ConfigurationInteractor
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationInfoListRequest
import ru.alarmtrade.specialist.interactors.configuration.requests.ConfigurationRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationCar
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

internal fun CarConfigurationInfo.toSummary() = CloudConfigurationSummary(
    id = id,
    name = name,
    shortDescription = shortDescription,
    deviceName = alarmDeviceName,
    deviceTitle = alarmDeviceTitle.ifBlank { alarmDeviceName },
    car = ConfigurationCar(id = car.id, brand = car.brand, model = car.model, trim = car.name),
    author = author.fullName.ifBlank { author.login },
    // Оценок нет — SDK отдаёт 0.
    rating = rating.takeIf { it > 0 },
    updated = lastUpdateDate,
    onlyForAuthor = !isAvailable,
)

internal fun CarConfiguration.toDomain() = CloudConfiguration(
    summary = info.toSummary(),
    fullDescriptionHtml = info.fullDescription,
    mountingManual = info.mountingManualFile.takeIf { it.url.isNotBlank() }?.let {
        ConfigurationAttachment(title = it.name, previewUrl = null, url = it.url)
    },
    attachments = images.filter { it.url.isNotBlank() }.map { image ->
        ConfigurationAttachment(
            title = image.shortDescription,
            previewUrl = image.previewUrl.takeIf { it.isNotBlank() },
            url = image.url,
        )
    },
)
