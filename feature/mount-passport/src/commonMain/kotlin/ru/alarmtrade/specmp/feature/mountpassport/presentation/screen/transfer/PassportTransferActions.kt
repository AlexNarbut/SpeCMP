package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.transfer

import androidx.compose.runtime.Immutable

@Immutable
internal class PassportTransferActions(
    val onClose: () -> Unit,
    val onIncludeSettingsChange: (Boolean) -> Unit,
    val onIncludePhotosChange: (Boolean) -> Unit,
    val onShare: () -> Unit,
    val onSave: () -> Unit,
    val onRetry: () -> Unit,
    val onPickFile: () -> Unit,
    val onPickAnother: () -> Unit,
    val onReplaceChange: (Boolean) -> Unit,
    val onImportConfirm: () -> Unit,
)
