package ru.alarmtrade.specmp.feature.device.settings.ui.presentation.screen.editor

import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

internal sealed interface DeviceSettingsEffect {
    data class Written(val count: Int) : DeviceSettingsEffect
    data object SetValueFailed : DeviceSettingsEffect
    data object ResetDone : DeviceSettingsEffect
    data class Exit(val target: ExitTarget) : DeviceSettingsEffect

    data class Loaded(val count: Int) : DeviceSettingsEffect

    data object PickFile : DeviceSettingsEffect

    data class ExportFile(val path: String, val fileName: String) : DeviceSettingsEffect

    data class FileSaved(val name: String) : DeviceSettingsEffect

    data class SavedToMine(val configuration: SettingsSource.LocalConfiguration) : DeviceSettingsEffect

    data object Saved : DeviceSettingsEffect

    data class WriteToDevice(val source: SettingsSource) : DeviceSettingsEffect
}
