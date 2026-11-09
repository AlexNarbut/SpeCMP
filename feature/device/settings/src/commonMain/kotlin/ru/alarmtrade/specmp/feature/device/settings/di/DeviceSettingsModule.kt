package ru.alarmtrade.specmp.feature.device.settings.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.di.appScopeQualifier
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.device.settings.data.datasource.DeviceSettingsDataSource
import ru.alarmtrade.specmp.feature.device.settings.data.datasource.SpecialistDeviceSettingsDataSource
import ru.alarmtrade.specmp.feature.device.settings.data.preferences.SettingsDeviceSettingsPreferences
import ru.alarmtrade.specmp.feature.device.settings.data.repository.SpecialistDeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.data.service.DeviceSettingsSessionControllerImpl
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsPreferences
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController

private const val DEVICE_SETTINGS_PREFERENCES = "devicesettings"

val deviceSettingsModule = module {
    single<DeviceSettingsDataSource> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание источника.
        SpecialistDeviceSettingsDataSource(settings = { get() }, alarmDevice = { get() })
    }
    singleOf(::SpecialistDeviceSettingsRepository) bind DeviceSettingsRepository::class
    single<DeviceSettingsSessionController> {
        DeviceSettingsSessionControllerImpl(repository = get(), appScope = get(appScopeQualifier), dispatchers = get())
    }
    // Не привязано к аккаунту (ширина колонки дерева) — переживает логаут.
    single<DeviceSettingsPreferences> {
        SettingsDeviceSettingsPreferences(get<AppSettingsFactory>().create(DEVICE_SETTINGS_PREFERENCES))
    }
}
