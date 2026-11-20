package ru.alarmtrade.specmp.feature.mountpassport.presentation.model

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaveResult
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.SettingsFileSaver
import ru.alarmtrade.specmp.feature.device.presentation.core.platform.settingsFileName
import ru.alarmtrade.specmp.feature.mountpassport.domain.model.MountPassport
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.MountPassportRepository
import ru.alarmtrade.specmp.feature.mountpassport.domain.repository.PassportConfigurationsRepository
import ru.alarmtrade.specmp.feature.mountpassport.presentation.screen.editor.vehicleTitle

/** Снимок настроек блока из секции «Конфигурация» предотчёта: `.dxs` в файл и в «Мои конфигурации». */
internal sealed interface SettingsExport {
    data class Saved(val name: String) : SettingsExport

    data object Cancelled : SettingsExport

    data class Failed(val error: PassportError?) : SettingsExport
}

internal suspend fun MountPassport.saveSettingsFile(
    repository: MountPassportRepository,
    saver: SettingsFileSaver,
    io: CoroutineDispatcher,
): SettingsExport {
    val path = withContext(io) { repository.settingsFile(id) }
    if (path !is Response.Success) return SettingsExport.Failed(null)
    return when (val result = saver.save(path.value, settingsFileName(settingsModel(), vehicleTitle() ?: number))) {
        is SettingsFileSaveResult.Saved -> SettingsExport.Saved(result.name)
        SettingsFileSaveResult.Failed -> SettingsExport.Failed(null)
        SettingsFileSaveResult.Cancelled -> SettingsExport.Cancelled
    }
}

internal suspend fun MountPassport.saveSettingsToConfigurations(
    repository: MountPassportRepository,
    configurations: PassportConfigurationsRepository,
    io: CoroutineDispatcher,
): SettingsExport {
    val name = vehicleTitle() ?: number
    val response = withContext(io) {
        when (val path = repository.settingsFile(id)) {
            is Response.Success -> configurations.saveSettings(name, settingsModel(), path.value)
            is Response.Error -> path
        }
    }
    return when (response) {
        is Response.Success -> SettingsExport.Saved(name)
        is Response.Error -> SettingsExport.Failed(PassportError.from(response.exception))
    }
}

private fun MountPassport.settingsModel(): String = settingsSnapshot?.deviceModel ?: unit.systemName
