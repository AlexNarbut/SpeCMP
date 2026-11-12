package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.koin.core.module.Module

// Монитора сети на desktop нет: SDK использует свой дефолт.
@Composable
actual fun rememberPlatformKoinModules(): List<Module> = remember {
    emptyList()
}
