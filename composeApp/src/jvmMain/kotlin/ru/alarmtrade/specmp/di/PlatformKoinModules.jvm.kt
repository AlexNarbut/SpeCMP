package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

// Монитора сети на desktop нет: SDK использует свой дефолт.
@Composable
actual fun rememberPlatformKoinModules(): List<Module> = emptyList()
