package ru.alarmtrade.specmp.feature.login.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.feature.login.data.repository.SpecialistLoginRepository
import ru.alarmtrade.specmp.feature.login.domain.repository.LoginRepository
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.SpecialistConfig

actual val platformLoginModule: Module = module {
    single {
        val dispatchers: AppDispatchers = get()
        SpecialistApp(
            config = SpecialistConfig(
                appName = "SpeCMP",
                mainDispatcher = dispatchers.main,
                backgroundDispatcher = dispatchers.default,
            )
        )
    }
    singleOf(::SpecialistLoginRepository) bind LoginRepository::class
}
