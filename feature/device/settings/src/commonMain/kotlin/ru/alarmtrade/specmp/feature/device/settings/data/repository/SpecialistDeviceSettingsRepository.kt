package ru.alarmtrade.specmp.feature.device.settings.data.repository

import kotlin.random.Random
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.common.settings.interaction.model.errors.WrongSettingsSizeException
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.AlarmDeviceSettingsInteractor
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CloseDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CopySettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.DeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.OpenDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfError
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.settings.data.files.deleteSettingsScratchFile
import ru.alarmtrade.specmp.feature.device.settings.data.files.settingsScratchDirectory
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * [DeviceSettingsRepository] над `alarmDeviceSettingsInteractor` (сессии настроек по `SettingsPath`) и
 * `bluetoothAlarmDeviceInteractor` (сброс блока). Интеракторы — лениво; [appScope] — удаление снимков,
 * переживающее экран.
 */
class SpecialistDeviceSettingsRepository(
    private val settingsInteractor: () -> AlarmDeviceSettingsInteractor,
    private val alarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
    private val appScope: CoroutineScope,
    private val dispatchers: AppDispatchers,
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
        saveChanges: Boolean,
    ): Response<SettingsSection> {
        val interactor = settingsInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.copyDeviceSettingsUseCase
            .execute(CopySettingsRequest(from.toPath(), to.toPath(), saveChanges))
            .toResponse()
            .map { it.toRootSection() }
            .mapIfError { error ->
                if (error.exception is WrongSettingsSizeException) {
                    Response.Error(
                        SettingsMismatchException(error.exception),
                    )
                } else {
                    error
                }
            }
    }

    // Файла снимка ещё нет: SDK не открывает приёмник и пишет в него байты источника как есть; при чтении меню
    // строится по модели из пути — так, как настройки увидит блок этой модели.
    override suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File> {
        val path = "${settingsScratchDirectory()}/snapshot-${Random.nextLong().toULong().toString(16)}.dxs"
        val snapshot = SettingsSource.File(path, deviceName)
        return copy(from, snapshot, saveChanges = true).map { snapshot }
    }

    override fun discardSnapshot(snapshot: SettingsSource.File) {
        appScope.launch(dispatchers.io) {
            close(snapshot)
            deleteSettingsScratchFile(snapshot.path)
        }
    }

    override suspend fun resetDeviceToFactory(): Response<Unit> {
        val interactor = alarmDeviceInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.resetAlarmDeviceSettingsUseCase.execute(EmptyRequest()).toResponse()
    }
}
