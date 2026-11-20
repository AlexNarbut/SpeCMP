package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.ready

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportReadyEffect {
    /** «Изменить» — назад в предотчёт. */
    data object Edit : PassportReadyEffect

    data class Saved(val name: String) : PassportReadyEffect

    data object SaveFailed : PassportReadyEffect

    data object ShareFailed : PassportReadyEffect

    data object NoPdfApp : PassportReadyEffect

    data object OpenFailed : PassportReadyEffect

    /** Пересборка после правки не удалась — на экране прежний документ. */
    data class RefreshFailed(val error: PassportError) : PassportReadyEffect

    data class SettingsFileSaved(val name: String) : PassportReadyEffect

    data object SettingsFileFailed : PassportReadyEffect

    data object ConfigurationSaved : PassportReadyEffect

    data class ConfigurationFailed(val error: PassportError) : PassportReadyEffect
}
