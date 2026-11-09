package ru.alarmtrade.specmp.feature.advancedmount.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.advancedmount.data.datasource.AdvancedMountDataSource
import ru.alarmtrade.specmp.feature.advancedmount.data.datasource.SpecialistAdvancedMountDataSource
import ru.alarmtrade.specmp.feature.advancedmount.data.repository.SpecialistAdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root.AdvancedMountRootViewModel

val advancedMountModule = module {
    singleOf(::SpecialistAdvancedMountDataSource) bind AdvancedMountDataSource::class
    singleOf(::SpecialistAdvancedMountRepository) bind AdvancedMountRepository::class
    viewModelOf(::AdvancedMountRootViewModel)
}
