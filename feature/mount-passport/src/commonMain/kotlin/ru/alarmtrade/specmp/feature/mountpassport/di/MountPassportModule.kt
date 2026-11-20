package ru.alarmtrade.specmp.feature.mountpassport.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.MountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.datasource.SpecialistMountPassportDataSource
import ru.alarmtrade.specmp.feature.mountpassport.data.repository.SpecialistMountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository

/** `PassportConfigurationsRepository` («Мои конфигурации») регистрирует корень приложения. */
val mountPassportModule = module {
    singleOf(::SpecialistMountPassportDataSource) bind MountPassportDataSource::class
    singleOf(::SpecialistMountPassportRepository) bind MountPassportRepository::class
}
