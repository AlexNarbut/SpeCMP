package ru.alarmtrade.specmp.feature.device.settings.domain.service

import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/**
 * Уборка сессий настроек в фоне приложения: переживает экран, который её вызвал, поэтому вызывается и из `onCleared`
 * и `finally` отменённой работы.
 */
interface DeviceSettingsSessionController {
    fun discardSession(source: SettingsSource)

    fun discardSnapshot(snapshot: SettingsSource.File)
}
