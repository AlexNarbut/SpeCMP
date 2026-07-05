package ru.alarmtrade.specmp.feature.login.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.login.data.repository.SpecialistLoginRepository
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specmp.feature.login.presentation.screen.forgotpassword.ForgotPasswordScreenViewModel
import ru.alarmtrade.specmp.feature.login.presentation.screen.login.LoginScreenViewModel
import ru.alarmtrade.specmp.feature.login.presentation.screen.registration.RegistrationScreenViewModel

val loginModule = module {
    singleOf(::SpecialistLoginRepository) bind LoginRepository::class
    viewModelOf(::LoginScreenViewModel)
    viewModelOf(::RegistrationScreenViewModel)
    viewModelOf(::ForgotPasswordScreenViewModel)
}
