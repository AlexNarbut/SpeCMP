package ru.alarmtrade.specmp.feature.mountassistant.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsSessionController
import ru.alarmtrade.specmp.feature.device.settings.domain.service.DeviceSettingsWrites
import ru.alarmtrade.specmp.feature.device.settings.domain.service.SettingsWrite
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantSettingValue
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FoundSetting
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.SettingEditor
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantAutostartRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantDeviceRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantFirmwareRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantSettingsRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantStateRepository

internal val testFacts = DeviceFacts(
    systemId = 77,
    title = "DX4G  77",
    firmwareVersion = "3.56",
    transport = AssistantTransport.BLE,
    capabilities = AssistantCapability.entries.toSet(),
    can = CanFacts(factory = false, code = "1234", version = "12.05"),
    online = true,
)

internal val testCheck = FirmwareCheck(
    listOf(FirmwareModuleCheck(1, FirmwareModuleRole.MAIN, "DX4G", "3.56", "3.57", updateAvailable = true)),
)

internal val testSummary = StateSummary(true, emptySet(), false, false, 12.6f, 20)

internal class FakeAssistantDeviceRepository(facts: DeviceFacts? = testFacts) : AssistantDeviceRepository {
    val facts = MutableStateFlow(facts)

    override fun facts(): Flow<Response<DeviceFacts?>> = this.facts.map { Response.Success(it) }
}

internal class FakeAssistantFirmwareRepository : AssistantFirmwareRepository {
    val results = mutableListOf<Response<FirmwareCheck>>()
    var gate: CompletableDeferred<Unit>? = null
    var calls = 0

    override suspend fun check(): Response<FirmwareCheck> {
        calls++
        gate?.await()
        return results.removeFirstOrNull() ?: Response.Success(testCheck)
    }
}

internal class FakeAssistantAutostartRepository : AssistantAutostartRepository {
    var status: Response<AutostartStatus> = Response.Success(AutostartStatus(false, CloneState.REQUIRED, 3))
    var calls = 0

    override suspend fun status(): Response<AutostartStatus> {
        calls++
        return status
    }
}

internal class FakeAssistantStateRepository : AssistantStateRepository {
    var summary: Response<StateSummary> = Response.Success(testSummary)

    override suspend fun summary(): Response<StateSummary> = summary
}

internal class FakeAssistantSettingsRepository : AssistantSettingsRepository {
    var found: List<FoundSetting> = emptyList()
    val writeResults = mutableListOf<Response<Unit>>()
    val saveResults = mutableListOf<Response<Unit>>()
    var others = 0
    val writes = mutableListOf<Pair<Int, AssistantSettingValue>>()
    val reverts = mutableListOf<Pair<Int, AssistantSettingValue>>()
    var saves = 0
    val queries = mutableListOf<String>()

    override suspend fun find(query: String): Response<List<FoundSetting>> {
        queries += query
        return Response.Success(found)
    }

    override suspend fun setting(id: Int): Response<FoundSetting> =
        found.firstOrNull { it.id == id }?.let { Response.Success(it) } ?: Response.Error(NoSuchElementException())

    override suspend fun otherUnsaved(settingId: Int): Response<Int> = Response.Success(others)

    override suspend fun write(settingId: Int, value: AssistantSettingValue): Response<Unit> {
        writes += settingId to value
        return writeResults.removeFirstOrNull() ?: Response.Success(Unit)
    }

    override suspend fun save(): Response<Unit> {
        saves++
        return saveResults.removeFirstOrNull() ?: Response.Success(Unit)
    }

    override suspend fun revert(settingId: Int, value: AssistantSettingValue): Response<Unit> {
        reverts += settingId to value
        return Response.Success(Unit)
    }
}

internal class FakeSessionController : DeviceSettingsSessionController {
    val discarded = mutableListOf<SettingsSource>()

    override fun discardSession(source: SettingsSource) {
        discarded += source
    }

    override fun discardSnapshot(snapshot: SettingsSource.File) = Unit
}

internal val switchSetting = FoundSetting(
    id = 11,
    name = "Автозапуск по температуре",
    path = listOf("Автозапуск"),
    nameHighlights = emptyList(),
    value = AssistantSettingValue.Switch(false),
    editor = SettingEditor.Switch(null, null),
)

internal val timeSetting = FoundSetting(
    id = 23,
    name = "Время запуска",
    path = listOf("Автозапуск"),
    nameHighlights = emptyList(),
    value = null,
    editor = null,
)

internal class FakeSettingsWrites : DeviceSettingsWrites {
    private val events = MutableSharedFlow<SettingsWrite>(extraBufferCapacity = 8)
    val recorded = mutableListOf<SettingsWrite>()

    override fun writes(): Flow<Response<SettingsWrite>> = events.map { Response.Success(it) }

    override fun record(write: SettingsWrite) {
        recorded += write
        events.tryEmit(write)
    }
}
