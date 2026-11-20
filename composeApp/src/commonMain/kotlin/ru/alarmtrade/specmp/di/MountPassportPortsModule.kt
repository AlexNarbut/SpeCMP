package ru.alarmtrade.specmp.di

import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository
import ru.alarmtrade.specmp.mountpassport.AppPassportConfigurationsRepository

/** Паспорт монтажа не зависит от конфигуратора: «Мои конфигурации» связывает корень. */
val mountPassportPortsModule = module {
    singleOf(::AppPassportConfigurationsRepository) bind PassportConfigurationsRepository::class
}
