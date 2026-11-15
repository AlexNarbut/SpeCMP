package ru.alarmtrade.specmp.feature.push.ui.presentation.screen.rules

import ru.alarmtrade.specmp.feature.push.ui.presentation.model.PushRulesError

internal sealed interface PushRulesEffect {
    data object Saved : PushRulesEffect

    data class SaveFailed(val error: PushRulesError) : PushRulesEffect
}
