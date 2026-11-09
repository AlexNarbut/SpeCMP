package ru.alarmtrade.specmp.feature.device.settings.domain.service

import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/**
 * Уборка сессий настроек в фоне приложения: переживает экран, который её вызвал, поэтому вызывается и из `onCleared`
 * и `finally` отменённой работы.
 */
interface DeviceSettingsSessionController {
    /** Закрыть сессию [source] без сохранения правок. */
    fun discardSession(source: SettingsSource)

    /** Закрыть сессию снимка и удалить его файл. */
    fun discardSnapshot(snapshot: SettingsSource.File)
}
