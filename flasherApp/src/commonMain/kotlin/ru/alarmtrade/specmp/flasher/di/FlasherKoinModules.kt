package ru.alarmtrade.specmp.flasher.di

import org.koin.core.module.Module
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.lifecycle.di.appLifecycleModule
import ru.alarmtrade.specmp.core.localization.di.localizationModule
import ru.alarmtrade.specmp.core.permissions.permissionsModule
import ru.alarmtrade.specmp.core.specialist.di.specialistModules
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.device.firmware.di.deviceFirmwareModule
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceInteractionModule
import ru.alarmtrade.specmp.feature.device.interaction.ui.di.deviceInteractionUiModule
import ru.alarmtrade.specmp.feature.device.selection.di.deviceSelectionModule
import ru.alarmtrade.specmp.feature.login.di.loginModule

/** Модули приложения прошивки без платформенных ([rememberPlatformKoinModules]); порядок — порядок подключения. */
fun flasherKoinModules(appVersionName: String, appVersionCode: Int): List<Module> = buildList {
    add(commonModule)
    addAll(specialistModules(appVersionName, appVersionCode))
    add(localizationModule)
    add(permissionsModule())
    add(appLifecycleModule)
    add(flasherServicesModule)
    add(authStateModule)
    add(loginModule)
    add(deviceSelectionModule)
    add(deviceInteractionModule)
    add(deviceInteractionUiModule)
    add(deviceFirmwareModule)
}
