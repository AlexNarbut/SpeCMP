package ru.alarmtrade.specmp.feature.configurator.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.configurator.data.datasource.CloudConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.datasource.DeviceModelsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.datasource.LocalConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.datasource.SpecialistCloudConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.datasource.SpecialistDeviceModelsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.datasource.SpecialistLocalConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.repository.SpecialistCloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.data.repository.SpecialistDeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.data.repository.SpecialistLocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository

private const val CONFIGURATOR_PREFERENCES = "configurator"

val configuratorModule = module {
    // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание источника.
    single<LocalConfigurationsDataSource> { SpecialistLocalConfigurationsDataSource(interactor = { get() }) }
    single<CloudConfigurationsDataSource> { SpecialistCloudConfigurationsDataSource(interactor = { get() }) }
    single<DeviceModelsDataSource> { SpecialistDeviceModelsDataSource(interactor = { get() }) }
    singleOf(::SpecialistLocalConfigurationsRepository) bind LocalConfigurationsRepository::class
    singleOf(::SpecialistCloudConfigurationsRepository) bind CloudConfigurationsRepository::class
    single<DeviceModelsRepository> {
        SpecialistDeviceModelsRepository(
            dataSource = get(),
            settings = get<AppSettingsFactory>().create(CONFIGURATOR_PREFERENCES),
        )
    }
}
