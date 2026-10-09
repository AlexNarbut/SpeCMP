package ru.alarmtrade.specmp.core.network.di

import org.koin.dsl.module
import ru.alarmtrade.specmp.core.network.HttpClientFactory

val networkModule = module {
    single { HttpClientFactory.create() }
}
