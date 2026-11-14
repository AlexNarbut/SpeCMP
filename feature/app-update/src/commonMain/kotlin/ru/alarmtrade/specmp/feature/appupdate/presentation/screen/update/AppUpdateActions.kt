package ru.alarmtrade.specmp.feature.appupdate.presentation.screen.update

import androidx.compose.runtime.Immutable

@Immutable
internal data class AppUpdateActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onUpdate: () -> Unit,
    val onSave: () -> Unit,
    val onLater: () -> Unit,
    val onCancel: () -> Unit,
    val onInstall: () -> Unit,
    val onToggleNotes: () -> Unit,
    val onSelectChannel: (Int) -> Unit,
    val onConfirmDialog: () -> Unit,
    val onDismissDialog: () -> Unit,
)
