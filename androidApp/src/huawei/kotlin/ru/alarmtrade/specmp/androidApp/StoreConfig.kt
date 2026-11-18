package ru.alarmtrade.specmp.androidApp

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppStore
import ru.alarmtrade.specmp.core.pushhms.di.hmsPushModule

internal fun storeModules(): List<Module> = listOf(
    module { single { AppStore.APP_GALLERY } },
    hmsPushModule(),
)
