package ru.alarmtrade.specmp.core.push.di

import org.koin.core.module.Module

/** [ru.alarmtrade.specmp.core.push.PushTokenSource] платформы, привязан и как `AuthCloseable`. */
expect fun pushPlatformModule(): Module
