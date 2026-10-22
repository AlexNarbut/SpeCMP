package ru.alarmtrade.specmp.feature.device.settings.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.device.settings.data.repository.SpecialistDeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * Koin-модуль логической фичи `feature/device/settings`: сессии расширенных настроек блока и других источников.
 * SDK-интеракторы берутся из `core/specialist`.
 */
val deviceSettingsModule = module {
    single<DeviceSettingsRepository> {
        // Ленивые провайдеры: недоступный на платформе интерактор не должен ронять создание репозитория.
        SpecialistDeviceSettingsRepository(settingsInteractor = { get() }, alarmDeviceInteractor = { get() })
    }
}
