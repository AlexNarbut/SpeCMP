package ru.alarmtrade.specmp.core.pushhms.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.push.PushTokenSource
import ru.alarmtrade.specmp.core.pushhms.HmsPushTokenSource

/** `Context` (applicationContext) — из платформенных модулей `composeApp`. */
fun hmsPushModule(): Module = module {
    single<PushTokenSource> { HmsPushTokenSource(get<Context>(), get()) } bind AuthCloseable::class
}
