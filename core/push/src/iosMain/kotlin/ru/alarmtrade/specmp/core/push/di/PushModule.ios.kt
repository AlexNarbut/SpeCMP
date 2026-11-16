package ru.alarmtrade.specmp.core.push.di

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.push.IosPushTokenSource
import ru.alarmtrade.specmp.core.push.PushTokenSource

actual fun pushPlatformModule(): Module = module {
    single<PushTokenSource> { IosPushTokenSource() } bind AuthCloseable::class
}
