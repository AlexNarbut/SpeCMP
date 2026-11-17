package ru.alarmtrade.specmp.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppServicesStarter
import ru.alarmtrade.specmp.core.ui.startup.AppStartViewModel
import ru.alarmtrade.specmp.core.ui.theme.CustomThemeStore
import ru.alarmtrade.specmp.core.ui.theme.DefaultCustomThemeStore
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession

val appServicesModule = module {
    single { AppServicesStarter(listOf("deviceSession" to { get<DeviceSession>().start() })) }
    viewModelOf(::AppStartViewModel)
    // Тот же объект, что у AppTheme в App(): «Применить» на экране «Оформление» сразу меняет тему приложения.
    single<CustomThemeStore> { DefaultCustomThemeStore }
}
