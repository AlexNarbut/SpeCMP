package ru.alarmtrade.specmp.core.specialist

import org.koin.core.module.Module
import org.koin.dsl.module

data class SpecialistAppVersion(val name: String, val code: Int)

fun interface SpecialistAppVersionProvider {
    fun current(): SpecialistAppVersion
}

/**
 * Модули Specialist SDK для composition root: [specialistModule] и версия приложения — её знает только платформенная
 * обёртка.
 */
fun specialistModules(appVersionName: String, appVersionCode: Int): List<Module> {
    val version = SpecialistAppVersion(name = appVersionName, code = appVersionCode)
    return listOf(
        specialistModule,
        module { single<SpecialistAppVersionProvider> { SpecialistAppVersionProvider { version } } },
    )
}
