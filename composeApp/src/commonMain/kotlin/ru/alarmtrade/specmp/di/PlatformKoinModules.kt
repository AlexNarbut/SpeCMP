package ru.alarmtrade.specmp.di

import androidx.compose.runtime.Composable
import org.koin.core.module.Module

/**
 * Платформенные Koin-модули composition root. На Android регистрирует
 * `android.content.Context` (нужен, напр., для PermissionsProvider); на других — пусто.
 */
@Composable
expect fun rememberPlatformKoinModules(): List<Module>
