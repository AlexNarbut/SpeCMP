package ru.alarmtrade.specmp.feature.configurator.data.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

internal class FakeDeviceSettingsRepository : DeviceSettingsRepository {
    var copyResult: Response<SettingsSection> = Response.Success(SettingsSection(0, "", "", null, emptyList()))
    val copies = mutableListOf<Triple<SettingsSource, SettingsSource, Boolean>>()
    val closed = mutableListOf<SettingsSource>()

    override suspend fun copy(
        from: SettingsSource,
        to: SettingsSource,
        saveChanges: Boolean,
    ): Response<SettingsSection> {
        copies += Triple(from, to, saveChanges)
        return copyResult
    }

    override suspend fun close(source: SettingsSource): Response<Unit> {
        closed += source
        return Response.Success(Unit)
    }

    override suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection> = error("not used")

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> =
        error("not used")

    override suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>> = error("not used")

    override suspend fun save(source: SettingsSource): Response<Unit> = error("not used")

    override suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File> =
        error("not used")

    override suspend fun resetDeviceToFactory(): Response<Unit> = error("not used")
}
