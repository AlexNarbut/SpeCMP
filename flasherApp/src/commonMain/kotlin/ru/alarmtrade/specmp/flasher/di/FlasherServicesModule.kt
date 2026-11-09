package ru.alarmtrade.specmp.flasher.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppServicesStarter
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

val flasherServicesModule = module {
    single { AppServicesStarter(listOf("deviceSession" to { get<DeviceSession>().start() })) }
}
