package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import androidx.compose.runtime.Immutable

@Immutable
internal class PassportReadyActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onPageVisible: (index: Int, widthPx: Int) -> Unit,
    val onExportPdf: () -> Unit = {},
    val onExportZip: () -> Unit = {},
    val onIssueRetry: () -> Unit = {},
    val onIssueDismiss: () -> Unit = {},
)
