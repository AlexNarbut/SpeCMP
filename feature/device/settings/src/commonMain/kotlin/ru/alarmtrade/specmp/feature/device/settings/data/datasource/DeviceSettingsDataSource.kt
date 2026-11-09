package ru.alarmtrade.specmp.feature.device.settings.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.common.settings.interaction.model.menu.AlarmDeviceSettingsMenu
import ru.alarmtrade.common.settings.interaction.model.search.SettingSearchResult
import ru.alarmtrade.specialist.domain.alarmDevice.settings.menu.SettingItem
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.AlarmDeviceSettingsInteractor
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CloseDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CopySettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.DeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.FindSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.OpenDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.SetDeviceSettingValueRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface DeviceSettingsDataSource {
    suspend fun open(request: OpenDeviceSettingsRequest): SpecialistResponse<AlarmDeviceSettingsMenu>?
    suspend fun close(request: CloseDeviceSettingsRequest): SpecialistResponse<Unit>?
    suspend fun setValue(request: SetDeviceSettingValueRequest): SpecialistResponse<SettingItem>?
    suspend fun unsaved(request: DeviceSettingsRequest): SpecialistResponse<List<SettingItem>>?
    suspend fun save(request: DeviceSettingsRequest): SpecialistResponse<Unit>?
    suspend fun copy(request: CopySettingsRequest): SpecialistResponse<SettingItem>?
    suspend fun search(request: FindSettingsRequest): SpecialistResponse<List<SettingSearchResult>>?
    suspend fun resetDeviceToFactory(): SpecialistResponse<Unit>?
}

internal class SpecialistDeviceSettingsDataSource(
    private val settings: () -> AlarmDeviceSettingsInteractor,
    private val alarmDevice: () -> BluetoothAlarmDeviceInteractor,
) : DeviceSettingsDataSource {
    override suspend fun open(request: OpenDeviceSettingsRequest): SpecialistResponse<AlarmDeviceSettingsMenu>? =
        settings.resolveOrNull()?.openDeviceSettingsUseCase?.execute(request)

    override suspend fun close(request: CloseDeviceSettingsRequest): SpecialistResponse<Unit>? =
        settings.resolveOrNull()?.closeDeviceSettingsUseCase?.execute(request)

    override suspend fun setValue(request: SetDeviceSettingValueRequest): SpecialistResponse<SettingItem>? =
        settings.resolveOrNull()?.setDeviceSettingValueUseCase?.execute(request)

    override suspend fun unsaved(request: DeviceSettingsRequest): SpecialistResponse<List<SettingItem>>? =
        settings.resolveOrNull()?.getUnsavedSettingsUseCase?.execute(request)

    override suspend fun save(request: DeviceSettingsRequest): SpecialistResponse<Unit>? =
        settings.resolveOrNull()?.saveDeviceSettingsUseCase?.execute(request)

    override suspend fun copy(request: CopySettingsRequest): SpecialistResponse<SettingItem>? =
        settings.resolveOrNull()?.copyDeviceSettingsUseCase?.execute(request)

    override suspend fun search(request: FindSettingsRequest): SpecialistResponse<List<SettingSearchResult>>? =
        settings.resolveOrNull()?.searchSettingsUseCase?.execute(request)

    override suspend fun resetDeviceToFactory(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.resetAlarmDeviceSettingsUseCase?.execute(EmptyRequest())
}
