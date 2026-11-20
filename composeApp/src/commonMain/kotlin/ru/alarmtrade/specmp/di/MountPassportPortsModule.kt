package ru.alarmtrade.specmp.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportInstallerRepository
import ru.alarmtrade.specmp.mountpassport.AppPassportConfigurationsRepository
import ru.alarmtrade.specmp.mountpassport.AppPassportInstallerRepository

/** Паспорт монтажа не зависит от конфигуратора и аккаунта: «Мои конфигурации» и исполнителя связывает корень. */
val mountPassportPortsModule = module {
    singleOf(::AppPassportConfigurationsRepository) bind PassportConfigurationsRepository::class
    singleOf(::AppPassportInstallerRepository) bind PassportInstallerRepository::class
}
