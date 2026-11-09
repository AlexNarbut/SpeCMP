package ru.alarmtrade.specmp.core.localization.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.localization.DefaultAppLocaleStore

val localizationModule = module {
    single<AppLocaleStore> { DefaultAppLocaleStore }
}
