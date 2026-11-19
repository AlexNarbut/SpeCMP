package ru.alarmtrade.specmp.androidApp

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppStore
import ru.alarmtrade.specmp.core.pushfcm.di.fcmPushModule

internal fun storeModules(): List<Module> = listOf(
    module { single { AppStore.GOOGLE_PLAY } },
    fcmPushModule(),
)
