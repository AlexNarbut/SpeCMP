package ru.alarmtrade.specmp.feature.device.settings.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.settings.interaction.model.menu.AlarmDeviceSettingsMenu
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SettingItem
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CloseDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CopySettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.DeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.OpenDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingValueRequest
import ru.alarmtrade.specmp.feature.device.settings.data.datasource.DeviceSettingsDataSource

internal class FakeDeviceSettingsDataSource : DeviceSettingsDataSource {
    var menu: SpecialistResponse<AlarmDeviceSettingsMenu>? = null
    var item: SpecialistResponse<SettingItem>? = null
    var unsaved: SpecialistResponse<List<SettingItem>>? = SpecialistResponse.Success(emptyList())
    var unitResult: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)

    val openRequests = mutableListOf<OpenDeviceSettingsRequest>()
    val closeRequests = mutableListOf<CloseDeviceSettingsRequest>()
    val valueRequests = mutableListOf<SetDeviceSettingValueRequest>()
    val copyRequests = mutableListOf<CopySettingsRequest>()
    val saveRequests = mutableListOf<DeviceSettingsRequest>()
    var resets = 0

    override suspend fun open(request: OpenDeviceSettingsRequest): SpecialistResponse<AlarmDeviceSettingsMenu>? {
        openRequests += request
        return menu
    }

    override suspend fun close(request: CloseDeviceSettingsRequest): SpecialistResponse<Unit>? {
        closeRequests += request
        return unitResult
    }

    override suspend fun setValue(request: SetDeviceSettingValueRequest): SpecialistResponse<SettingItem>? {
        valueRequests += request
        return item
    }

    override suspend fun unsaved(request: DeviceSettingsRequest): SpecialistResponse<List<SettingItem>>? = unsaved

    override suspend fun save(request: DeviceSettingsRequest): SpecialistResponse<Unit>? {
        saveRequests += request
        return unitResult
    }

    override suspend fun copy(request: CopySettingsRequest): SpecialistResponse<SettingItem>? {
        copyRequests += request
        return item
    }

    override suspend fun resetDeviceToFactory(): SpecialistResponse<Unit>? {
        resets++
        return unitResult
    }
}
