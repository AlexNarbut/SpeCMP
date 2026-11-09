package ru.alarmtrade.specmp.feature.account.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.alarmtrade.specmp.feature.account.data.datasource.AccountDataSource
import ru.alarmtrade.specmp.feature.account.data.datasource.SpecialistAccountDataSource
import ru.alarmtrade.specmp.feature.account.data.repository.SpecialistAccountRepository
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository

/** Koin-модуль фичи `feature/account`: профиль текущего аккаунта. SDK-интерактор — из `core/specialist`. */
val accountModule = module {
    singleOf(::SpecialistAccountDataSource) { bind<AccountDataSource>() }
    singleOf(::SpecialistAccountRepository) { bind<AccountRepository>() }
}
