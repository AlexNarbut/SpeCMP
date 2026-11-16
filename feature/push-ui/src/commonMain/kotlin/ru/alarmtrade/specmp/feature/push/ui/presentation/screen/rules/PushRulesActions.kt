package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import ru.alarmtrade.specmp.core.push.PushType

internal class PushRulesActions(
    val onBack: () -> Unit,
    val onToggle: (PushType, Boolean) -> Unit,
    val onSave: () -> Unit,
    val onRetry: () -> Unit,
)
