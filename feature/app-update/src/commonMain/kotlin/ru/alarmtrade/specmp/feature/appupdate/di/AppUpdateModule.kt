package ru.alarmtrade.specmp.feature.appupdate.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.feature.appupdate.data.datasource.AppUpdateDataSource
import ru.alarmtrade.specmp.feature.appupdate.data.datasource.SpecialistAppUpdateDataSource
import ru.alarmtrade.specmp.feature.appupdate.data.repository.SpecialistAppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.data.service.AppUpdateServiceImpl
import ru.alarmtrade.specmp.feature.appupdate.domain.repository.AppUpdateRepository
import ru.alarmtrade.specmp.feature.appupdate.domain.service.AppUpdateService
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateArgs
import ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update.AppUpdateViewModel

val appUpdateScopeQualifier = named("appUpdateScope")

/** Нужны из composition root: `AppUpdateInstaller` (платформенные модули) и `AppUpdateDeviceActivity`. */
val appUpdateModule = module {
    singleOf(::SpecialistAppUpdateDataSource) bind AppUpdateDataSource::class
    singleOf(::SpecialistAppUpdateRepository) bind AppUpdateRepository::class
    single(appUpdateScopeQualifier) { serviceScope(get<AppDispatchers>().io, logTag = "AppUpdate") }
    single<AppUpdateService> { AppUpdateServiceImpl(get(), get(), get(appUpdateScopeQualifier)) }
    viewModel { params ->
        AppUpdateViewModel(
            args = params.get<AppUpdateArgs>(),
            service = get(),
            installer = get(),
            deviceActivity = get(),
            versionProvider = get(),
        )
    }
}
