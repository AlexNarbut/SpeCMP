package ru.alarmtrade.specmp.feature.device.settings.data.service

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.feature.device.settings.data.files.deleteSettingsScratchFile
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController

internal class DeviceSettingsSessionControllerImpl(
    private val repository: DeviceSettingsRepository,
    private val appScope: CoroutineScope,
    private val dispatchers: AppDispatchers,
) : DeviceSettingsSessionController {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override fun discardSession(source: SettingsSource) {
        appScope.launch(dispatchers.io) {
            repository.close(source).onError { logger.w(it) { "Failed to discard the settings session" } }
        }
    }

    override fun discardSnapshot(snapshot: SettingsSource.File) {
        appScope.launch(dispatchers.io) {
            repository.close(snapshot).onError { logger.w(it) { "Failed to close the snapshot session" } }
            deleteSettingsScratchFile(snapshot.path)
        }
    }
}
