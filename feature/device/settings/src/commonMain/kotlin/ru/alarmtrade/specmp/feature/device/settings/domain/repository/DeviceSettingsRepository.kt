package ru.alarmtrade.specmp.feature.device.settings.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/** Правки ([setValue]) живут в сессии SDK и уходят в источник только по [save]. */
interface DeviceSettingsRepository {
    /** Открыть сессию и прочитать меню; [reload] — перечитать из источника, а не из кэша сессии SDK. */
    suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection>

    suspend fun close(source: SettingsSource): Response<Unit>

    suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting>

    suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>>

    suspend fun save(source: SettingsSource): Response<Unit>

    /** Другой набор настроек — [SettingsMismatchException]; скопированное SDK не отмечает в [unsavedIds]. */
    suspend fun copy(from: SettingsSource, to: SettingsSource, saveChanges: Boolean): Response<SettingsSection>

    /** Прочитан моделью [deviceName] — так их увидит блок этой модели; сессия [from] не закрывается. */
    suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File>

    /** Нечёткий поиск по названию в открытой сессии [source]; по релевантности, разделы не ищутся. */
    suspend fun search(source: SettingsSource, query: String): Response<List<SettingNameMatch>>

    suspend fun resetDeviceToFactory(): Response<Unit>
}
