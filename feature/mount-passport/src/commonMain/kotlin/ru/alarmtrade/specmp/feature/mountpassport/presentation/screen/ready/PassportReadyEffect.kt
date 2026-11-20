package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportReadyEffect {
    data class Saved(val name: String) : PassportReadyEffect

    data object SaveFailed : PassportReadyEffect

    data object ShareFailed : PassportReadyEffect

    /** Пересборка после правки не удалась — на экране прежний документ. */
    data class RefreshFailed(val error: PassportError) : PassportReadyEffect
}
