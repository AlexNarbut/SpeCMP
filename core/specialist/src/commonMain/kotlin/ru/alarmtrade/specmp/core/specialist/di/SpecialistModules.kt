package ru.alarmtrade.specmp.core.specialist.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppVersion
import ru.alarmtrade.specmp.core.common.AppVersionProvider

fun specialistModules(appVersionName: String, appVersionCode: Int): List<Module> {
    val version = AppVersion(name = appVersionName, code = appVersionCode)
    return listOf(
        specialistModule,
        module { single<AppVersionProvider> { AppVersionProvider { version } } },
    )
}
