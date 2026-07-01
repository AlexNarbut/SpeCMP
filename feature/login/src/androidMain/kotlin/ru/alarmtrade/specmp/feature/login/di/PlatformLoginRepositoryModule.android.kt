package ru.alarmtrade.specmp.feature.login.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.login.data.repository.SpecialistLoginRepository
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

actual val platformLoginRepositoryModule: Module = module {
    singleOf(::SpecialistLoginRepository) bind LoginRepository::class
}
