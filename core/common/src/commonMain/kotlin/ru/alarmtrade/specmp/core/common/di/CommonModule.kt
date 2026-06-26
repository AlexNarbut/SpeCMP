package ru.alarmtrade.specmp.core.common.di

import ru.alarmtrade.specmp.core.common.AppDispatchers
import org.koin.dsl.module

val commonModule = module {
    single { AppDispatchers() }
}
