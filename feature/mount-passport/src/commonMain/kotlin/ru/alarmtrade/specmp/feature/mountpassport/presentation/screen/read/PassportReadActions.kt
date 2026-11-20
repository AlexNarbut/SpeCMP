package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.read

import androidx.compose.runtime.Immutable

@Immutable
internal class PassportReadActions(
    val onCancel: () -> Unit,
    val onRetry: () -> Unit,
    val onContinueWithout: () -> Unit,
    val onSkipSettings: () -> Unit,
)
