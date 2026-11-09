package ru.alarmtrade.specmp.core.lifecycle.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.lifecycle.AppForegroundProvider
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycle
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycleForegroundProvider
import ru.alarmtrade.specmp.core.lifecycle.platformAppLifecycle

/** Koin-модуль `core:lifecycle`, подключается в composition root. */
val appLifecycleModule: Module = module {
    single<AppLifecycle> { platformAppLifecycle() }
    single<AppForegroundProvider> { AppLifecycleForegroundProvider(get()) }
}
