package ru.alarmtrade.specmp.flasher.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * Платформенные Koin-модули приложения прошивки (как у `composeApp`): Android — `Context`, `NetworkMonitor`,
 * `AppRestarter`; desktop — `AppRestarter`.
 */
@Composable
expect fun rememberPlatformKoinModules(): List<Module>
