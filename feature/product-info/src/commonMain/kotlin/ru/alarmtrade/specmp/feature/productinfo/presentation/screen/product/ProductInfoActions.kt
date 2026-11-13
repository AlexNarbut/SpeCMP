package ru.alarmtrade.specmp.feature.productinfo.presentation.screen.product

import androidx.compose.runtime.Immutable

@Immutable
internal data class ProductInfoActions(
    val onBack: () -> Unit,
    val onQueryChange: (String) -> Unit,
    val onClear: () -> Unit,
    val onSearch: () -> Unit,
    val onRetry: () -> Unit,
    val onToggleComponent: (Int) -> Unit,
    val onToggleAll: () -> Unit,
    val onCopy: (String) -> Unit,
    val onScan: () -> Unit,
    val onScanAccessRetry: () -> Unit,
    val onScanOpenSettings: () -> Unit,
    val onScanned: (String) -> Unit,
    val onScanClose: () -> Unit,
    val onScanManual: () -> Unit,
)
