package ru.alarmtrade.specmp.core.specialist.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppVersion
import ru.alarmtrade.specmp.core.common.AppVersionProvider

/**
 * Модули Specialist SDK для composition root: [specialistModule] и версия приложения — её знает только платформенная
 * обёртка.
 */
fun specialistModules(appVersionName: String, appVersionCode: Int): List<Module> {
    val version = AppVersion(name = appVersionName, code = appVersionCode)
    return listOf(
        specialistModule,
        module { single<AppVersionProvider> { AppVersionProvider { version } } },
    )
}
