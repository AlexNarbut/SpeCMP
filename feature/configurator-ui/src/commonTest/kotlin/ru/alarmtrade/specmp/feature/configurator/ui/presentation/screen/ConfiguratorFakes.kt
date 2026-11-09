package ru.alarmtrade.specmp.feature.configurator.ui.presentation.screen

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.testDispatchers
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary
import ru.alarmtrade.specmp.feature.configurator.domain.model.ConfigurationCar
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.CloudConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.interaction.di.createDeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.model.Setting
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingValue
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSection
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController

internal val testTime = LocalDateTime(2026, 10, 21, 14, 20)

internal val testDevice = AlarmDevice(1, "DX-9X", "3.57", DeviceOperationMode.MAIN, emptySet(), can = null)

internal fun TestScope.testDeviceSession(link: MutableStateFlow<DeviceLinkState>, dispatcher: TestDispatcher) =
    createDeviceSession(
        FakeDeviceSessionRepository(link),
        FakeDeviceConnectionRepository,
        testDispatchers(dispatcher),
        backgroundScope.coroutineContext,
    ).also { it.start() }

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

internal class FakeLocalConfigurations : LocalConfigurationsRepository {
    override val changes = MutableSharedFlow<Response<Unit>>(extraBufferCapacity = 1)
    var items: Response<List<LocalConfiguration>> = Response.Success(emptyList())
    var allCalls = 0
    var created: Response<SettingsSource.LocalConfiguration> =
        Response.Success(SettingsSource.LocalConfiguration(100, "DX-9X", "Новая"))
    val createCalls = mutableListOf<Pair<String, SettingsSource>>()
    val deleted = mutableListOf<Long>()

    override suspend fun all(): Response<List<LocalConfiguration>> {
        allCalls++
        return items
    }

    override suspend fun create(
        name: String,
        description: String,
        deviceName: String,
        from: SettingsSource,
    ): Response<SettingsSource.LocalConfiguration> {
        createCalls += name to from
        return created
    }

    override suspend fun updateInfo(
        configuration: LocalConfiguration,
        name: String,
        description: String,
    ): Response<SettingsSource.LocalConfiguration> =
        Response.Success(SettingsSource.LocalConfiguration(configuration.id + 1000, configuration.deviceName, name))

    override suspend fun delete(id: Long): Response<Unit> {
        deleted += id
        return Response.Success(Unit)
    }
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

    override suspend fun resetDeviceToFactory(): Response<Unit> = Response.Success(Unit)
}

private class FakeDeviceSessionRepository(private val link: Flow<DeviceLinkState>) : DeviceSessionRepository {
    override fun linkState(): Flow<Response<DeviceLinkState>> = link.map { Response.Success(it) }
    override suspend fun readBluetoothModule(): Response<BluetoothModule> =
        Response.Error(IllegalStateException("usb only"))
    override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(testDevice)
    override suspend fun readImei(): Response<String> = Response.Success("")
    override suspend fun syncDeviceDatabase(): Response<Unit> = Response.Success(Unit)
    override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
    override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> =
        Response.Success(BootModeResult.Entered)
    override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
    override suspend fun setServicePin(pin: ServicePin): Response<Unit> = Response.Success(Unit)
}

private object FakeDeviceConnectionRepository : DeviceConnectionRepository {
    override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override fun connectSerial(portId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
    override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
}
