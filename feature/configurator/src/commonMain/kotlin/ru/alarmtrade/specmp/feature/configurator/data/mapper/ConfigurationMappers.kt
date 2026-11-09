package ru.alarmtrade.specmp.feature.configurator.data.mapper

import ru.alarmtrade.specialist.domain.alarmDevice.settings.LocalSettingsConfigurationInfo
import ru.alarmtrade.specialist.domain.configuration.CarConfiguration
import ru.alarmtrade.specialist.domain.configuration.CarConfigurationInfo
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationAttachment
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationCar
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration

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

internal fun LocalSettingsConfigurationInfo.toDomain() = LocalConfiguration(
    id = id,
    name = name,
    description = description,
    deviceName = deviceName,
    created = createdDate,
    updated = updatedDate,
)
