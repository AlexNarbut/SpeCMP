package ru.alarmtrade.specmp.feature.device.settings.data.repository

import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.AlarmDeviceSettingsInteractor
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CloseDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CopySettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.DeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.OpenDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * [DeviceSettingsRepository] над `alarmDeviceSettingsInteractor` (сессии настроек по `SettingsPath`) и
 * `bluetoothAlarmDeviceInteractor` (сброс блока). Интеракторы — лениво.
 */
class SpecialistDeviceSettingsRepository(
    private val settingsInteractor: () -> AlarmDeviceSettingsInteractor,
    private val alarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor
) : DeviceSettingsRepository {

    override suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.openDeviceSettingsUseCase
            .execute(OpenDeviceSettingsRequest(source.toPath(), reload))
            .toResponse()
            .map { it.settings.toRootSection() }
    }

    override suspend fun close(source: SettingsSource): Response<Unit> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.closeDeviceSettingsUseCase
            .execute(CloseDeviceSettingsRequest(source.toPath(), saveChanges = false))
            .toResponse()
    }

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.setDeviceSettingValueUseCase
            .execute(value.toRequest(source.toPath(), settingId))
            .toResponse()
            .mapIfSuccess { item ->
                when (val node = item.toDomain()) {
                    is Setting -> Response.Success(node)
                    is SettingsSection -> Response.Error(IllegalStateException("Setting $settingId is a section"))
                }
            }
    }

    override suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.getUnsavedSettingsUseCase
            .execute(DeviceSettingsRequest(source.toPath()))
            .toResponse()
            .map { list -> list.mapTo(mutableSetOf()) { it.id } }
    }

    override suspend fun save(source: SettingsSource): Response<Unit> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.saveDeviceSettingsUseCase.execute(DeviceSettingsRequest(source.toPath())).toResponse()
    }

    override suspend fun copy(
        from: SettingsSource,
        to: SettingsSource,
        saveChanges: Boolean
    ): Response<SettingsSection> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.copyDeviceSettingsUseCase
            .execute(CopySettingsRequest(from.toPath(), to.toPath(), saveChanges))
            .toResponse()
            .map { it.toRootSection() }
    }

    override suspend fun resetDeviceToFactory(): Response<Unit> {
        val interactor = alarmDeviceInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.resetAlarmDeviceSettingsUseCase.execute(EmptyRequest()).toResponse()
    }
}
