package ru.alarmtrade.specmp.feature.login.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.login.domain.usecase.CheckLoginStatusUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.LoginUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.LogoutUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.ResendActivationEmailUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.ResetPasswordUseCase
import ru.alarmtrade.specmp.feature.login.domain.usecase.SignUpUseCase
import ru.alarmtrade.specmp.feature.login.presentation.screen.authroot.AuthRootViewModel
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreenViewModel
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreenViewModel

expect val platformLoginRepositoryModule: Module

val loginModule = module {
    factoryOf(::LoginUseCase)
    factoryOf(::SignUpUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::ResendActivationEmailUseCase)
    factoryOf(::CheckLoginStatusUseCase)
    factoryOf(::LogoutUseCase)
    viewModelOf(::AuthRootViewModel)
    viewModelOf(::RegistrationScreenViewModel)
    viewModelOf(::ForgotPasswordScreenViewModel)
}
