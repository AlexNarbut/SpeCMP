package ru.alarmtrade.specmp.feature.main.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.main.data.datasource.MainDataSource
import ru.alarmtrade.specmp.feature.main.data.datasource.SpecialistMainDataSource
import ru.alarmtrade.specmp.feature.main.data.repository.SpecialistMainRepository
import ru.alarmtrade.specmp.feature.main.domain.repository.MainRepository
import ru.alarmtrade.specmp.feature.main.presentation.screen.main.MainScreenViewModel

val mainModule = module {
    singleOf(::SpecialistMainDataSource) bind MainDataSource::class
    singleOf(::SpecialistMainRepository) bind MainRepository::class
    viewModelOf(::MainScreenViewModel)
}
