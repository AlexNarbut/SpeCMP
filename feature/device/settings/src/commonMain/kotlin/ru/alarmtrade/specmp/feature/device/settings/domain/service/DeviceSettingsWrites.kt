package ru.alarmtrade.specmp.feature.device.settings.domain.service

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/** [settingNames] — названия настроек, изменённых этой записью. */
data class SettingsWrite(val source: SettingsSource, val settingNames: List<String>)

/** Успешные записи настроек: кто записал — сообщает, кому важен итог (ассистент монтажа) — слушает. */
interface DeviceSettingsWrites {
    fun writes(): Flow<Response<SettingsWrite>>

    fun record(write: SettingsWrite)
}
