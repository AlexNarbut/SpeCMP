package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * Платформенные зависимости корня DI, которым нужен платформенный контекст:
 * [ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor] и т. п.
 */
@Composable
expect fun rememberPlatformModule(): Module
