package ru.alarmtrade.specmp.feature.authstate.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.authstate.data.datasource.AuthStateDataSource
import ru.alarmtrade.specmp.feature.authstate.data.datasource.SpecialistAuthStateDataSource
import ru.alarmtrade.specmp.feature.authstate.data.repository.SpecialistAuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.repository.AuthStateRepository
import ru.alarmtrade.specmp.feature.authstate.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.authstate.presentation.logic.AuthStateViewModel

val authStateModule = module {
    singleOf(::SpecialistAuthStateDataSource) bind AuthStateDataSource::class
    singleOf(::SpecialistAuthStateRepository) bind AuthStateRepository::class
    single { LogoutUseCase(get(), getAll()) }
    viewModelOf(::AuthStateViewModel)
}
