package ru.alarmtrade.specmp.feature.main.presentation.screen.main

import androidx.compose.runtime.Immutable

/** Входы в обновление приложения на главном: меню, блок бокового меню, плашка, уведомление, значок. */
@Immutable
data class MainAppUpdateActions(
    val onOpenFromMenu: () -> Unit,
    val onOpenPrompt: () -> Unit,
    val onInstall: () -> Unit,
    val onDismissBanner: () -> Unit,
    val onToastAction: () -> Unit,
    val onToastDismiss: () -> Unit,
)
