package ru.alarmtrade.specmp.feature.techrequests.presentation.screen.requests

import ru.alarmtrade.specmp.feature.techrequests.presentation.model.TechRequestsError

internal sealed interface TechRequestsEffect {
    data class ActionDone(val action: RequestAction) : TechRequestsEffect

    data class ActionFailed(val action: RequestAction, val error: TechRequestsError) : TechRequestsEffect

    /** Отправку остановили: заявка могла уже уйти — список перечитан. */
    data object SendingStopped : TechRequestsEffect
}
