package ru.alarmtrade.specmp.core.push.di

import org.koin.core.module.Module
import org.koin.dsl.module

/** `PushTokenSource` на Android даёт модуль магазина: `core/push-fcm` или `core/push-hms`. */
actual fun pushPlatformModule(): Module = module { }
