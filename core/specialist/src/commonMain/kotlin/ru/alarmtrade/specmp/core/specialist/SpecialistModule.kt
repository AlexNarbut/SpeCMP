package ru.alarmtrade.specmp.core.specialist

import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specialist.SpecialistApp
import ru.alarmtrade.specialist.SpecialistConfig

val specialistModule = module {
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
    single { get<SpecialistApp>().authInteractor }
    single { get<SpecialistApp>().appDeviceInformationInteractor }
    single<SpecialistDeviceInformationProvider> {
        SpecialistDeviceInformationProvider { createSpecialistDeviceInformation() }
    }
}
