package ru.alarmtrade.specmp.flasher.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.module.Module
import org.koin.dsl.module
import ru.alarmtrade.specmp.core.common.AppRestarter

// Монитора сети на desktop нет: SDK использует свой дефолт. Перезапуск JVM-приложения изнутри не делаем —
// пользователь перезапускает сам.
@Composable
actual fun rememberPlatformKoinModules(): List<Module> = remember {
    listOf(module { single<AppRestarter> { AppRestarter { false } } })
}
