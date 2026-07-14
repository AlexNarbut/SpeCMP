package ru.alarmtrade.specmp.core.specialist

import org.koin.dsl.module
import ru.alarmtrade.common.core.configuration.CommonConfigurator
import ru.alarmtrade.common.core.configuration.ProvideCommon
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.SpecialistConfig

val specialistModule = module {
    single<SpecialistAppVersionProvider> {
        SpecialistAppVersionProvider { CurrentSpecialistAppVersion }
    }

    single {
        val dispatchers: AppDispatchers = get()
        val appVersionProvider: SpecialistAppVersionProvider = get()

        CommonConfigurator.commonProvider = object : ProvideCommon {
            override fun buildVersion(): Int = appVersionProvider.current().code
        }

        SpecialistApp(
            config = SpecialistConfig(
                appName = "SpeCMP",
                mainDispatcher = dispatchers.main,
                backgroundDispatcher = dispatchers.default,
            )
        )
    }
    single { get<SpecialistApp>().authInteractor }
    single { get<SpecialistApp>().appDeviceInformationInteractor }
    single { get<SpecialistApp>().appUpdatesInteractor }
    single { get<SpecialistApp>().userInteractor }
    single { get<SpecialistApp>().chatInteractor }
    single<SpecialistDeviceInformationProvider> {
        SpecialistDeviceInformationProvider { createSpecialistDeviceInformation() }
    }
}
