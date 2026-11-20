package ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor

import ru.alarmtrade.specmp.feature.mountpassport.presentation.model.PassportError

internal sealed interface PassportEditorEffect {
    /** Несохранённое записано — можно уходить. */
    data object Close : PassportEditorEffect

    data object OpenDocument : PassportEditorEffect

    data class SettingsFileSaved(val name: String) : PassportEditorEffect

    data object SettingsFileFailed : PassportEditorEffect

    data object ConfigurationSaved : PassportEditorEffect

    data class ConfigurationFailed(val error: PassportError) : PassportEditorEffect

    data class PhotoFailed(val error: PassportError) : PassportEditorEffect

    data object Reread : PassportEditorEffect

    data class RereadFailed(val error: RereadError) : PassportEditorEffect
}

/** «Перечитать» не удалось: подключён другой блок, этап не прочитан или прочая ошибка. */
internal sealed interface RereadError {
    data class WrongUnit(val actualSystemId: String) : RereadError

    data class NotRead(val connectionLost: Boolean) : RereadError

    data class Common(val error: PassportError) : RereadError
}
