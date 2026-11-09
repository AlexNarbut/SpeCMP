package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen

import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationCar
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingNameMatch
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController

internal val testTime = LocalDateTime(2026, 10, 21, 14, 20)

internal val testDevice = AlarmDevice(1, "DX-9X", "3.57", DeviceOperationMode.MAIN, emptySet(), can = null)

internal fun cloudSummary(id: Int, deviceName: String = "DX-9X", rating: Int? = null, day: Int = 1) =
    CloudConfigurationSummary(
        id = id,
        name = "Конфигурация $id",
        shortDescription = "Описание $id",
        deviceName = deviceName,
        deviceTitle = deviceName,
        car = ConfigurationCar(7, "Lada", "Niva", ""),
        author = "Автор",
        rating = rating,
        updated = LocalDateTime(2026, 10, day, 12, 0),
        onlyForAuthor = false,
    )

internal fun localConfiguration(id: Long, name: String = "Моя $id", deviceName: String = "DX-9X") =
    LocalConfiguration(id, name, "", deviceName, testTime, testTime)

internal class FakeModelsRepository : DeviceModelsRepository {
    var models: Response<List<String>> = Response.Success(listOf("DX-4GS", "DX-9X"))
    var modelsCalls = 0
    val recentList = mutableListOf("DX-4GS")

    override suspend fun models(): Response<List<String>> {
        modelsCalls++
        return models
    }

    override suspend fun recent(): Response<List<String>> = Response.Success(recentList.toList())

    override suspend fun pushRecent(deviceName: String): Response<List<String>> {
        recentList.remove(deviceName)
        recentList.add(0, deviceName)
        return Response.Success(recentList.toList())
    }

    override suspend fun removeRecent(deviceName: String): Response<List<String>> {
        recentList.remove(deviceName)
        return Response.Success(recentList.toList())
    }
}

internal class FakeCloudRepository : CloudConfigurationsRepository {
    var list: Response<List<CloudConfigurationSummary>> = Response.Success(emptyList())
    var card: Response<CloudConfiguration> = Response.Error(IllegalStateException("no card"))
    val byCarCalls = mutableListOf<Pair<Int, String>>()

    override suspend fun byCar(carId: Int, deviceName: String): Response<List<CloudConfigurationSummary>> {
        byCarCalls += carId to deviceName
        return list
    }

    override suspend fun card(id: Int): Response<CloudConfiguration> = card
}

internal class FakeSettingsRepository :
    DeviceSettingsRepository,
    DeviceSettingsSessionController {
    var open: Response<SettingsSection> = Response.Success(SettingsSection(0, "", "", null, emptyList()))
    var snapshot: Response<SettingsSource.File> = Response.Success(SettingsSource.File("/tmp/snapshot.dxs", "DX-9X"))
    val opened = mutableListOf<SettingsSource>()
    val closed = mutableListOf<SettingsSource>()
    val discarded = mutableListOf<SettingsSource.File>()

    override suspend fun open(source: SettingsSource, reload: Boolean): Response<SettingsSection> {
        opened += source
        return open
    }

    override suspend fun close(source: SettingsSource): Response<Unit> {
        closed += source
        return Response.Success(Unit)
    }

    override suspend fun setValue(source: SettingsSource, settingId: Int, value: SettingValue): Response<Setting> =
        Response.Error(UnsupportedOperationException())

    override suspend fun unsavedIds(source: SettingsSource): Response<Set<Int>> = Response.Success(emptySet())

    override suspend fun save(source: SettingsSource): Response<Unit> = Response.Success(Unit)

    override suspend fun copy(
        from: SettingsSource,
        to: SettingsSource,
        saveChanges: Boolean,
    ): Response<SettingsSection> = Response.Error(UnsupportedOperationException())

    override suspend fun snapshot(from: SettingsSource, deviceName: String): Response<SettingsSource.File> = snapshot

    override fun discardSession(source: SettingsSource) = Unit

    override fun discardSnapshot(snapshot: SettingsSource.File) {
        discarded += snapshot
    }

    override suspend fun search(source: SettingsSource, query: String): Response<List<SettingNameMatch>> =
        Response.Success(emptyList())

    override suspend fun resetDeviceToFactory(): Response<Unit> = Response.Success(Unit)
}
