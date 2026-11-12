package ru.alarmtrade.specmp.feature.device.settings.data.repository

import kotlin.random.Random
import ru.alarmtrade.common.settings.interaction.model.errors.WrongSettingsSizeException
import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CloseDeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.CopySettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.DeviceSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.FindSettingsRequest
import ru.alarmtrade.specialist.interactors.alarmDeviceSettings.requests.OpenDeviceSettingsRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.mapIfError
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.settings.data.datasource.DeviceSettingsDataSource
import ru.alarmtrade.specmp.feature.device.settings.data.files.settingsScratchDirectory
import ru.alarmtrade.specmp.feature.device.settings.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.device.settings.data.mapper.toNameMatch
import ru.alarmtrade.specmp.feature.device.settings.data.mapper.toPath
import ru.alarmtrade.specmp.feature.device.settings.data.mapper.toRequest
import ru.alarmtrade.specmp.feature.device.settings.data.mapper.toRootSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsMismatchException
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsWrongPinException
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

internal class SpecialistDeviceSettingsRepository(private val dataSource: DeviceSettingsDataSource) :
    DeviceSettingsRepository {

    override suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection> =
        dataSource.open(OpenDeviceSettingsRequest(source.toPath(), reload))
            .toResponseOrUnavailable()
            .map { it.settings.toRootSection() }

    override suspend fun close(source: SettingsSource): Response<Unit> =
        dataSource.close(CloseDeviceSettingsRequest(source.toPath(), saveChanges = false)).toResponseOrUnavailable()

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> =
        dataSource.setValue(value.toRequest(source.toPath(), settingId))
            .toResponseOrUnavailable()
            .mapIfSuccess { item ->
                when (val node = item.toDomain()) {
                    is Setting -> Response.Success(node)
                    is SettingsSection -> Response.Error(IllegalStateException("Setting $settingId is a section"))
                }
            }

    override suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>> =
        dataSource.unsaved(DeviceSettingsRequest(source.toPath()))
            .toResponseOrUnavailable()
            .map { list -> list.mapTo(mutableSetOf()) { it.id } }

    override suspend fun save(source: SettingsSource): Response<Unit> =
        dataSource.save(DeviceSettingsRequest(source.toPath()))
            .toResponseOrUnavailable()
            .mapIfError { error ->
                if (error.exception is IncorrectServicePinException) {
                    Response.Error(SettingsWrongPinException(error.exception))
                } else {
                    error
                }
            }

    override suspend fun copy(
        from: SettingsSource,
        to: SettingsSource,
        saveChanges: Boolean,
    ): Response<SettingsSection> = dataSource.copy(CopySettingsRequest(from.toPath(), to.toPath(), saveChanges))
        .toResponseOrUnavailable()
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

    // Файла снимка ещё нет: SDK не открывает приёмник и пишет в него байты источника как есть; при чтении меню
    // строится по модели из пути — так, как настройки увидит блок этой модели.
    override suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File> {
        val path = "${settingsScratchDirectory()}/snapshot-${Random.nextLong().toULong().toString(16)}.dxs"
        val snapshot = SettingsSource.File(path, deviceName)
        return copy(from, snapshot, saveChanges = true).map { snapshot }
    }

    override suspend fun search(source: SettingsSource, query: String): Response<List<SettingNameMatch>> =
        dataSource.search(FindSettingsRequest(source.toPath(), query))
            .toResponseOrUnavailable()
            .map { results -> results.map { it.toNameMatch() } }

    override suspend fun resetDeviceToFactory(): Response<Unit> =
        dataSource.resetDeviceToFactory().toResponseOrUnavailable()
}
