package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.runtime.Immutable

@Immutable
internal class PassportReadyActions(
    val onEdit: () -> Unit,
    val onRetry: () -> Unit,
    val onPageVisible: (index: Int, widthPx: Int) -> Unit,
    val onShare: () -> Unit,
    val onSave: () -> Unit,
    val onOpen: () -> Unit,
    val onSaveSettingsFile: () -> Unit,
    val onSaveSettingsToConfigurations: () -> Unit,
    val onExport: () -> Unit = {},
)
