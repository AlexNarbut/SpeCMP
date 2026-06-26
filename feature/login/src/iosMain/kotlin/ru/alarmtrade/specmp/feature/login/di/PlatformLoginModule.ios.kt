package ru.alarmtrade.specmp.feature.login.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.login.data.repository.UnavailableLoginRepository
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository

actual val platformLoginModule: Module = module {
    singleOf(::UnavailableLoginRepository) bind LoginRepository::class
}
