package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportEditorEffect {
    /** Несохранённое записано — можно уходить. */
    data object Close : PassportEditorEffect

    data class SettingsFileSaved(val name: String) : PassportEditorEffect

    data object SettingsFileFailed : PassportEditorEffect

    data object ConfigurationSaved : PassportEditorEffect

    data class ConfigurationFailed(val error: PassportError) : PassportEditorEffect

    data class PhotoFailed(val error: PassportError) : PassportEditorEffect
}
