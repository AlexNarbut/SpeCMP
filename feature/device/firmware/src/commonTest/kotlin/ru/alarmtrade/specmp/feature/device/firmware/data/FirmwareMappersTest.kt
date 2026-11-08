package ru.alarmtrade.specmp.feature.device.firmware.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import ru.alarmtrade.devices.domain.alarmDevice.DataWritingError
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateState
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateStatus
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmware
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SubsystemFirmwareType
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemInformation
import ru.alarmtrade.specialist.domain.alarmDevice.information.SubsystemType
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.bootTarget
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.toDomain
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.toKind
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.toModule
import ru.alarmtrade.specmp.feature.device.firmware.data.repository.toProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.WriteFailureReason
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport

class FirmwareMappersTest {

    @Test
    fun `main program types enter the bootloader through the main command on both transports`() {
        listOf(
            SubsystemType.ALARM_MAIN,
            SubsystemType.WATCH_MAIN,
            SubsystemType.GPS_TRACKER_MAIN,
            SubsystemType.BMW_BYPASS_MAIN,
            SubsystemType.REMOTE_CONTROL_MAIN,
        ).forEach { type ->
            assertEquals(BootModeTarget.MainProgram, type.bootTarget(ConnectionTransport.BLE), "BLE $type")
            assertEquals(BootModeTarget.MainProgram, type.bootTarget(ConnectionTransport.USB), "USB $type")
        }
    }

    @Test
    fun `bluetooth module is updatable only over ble`() {
        assertEquals(
            BootModeTarget.BluetoothModule,
            SubsystemType.BLUETOOTH_MODULE.bootTarget(ConnectionTransport.BLE),
        )
        assertNull(SubsystemType.BLUETOOTH_MODULE.bootTarget(ConnectionTransport.USB))
    }

    @Test
    fun `types the app cannot put into the bootloader are not supported`() {
        listOf(
            SubsystemType.WATCH_BLUETOOTH,
            SubsystemType.FUEL_SENSOR,
            SubsystemType.CAN_STM32H563IIK,
            SubsystemType.MICROBOT_MAIN,
            SubsystemType.CHARGING_STATION_TESTER,
            SubsystemType.BATTERY_CHARGER_MAIN,
            SubsystemType.WATCH_INTERFACE,
            SubsystemType.UNKNOWN,
        ).forEach { type ->
            assertNull(type.bootTarget(ConnectionTransport.BLE), "BLE $type")
            assertNull(type.bootTarget(ConnectionTransport.USB), "USB $type")
        }
    }

    @Test
    fun `subsystem becomes a module`() {
        val module = SubsystemInformation("DX4G", "Pandora DX 4G", 7, SubsystemType.ALARM_MAIN)
            .toModule(ConnectionTransport.BLE)

        assertEquals(7, module.id)
        assertEquals(FirmwareModuleKind.AlarmMain, module.kind)
        assertEquals("DX4G", module.systemName)
        assertEquals("Pandora DX 4G", module.title)
        assertEquals(BootModeTarget.MainProgram, module.bootTarget)
        assertEquals(FirmwareModuleKind.CanModule, SubsystemType.CAN_STM32H563IIK.toKind())
    }

    @Test
    fun `server firmware keeps the date without time zone conversion`() {
        val firmware = SubsystemFirmware(
            id = 3,
            type = SubsystemFirmwareType.RELEASE,
            version = "3.61",
            creationTime = LocalDateTime(2026, 10, 14, 23, 50),
            description = "notes",
            url = "https://example.com/fw.bin",
            canCode = null,
            canVersion = null,
            canVersionString = null,
        ).toDomain()

        assertEquals(LocalDate(2026, 10, 14), firmware.createdOn)
        assertEquals("3.61", firmware.version)
        assertEquals("notes", firmware.description)
        assertEquals("https://example.com/fw.bin", firmware.url)
    }

    @Test
    fun `sdk update states map to stages`() {
        assertEquals(
            FirmwareUpdateProgress.Sending(64),
            FirmwareUpdateState(FirmwareUpdateStatus.SENDING_DATA, stateProgress = 64).toProgress(),
        )
        assertEquals(
            FirmwareUpdateProgress.Sending(null),
            FirmwareUpdateState(FirmwareUpdateStatus.SENDING_DATA).toProgress(),
        )
        assertEquals(
            FirmwareUpdateProgress.Verifying,
            FirmwareUpdateState(FirmwareUpdateStatus.CHECKING_CRC).toProgress(),
        )
        assertEquals(
            FirmwareUpdateProgress.Applying,
            FirmwareUpdateState(FirmwareUpdateStatus.APPLYING).toProgress(),
        )
        assertEquals(
            FirmwareUpdateProgress.Finished,
            FirmwareUpdateState(FirmwareUpdateStatus.FINISHED_SUCCESS).toProgress(),
        )
    }

    @Test
    fun `write errors keep their reason and a missing reason is unknown`() {
        val expected = mapOf(
            DataWritingError.CRC to WriteFailureReason.Crc,
            DataWritingError.TIMEOUT to WriteFailureReason.Timeout,
            DataWritingError.LOST_PACKET to WriteFailureReason.LostPacket,
            DataWritingError.WRONG_PACKET_LENGTH to WriteFailureReason.WrongPacketLength,
            DataWritingError.DEVICE_NOT_READY to WriteFailureReason.DeviceNotReady,
            DataWritingError.APPLYING_ERROR to WriteFailureReason.ApplyingError,
            DataWritingError.UNKNOWN to WriteFailureReason.Unknown,
        )
        expected.forEach { (error, reason) ->
            assertEquals(
                FirmwareUpdateProgress.Failed(FirmwareFailure.Write(reason)),
                FirmwareUpdateState(FirmwareUpdateStatus.FINISHED_ERROR, error = error).toProgress(),
            )
        }
        assertEquals(
            FirmwareUpdateProgress.Failed(FirmwareFailure.Write(WriteFailureReason.Unknown)),
            FirmwareUpdateState(FirmwareUpdateStatus.FINISHED_ERROR).toProgress(),
        )
    }
}
