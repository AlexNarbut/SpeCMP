package ru.alarmtrade.specmp.di

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
actual fun rememberPlatformKoinModules(): List<Module> {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        listOf(module { single<Context> { appContext } })
    }
}
