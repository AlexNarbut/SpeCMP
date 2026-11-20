package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.list

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportListEffect {
    data object Deleted : PassportListEffect

    data class DeleteFailed(val error: PassportError) : PassportListEffect
}
