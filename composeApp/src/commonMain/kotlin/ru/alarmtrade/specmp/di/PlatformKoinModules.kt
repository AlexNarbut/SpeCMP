package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * Платформенные Koin-модули composition root: зависимости, которым нужен платформенный
 * контекст. Android — `android.content.Context` (напр., для PermissionsProvider) и
 * [ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor]; iOS — `NetworkMonitor`;
 * desktop — пусто.
 */
@Composable
expect fun rememberPlatformKoinModules(): List<Module>
