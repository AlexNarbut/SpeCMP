package ru.alarmtrade.specmp.di

import org.koin.core.module.Module
import ru.alarmtrade.specmp.core.common.di.commonModule
import ru.alarmtrade.specmp.core.lifecycle.di.appLifecycleModule
import ru.alarmtrade.specmp.core.localization.di.localizationModule
import ru.alarmtrade.specmp.core.permissions.permissionsModule
import ru.alarmtrade.specmp.core.push.di.pushPlatformModule
import ru.alarmtrade.specmp.core.specialist.di.specialistModules
import ru.alarmtrade.specmp.core.speech.di.speechModule
import ru.alarmtrade.specmp.feature.account.di.accountModule
import ru.alarmtrade.specmp.feature.advancedmount.di.advancedMountModule
import ru.alarmtrade.specmp.feature.appearance.di.appearanceModule
import ru.alarmtrade.specmp.feature.appsettings.di.appSettingsModule
import ru.alarmtrade.specmp.feature.appupdate.di.appUpdateModule
import ru.alarmtrade.specmp.feature.authstate.di.authStateModule
import ru.alarmtrade.specmp.feature.configurator.di.configuratorModule
import ru.alarmtrade.specmp.feature.configurator.ui.di.configuratorUiModule
import ru.alarmtrade.specmp.feature.device.autostart.di.deviceAutostartModule
import ru.alarmtrade.specmp.feature.device.firmware.di.deviceFirmwareModule
import ru.alarmtrade.specmp.feature.device.interaction.di.deviceInteractionModule
import ru.alarmtrade.specmp.feature.device.interaction.ui.di.deviceInteractionUiModule
import ru.alarmtrade.specmp.feature.device.peripherals.di.devicePeripheralsModule
import ru.alarmtrade.specmp.feature.device.presentation.core.di.handsFreeVoiceModule
import ru.alarmtrade.specmp.feature.device.selection.di.deviceSelectionModule
import ru.alarmtrade.specmp.feature.device.settings.di.deviceSettingsModule
import ru.alarmtrade.specmp.feature.device.settings.ui.di.deviceSettingsUiModule
import ru.alarmtrade.specmp.feature.device.signallog.di.signalLogModule
import ru.alarmtrade.specmp.feature.device.state.di.deviceStateModule
import ru.alarmtrade.specmp.feature.fastmount.di.fastMountModule
import ru.alarmtrade.specmp.feature.login.di.loginModule
import ru.alarmtrade.specmp.feature.main.di.mainModule
import ru.alarmtrade.specmp.feature.mountassistant.di.mountAssistantModule
import ru.alarmtrade.specmp.feature.mountpassport.di.mountPassportModule
import ru.alarmtrade.specmp.feature.productinfo.di.productInfoModule
import ru.alarmtrade.specmp.feature.push.di.pushModule
import ru.alarmtrade.specmp.feature.push.ui.di.pushUiModule
import ru.alarmtrade.specmp.feature.supportchat.di.supportChatModule
import ru.alarmtrade.specmp.feature.techrequests.di.techRequestsModule
import ru.alarmtrade.specmp.feature.vehicle.di.vehicleModule
import ru.alarmtrade.specmp.feature.vehicle.ui.di.vehicleUiModule

/** Без платформенных ([rememberPlatformKoinModules]); порядок — порядок подключения. */
fun appKoinModules(appVersionName: String, appVersionCode: Int): List<Module> = buildList {
    add(commonModule)
    addAll(specialistModules(appVersionName, appVersionCode))
    add(localizationModule)
    add(permissionsModule())
    add(pushPlatformModule())
    add(appLifecycleModule)
    add(speechModule)
    add(handsFreeVoiceModule)
    add(appServicesModule)
    add(authStateModule)
    add(loginModule)
    add(mainModule)
    add(supportChatModule)
    add(techRequestsModule)
    add(productInfoModule)
    add(appUpdateModule)
    add(deviceSelectionModule)
    add(deviceInteractionModule)
    add(deviceFirmwareModule)
    add(deviceAutostartModule)
    add(deviceStateModule)
    add(signalLogModule)
    add(devicePeripheralsModule)
    add(deviceSettingsModule)
    add(vehicleModule)
    add(accountModule)
    add(pushModule)
    add(pushUiModule)
    add(vehicleUiModule)
    add(configuratorModule)
    add(configuratorUiModule)
    add(deviceSettingsUiModule)
    add(deviceInteractionUiModule)
    add(advancedMountModule)
    add(fastMountModule)
    add(mountAssistantModule)
    add(mountPassportModule)
    add(mountPassportPortsModule)
    add(assistantPortsModule)
    add(appUpdatePortsModule)
    add(appSettingsModule)
    add(appearanceModule)
}
