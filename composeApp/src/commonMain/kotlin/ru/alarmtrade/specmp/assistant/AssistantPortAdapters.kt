package ru.alarmtrade.specmp.assistant

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.AppException
import ru.alarmtrade.specmp.core.common.PlatformError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.AutostartState
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneCondition
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.AutostartStatusRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleStatus
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.displayTitle
import ru.alarmtrade.specmp.feature.device.firmware.domain.usecase.CheckFirmwareUseCase
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneTone
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateZone
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantAutostartRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantFirmwareRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantStateRepository

private val StateTimeout = 10.seconds

private fun deviceLost(): Response.Error =
    Response.Error(AppException(PlatformError.DeviceDisconnected, IllegalStateException("Device is not ready")))

/** Модули, которые на этом подключении не обновить, в ответ ассистенту не попадают. */
internal class AppAssistantFirmwareRepository(
    private val deviceSession: DeviceSession,
    private val checkFirmware: CheckFirmwareUseCase,
) : AssistantFirmwareRepository {

    override suspend fun check(): Response<FirmwareCheck> {
        val device = (deviceSession.state.value as? DeviceSessionState.Ready)?.device ?: return deviceLost()
        return checkFirmware(device).map { statuses ->
            FirmwareCheck(statuses.filter { it.module.isBootSupported }.map { it.toModuleCheck() })
        }
    }
}

private fun FirmwareModuleStatus.toModuleCheck(): FirmwareModuleCheck = FirmwareModuleCheck(
    id = module.id,
    role = when (module.kind) {
        FirmwareModuleKind.AlarmMain -> FirmwareModuleRole.MAIN
        FirmwareModuleKind.BluetoothModule -> FirmwareModuleRole.BLUETOOTH
        else -> FirmwareModuleRole.OTHER
    },
    title = module.displayTitle,
    installed = installed,
    latest = latest?.version,
    updateAvailable = updateAvailable,
)

/** Попытки Clone — дополнение: если блок их не отдал, статус всё равно есть. */
internal class AppAssistantAutostartRepository(
    private val statusRepository: AutostartStatusRepository,
    private val cloneRepository: CloneRepository,
) : AssistantAutostartRepository {

    override suspend fun status(): Response<AutostartStatus> = statusRepository.status().map { state ->
        state.toStatus(cloneRepository.attempts().getValueIfSuccess())
    }
}

private fun AutostartState.toStatus(attempts: Int?): AutostartStatus = AutostartStatus(
    bypassLearned = bypassLearned,
    clone = when (clone) {
        CloneCondition.NOT_SUPPORTED -> CloneState.NOT_NEEDED
        CloneCondition.REQUIRED -> CloneState.REQUIRED
        CloneCondition.COMPLETED -> CloneState.DONE
        CloneCondition.FAILED -> CloneState.FAILED
    },
    cloneAttempts = attempts,
)

/** Первый успешный снимок опроса; сбор отменяется — опрос останавливается (экран проверки в это время закрыт). */
internal class AppAssistantStateRepository(private val stateRepository: DeviceStateRepository) :
    AssistantStateRepository {

    override suspend fun summary(): Response<StateSummary> {
        val first = withTimeoutOrNull(StateTimeout) { stateRepository.states().firstOrNull { it is Response.Success } }
            ?: return Response.Error(AppException(ApiError.Timeout, IllegalStateException("No state in $StateTimeout")))
        return first.map { it.toSummary() }
    }
}

private val DoorZones = listOf(Zone.DOOR_FRONT_LEFT, Zone.DOOR_FRONT_RIGHT, Zone.DOOR_REAR_LEFT, Zone.DOOR_REAR_RIGHT)

private fun VehicleState.isActive(zone: Zone): Boolean = zone(zone).tone.let {
    it == ZoneTone.ACTIVE ||
        it == ZoneTone.ALARM
}

private fun VehicleState.toSummary(): StateSummary = StateSummary(
    armed = security != SecurityState.DISARMED,
    openZones = buildSet {
        if (DoorZones.any { isActive(it) }) add(StateZone.DOORS)
        if (isActive(Zone.HOOD)) add(StateZone.HOOD)
        if (isActive(Zone.TRUNK)) add(StateZone.TRUNK)
    },
    ignitionOn = isActive(Zone.IGNITION),
    engineRunning = engine.status == EngineStatus.RUNNING,
    voltage = power.voltage,
    temperature = engine.temperature,
)
