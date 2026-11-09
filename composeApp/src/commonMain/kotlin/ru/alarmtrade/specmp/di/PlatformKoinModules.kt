package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

@Composable
expect fun rememberPlatformKoinModules(): List<Module>
