package ru.alarmtrade.specmp.feature.device.settings.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.feature.device.settings.data.datasource.DeviceSettingsDataSource
import ru.alarmtrade.specmp.feature.device.settings.data.datasource.SpecialistDeviceSettingsDataSource
import ru.alarmtrade.specmp.feature.device.settings.data.repository.SpecialistDeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.data.service.DeviceSettingsSessionControllerImpl
import ru.alarmtrade.specmp.feature.device.settings.data.service.DeviceSettingsWritesImpl
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsWrites

val deviceSettingsModule = module {
    single<DeviceSettingsDataSource> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание источника.
        SpecialistDeviceSettingsDataSource(settings = { get() }, alarmDevice = { get() })
    }
    singleOf(::SpecialistDeviceSettingsRepository) bind DeviceSettingsRepository::class
    single<DeviceSettingsSessionController> {
        DeviceSettingsSessionControllerImpl(repository = get(), appScope = get(appScopeQualifier), dispatchers = get())
    }
    single<DeviceSettingsWrites> { DeviceSettingsWritesImpl() }
}
