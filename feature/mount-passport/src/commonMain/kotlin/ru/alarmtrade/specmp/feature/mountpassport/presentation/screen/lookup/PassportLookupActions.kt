package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.lookup

import androidx.compose.runtime.Immutable

@Immutable
internal class PassportLookupActions(
    val onBack: () -> Unit,
    val onIdChange: (String) -> Unit,
    val onFind: () -> Unit,
    val onModelSelect: (String) -> Unit,
    val onOtherModel: () -> Unit,
    val onCustomModelChange: (String) -> Unit,
    val onModelsRetry: () -> Unit,
    val onNext: () -> Unit,
    val scanner: PassportLookupScanActions,
)

@Immutable
internal class PassportLookupScanActions(
    val onScan: () -> Unit,
    val onAccessRetry: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onScanned: (String) -> Unit,
    val onClose: () -> Unit,
    val onManual: () -> Unit,
)
