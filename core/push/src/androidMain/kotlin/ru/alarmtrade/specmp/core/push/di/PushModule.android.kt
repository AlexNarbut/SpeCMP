package ru.alarmtrade.specmp.core.push.di

import android.content.Context
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.push.FirebasePushTokenSource
import ru.alarmtrade.specmp.core.push.PushTokenSource

/** `Context` (applicationContext) — из платформенных модулей `composeApp`. */
actual fun pushPlatformModule(): Module = module {
    single<PushTokenSource> { FirebasePushTokenSource(get<Context>()) } bind AuthCloseable::class
}
