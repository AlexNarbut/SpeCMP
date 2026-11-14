package ru.alarmtrade.specmp.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.appupdate.AppMainAppUpdateRepository
import ru.alarmtrade.specmp.core.common.settings.AppSettingsFactory
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateDeviceActivity
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.main.domain.repository.MainAppUpdateRepository

private const val MAIN_APP_UPDATE_SETTINGS = "main_app_update"

/** Обновление не зависит от фич устройства, главный — от обновления: порты связывает корень. */
val appUpdatePortsModule = module {
    single<MainAppUpdateRepository> {
        AppMainAppUpdateRepository(get(), get<AppSettingsFactory>().create(MAIN_APP_UPDATE_SETTINGS))
    }
    single<AppUpdateDeviceActivity> {
        val session = get<DeviceSession>()
        AppUpdateDeviceActivity { session.transport.value != null }
    }
}
