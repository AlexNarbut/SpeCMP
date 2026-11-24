package ru.alarmtrade.specmp.assistant

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.AutostartState
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneCondition
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.AutostartStatusRepository
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareRepository
import ru.alarmtrade.specmp.feature.device.firmware.domain.usecase.CheckFirmwareUseCase
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.device.state.domain.model.Autostart
import ru.alarmtrade.specmp.feature.device.state.domain.model.CommandReply
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.EngineStatus
import ru.alarmtrade.specmp.feature.device.state.domain.model.HandsFreeMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.LinkInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.PowerInfo
import ru.alarmtrade.specmp.feature.device.state.domain.model.SecurityState
import ru.alarmtrade.specmp.feature.device.state.domain.model.SirenMode
import ru.alarmtrade.specmp.feature.device.state.domain.model.StateCommand
import ru.alarmtrade.specmp.feature.device.state.domain.model.VehicleState
import ru.alarmtrade.specmp.feature.device.state.domain.model.Zone
import ru.alarmtrade.specmp.feature.device.state.domain.model.ZoneValue
import ru.alarmtrade.specmp.feature.device.state.domain.repository.DeviceStateRepository
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AutostartStatus
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CloneState
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleCheck
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.FirmwareModuleRole
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateSummary
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.StateZone

class AssistantPortAdaptersTest {

    private class FakeFirmwareRepository : FirmwareRepository {
        var modules: List<FirmwareModule> = emptyList()
        val latest = mutableMapOf<Long, FirmwareVersion>()

        override suspend fun modules(deviceName: String, transport: ConnectionTransport) = Response.Success(modules)

        override suspend fun latest(moduleId: Long, channel: FirmwareChannel, can: FirmwareCan?) =
            latest[moduleId]?.let { Response.Success(it) } ?: Response.Error(IllegalStateException("none"))

        override suspend fun moduleId(deviceName: String, kind: FirmwareModuleKind) = Response.Success(null)

        override suspend fun serialModuleId(deviceName: String) = Response.Success(null)

        override suspend fun systems(): Response<List<FirmwareSystem>> = Response.Success(emptyList())

        override suspend fun download(url: String): Response<ByteArray> = Response.Error(IllegalStateException())

        override suspend fun archivePage(
            moduleId: Long,
            channel: FirmwareChannel,
            page: Int,
            pageSize: Int,
            can: FirmwareCan?,
        ): Response<FirmwareVersionPage> = Response.Error(IllegalStateException())
    }

    private fun version(value: String) = FirmwareVersion(0, value, LocalDate(2026, 1, 1), "", "")

    private fun TestScope.session() = testDeviceSession(
        FakeDeviceSessionRepository(),
        dispatcher = UnconfinedTestDispatcher(testScheduler),
        transport = ConnectionTransport.BLE,
    )

    @Test
    fun `firmware check keeps updatable modules with roles`() = runTest {
        val repository = FakeFirmwareRepository().apply {
            modules = listOf(
                FirmwareModule(1, FirmwareModuleKind.AlarmMain, "DX4G", "Pandora", BootModeTarget.MainProgram),
                FirmwareModule(
                    2,
                    FirmwareModuleKind.BluetoothModule,
                    "BT",
                    "Bluetooth",
                    BootModeTarget.BluetoothModule,
                ),
                FirmwareModule(3, FirmwareModuleKind.WatchBluetooth, "PW", "Watch", bootTarget = null),
            )
            latest[1] = version("3.58")
            latest[2] = version("1.14")
        }
        val session = session()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { session.state.collect {} }
        val adapter = AppAssistantFirmwareRepository(session, CheckFirmwareUseCase(repository, session))

        val check = (adapter.check() as Response.Success).value

        assertEquals(
            listOf(
                FirmwareModuleCheck(
                    1,
                    FirmwareModuleRole.MAIN,
                    "Pandora (DX4G)",
                    "3.57",
                    "3.58",
                    updateAvailable = true,
                ),
                FirmwareModuleCheck(
                    2,
                    FirmwareModuleRole.BLUETOOTH,
                    "Bluetooth (BT)",
                    "1.14",
                    "1.14",
                    updateAvailable = false,
                ),
            ),
            check.modules,
        )
    }

    @Test
    fun `autostart status adds clone attempts when the block gives them`() = runTest {
        val status = object : AutostartStatusRepository {
            override suspend fun status() = Response.Success(AutostartState(true, CloneCondition.REQUIRED))
        }
        var attempts: Response<Int?> = Response.Success(2)
        val clone = object : CloneRepository {
            override suspend fun attempts(): Response<Int?> = attempts

            override fun run(): Flow<Response<CloneEvent>> = emptyFlow()
        }
        val adapter = AppAssistantAutostartRepository(status, clone)

        assertEquals(Response.Success(AutostartStatus(true, CloneState.REQUIRED, 2)), adapter.status())
        attempts = Response.Error(IllegalStateException())
        assertEquals(Response.Success(AutostartStatus(true, CloneState.REQUIRED, null)), adapter.status())
    }

    private class FakeStateRepository(private val source: Flow<Response<VehicleState>>) : DeviceStateRepository {
        override fun states(): Flow<Response<VehicleState>> = source

        override suspend fun send(command: StateCommand): Response<CommandReply> =
            Response.Error(IllegalStateException())
    }

    private val vehicle = VehicleState(
        security = SecurityState.ARMED,
        alarm = false,
        serviceMode = false,
        programMode = false,
        zones = mapOf(Zone.DOOR_REAR_LEFT to ZoneValue.ON, Zone.HOOD to ZoneValue.ALARM, Zone.IGNITION to ZoneValue.ON),
        engine = EngineInfo(EngineStatus.RUNNING, temperature = 85, rpm = 900, autostart = Autostart.Disabled),
        power = PowerInfo(voltage = 13.9f, cabinTemp = 21, outsideTemp = 12, fuelPercent = null),
        link = LinkInfo(gsmLevel = null, gpsFix = false, speedKmh = null, sms = false, calls = false),
        event = null,
        can = null,
        ev = null,
        heater = null,
        camera = null,
        siren = SirenMode.OFF,
        handsFree = HandsFreeMode.OFF,
    )

    @Test
    fun `state summary takes the first successful snapshot`() = runTest {
        val adapter = AppAssistantStateRepository(
            FakeStateRepository(flowOf(Response.Error(IllegalStateException()), Response.Success(vehicle))),
        )
        assertEquals(
            Response.Success(
                StateSummary(
                    armed = true,
                    openZones = setOf(StateZone.DOORS, StateZone.HOOD),
                    ignitionOn = true,
                    engineRunning = true,
                    voltage = 13.9f,
                    temperature = 85,
                ),
            ),
            adapter.summary(),
        )
    }

    @Test
    fun `state summary without snapshot is a timeout`() = runTest {
        val adapter = AppAssistantStateRepository(FakeStateRepository(flowOf(Response.Error(IllegalStateException()))))
        assertEquals(ApiError.Timeout, assertIs<Response.Error>(adapter.summary()).exception.appError)
    }
}
