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
)
