package ru.alarmtrade.specmp.feature.mountassistant.testing

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SwitchSetting
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

internal class FakeDeviceSettingsRepository(var root: SettingsSection = testSettingsRoot) : DeviceSettingsRepository {
    var openResult: Response<SettingsSection>? = null
    var searchResult: Response<List<SettingNameMatch>> = Response.Success(emptyList())
    val saveResults = mutableListOf<Response<Unit>>()
    var unsaved: Set<Int> = emptySet()
    val opens = mutableListOf<Pair<SettingsSource, Boolean>>()
    val values = mutableListOf<Pair<Int, SettingValue>>()
    var saves = 0
    var closes = 0

    override suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection> {
        opens += source to reload
        return openResult ?: Response.Success(root)
    }

    override suspend fun close(source: SettingsSource): Response<Unit> {
        closes++
        return Response.Success(Unit)
    }

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> {
        values += settingId to value
        unsaved = unsaved + settingId
        return Response.Success(
            SwitchSetting(
                settingId,
                "",
                "",
                null,
                value as? SettingValue.Switch ?: SettingValue.Switch(false),
                null,
                null,
            ),
        )
    }

    override suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>> = Response.Success(unsaved)

    override suspend fun save(source: SettingsSource): Response<Unit> {
        saves++
        val result = saveResults.removeFirstOrNull() ?: Response.Success(Unit)
        if (result is Response.Success) unsaved = emptySet()
        return result
    }

    override suspend fun copy(
        from: SettingsSource,
        to: SettingsSource,
        saveChanges: Boolean,
    ): Response<SettingsSection> = Response.Error(UnsupportedOperationException())

    override suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File> =
        Response.Error(UnsupportedOperationException())

    override suspend fun search(source: SettingsSource, query: String): Response<List<SettingNameMatch>> = searchResult

    override suspend fun resetDeviceToFactory(): Response<Unit> = Response.Error(UnsupportedOperationException())
}
