package ru.alarmtrade.specmp.feature.main.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenViewModel

val mainModule = module {
    viewModelOf(::MainScreenViewModel)
}
