package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmwareType
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.DeviceType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specialist.interactors.files.requests.DownloadFileRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwareRequest
import ru.alarmtrade.specialist.interactors.firmwareFiles.requests.SubsystemFirmwaresRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareCan
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareChannel
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModule
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSubsystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareSystem
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersionPage
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

class SpecialistFirmwareRepositoryTest {
    private val dataSource = FakeFirmwareDataSource()
    private val repository = SpecialistFirmwareRepository(dataSource)

    @Test
    fun `modules of the device map subsystems for the transport`() = runTest {
        dataSource.information = SpecialistResponse.Success(device(MAIN, BLUETOOTH))

        val modules = assertIs<Response.Success<List<FirmwareModule>>>(
            repository.modules("DX-9X", ConnectionTransport.USB),
        ).value

        assertEquals(listOf("DX-9X"), dataSource.deviceNames)
        assertEquals(listOf(FirmwareModuleKind.AlarmMain, FirmwareModuleKind.BluetoothModule), modules.map { it.kind })
        assertEquals(listOf(BootModeTarget.MainProgram, null), modules.map { it.bootTarget })
    }

    @Test
    fun `module id is the subsystem of the kind or null`() = runTest {
        dataSource.information = SpecialistResponse.Success(device(MAIN, BLUETOOTH))

        assertEquals(Response.Success(8L), repository.moduleId("DX-9X", FirmwareModuleKind.BluetoothModule))
        assertEquals(Response.Success(null), repository.moduleId("DX-9X", FirmwareModuleKind.CanModule))
    }

    @Test
    fun `latest version sends channel and can and maps the version`() = runTest {
        dataSource.latest = SpecialistResponse.Success(firmware("3.61"))

        val version = assertIs<Response.Success<FirmwareVersion>>(
            repository.latest(7, FirmwareChannel.BETA, FirmwareCan(code = "C1", version = 4)),
        ).value

        assertEquals(
            SubsystemFirmwareRequest(7, SubsystemFirmwareType.BETA, canCode = "C1", canCodeVersion = 4),
            dataSource.requests.single(),
        )
        assertEquals("3.61", version.version)
        assertEquals(LocalDate(2026, 10, 14), version.createdOn)
    }

    @Test
    fun `archive page keeps the page number`() = runTest {
        dataSource.firmwares = SpecialistResponse.Success(listOf(firmware("3.61"), firmware("3.60")))

        val page = assertIs<Response.Success<FirmwareVersionPage>>(
            repository.archivePage(7, FirmwareChannel.RELEASE, 2, 20, null),
        ).value

        assertEquals(2, page.pageNumber)
        assertEquals(listOf("3.61", "3.60"), page.items.map { it.version })
        assertEquals(
            SubsystemFirmwaresRequest(7, SubsystemFirmwareType.RELEASE, pageNumber = 2, pageSize = 20),
            dataSource.requests.single(),
        )
    }

    @Test
    fun `systems are the whole device base without systems lacking subsystems`() = runTest {
        dataSource.devices = SpecialistResponse.Success(listOf(device(MAIN), device()))

        val systems = assertIs<Response.Success<List<FirmwareSystem>>>(repository.systems()).value

        assertEquals(
            listOf(
                FirmwareSystem(
                    1,
                    "DX-9X",
                    listOf(FirmwareSubsystem(7, "Pandora", "DX4G", FirmwareModuleKind.AlarmMain)),
                ),
            ),
            systems,
        )
        val request = dataSource.listRequests.single()
        assertNull(request.name)
        assertEquals(0, request.pageNumber)
        assertEquals(10_000, request.pageSize)
    }

    @Test
    fun `download passes the url through`() = runTest {
        dataSource.file = SpecialistResponse.Success(byteArrayOf(1, 2))

        val bytes = assertIs<Response.Success<ByteArray>>(repository.download("https://example.com/fw.bin")).value

        assertContentEquals(byteArrayOf(1, 2), bytes)
        assertEquals(DownloadFileRequest("https://example.com/fw.bin"), dataSource.requests.single())
    }

    @Test
    fun `sdk error and unavailable interactor are errors`() = runTest {
        val failure = IllegalStateException("db")
        dataSource.information = SpecialistResponse.Error.General(failure)
        assertEquals(failure, assertIs<Response.Error>(repository.modules("DX-9X", ConnectionTransport.BLE)).exception)

        assertIs<Response.Error>(repository.systems())
        assertIs<Response.Error>(repository.latest(7, FirmwareChannel.RELEASE, null))
        assertIs<Response.Error>(repository.download("https://example.com/fw.bin"))
    }

    private fun device(vararg subsystems: SubsystemInformation) =
        DeviceInformation(1, "DX-9X", DeviceType.ALARM, subsystems.toList(), null)

    private fun firmware(version: String) = SubsystemFirmware(
        id = 3,
        type = SubsystemFirmwareType.RELEASE,
        version = version,
        creationTime = LocalDateTime(2026, 10, 14, 23, 50),
        description = "notes",
        url = "https://example.com/fw.bin",
        canCode = null,
        canVersion = null,
        canVersionString = null,
    )

    private companion object {
        val MAIN = SubsystemInformation("DX4G", "Pandora", 7, SubsystemType.ALARM_MAIN)
        val BLUETOOTH = SubsystemInformation("BT", "Bluetooth", 8, SubsystemType.BLUETOOTH_MODULE)
    }
}
