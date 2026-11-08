package ru.alarmtrade.specmp.feature.configurator.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.configurator.data.repository.SpecialistCloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.data.repository.SpecialistDeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.data.repository.SpecialistLocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository

private const val CONFIGURATOR_PREFERENCES = "configurator"

/**
 * Koin-модуль логической фичи `feature/configurator`: «Мои конфигурации», облачные конфигурации, модели блоков.
 * SDK-интеракторы — из `core/specialist`, сессии настроек — из `feature/device/settings`.
 */
val configuratorModule = module {
    // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание репозитория.
    single<LocalConfigurationsRepository> {
        SpecialistLocalConfigurationsRepository(interactor = { get() }, settings = get())
    }
    single<CloudConfigurationsRepository> { SpecialistCloudConfigurationsRepository(interactor = { get() }) }
    single<DeviceModelsRepository> {
        SpecialistDeviceModelsRepository(
            interactor = { get() },
            settings = get<AppSettingsFactory>().create(CONFIGURATOR_PREFERENCES),
        )
    }
}
