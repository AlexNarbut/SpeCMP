package ru.alarmtrade.specmp.flasher.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppServicesStarter
import ru.alarmtrade.specmp.core.ui.startup.AppStartViewModel
import ru.alarmtrade.specmp.feature.authstate.domain.service.AuthSessionController
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.flasher.navigation.FlasherDeviceViewModel

val flasherServicesModule = module {
    single {
        AppServicesStarter(
            listOf(
                "deviceSession" to { get<DeviceSession>().start() },
                "authSession" to { get<AuthSessionController>().start() },
            ),
        )
    }
    viewModelOf(::AppStartViewModel)
    viewModelOf(::FlasherDeviceViewModel)
}
