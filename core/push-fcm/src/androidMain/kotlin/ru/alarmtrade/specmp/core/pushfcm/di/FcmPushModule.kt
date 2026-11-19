package ru.alarmtrade.specmp.core.pushfcm.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.push.PushTokenSource
import ru.alarmtrade.specmp.core.pushfcm.FcmPushTokenSource

/** `Context` (applicationContext) — из платформенных модулей `composeApp`. */
fun fcmPushModule(): Module = module {
    single<PushTokenSource> { FcmPushTokenSource(get<Context>()) } bind AuthCloseable::class
}
