package ru.alarmtrade.specmp.feature.device.settings.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.feature.device.settings.data.files.deleteSettingsScratchFile
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController

internal class DeviceSettingsSessionControllerImpl(
    private val repository: DeviceSettingsRepository,
    private val appScope: CoroutineScope,
    private val dispatchers: AppDispatchers,
) : DeviceSettingsSessionController {

    override fun discardSession(source: SettingsSource) {
        appScope.launch(dispatchers.io) { repository.close(source) }
    }

    override fun discardSnapshot(snapshot: SettingsSource.File) {
        appScope.launch(dispatchers.io) {
            repository.close(snapshot)
            deleteSettingsScratchFile(snapshot.path)
        }
    }
}
