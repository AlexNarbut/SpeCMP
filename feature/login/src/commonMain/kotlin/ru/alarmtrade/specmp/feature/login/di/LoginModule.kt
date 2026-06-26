package ru.alarmtrade.specmp.feature.login.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.login.domain.usecase.CheckLoginStatusUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.LoginUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.ResendActivationEmailUseCase
import ru.alarmtrade.specmp.feature.login.presentation.LoginViewModel

expect val platformLoginModule: Module

val loginModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::ResendActivationEmailUseCase)
    factoryOf(::CheckLoginStatusUseCase)
    viewModelOf(::LoginViewModel)
}
