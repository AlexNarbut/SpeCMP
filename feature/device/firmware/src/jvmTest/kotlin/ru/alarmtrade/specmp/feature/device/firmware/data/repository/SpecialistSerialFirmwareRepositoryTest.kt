package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.errors.DeviceNotInBootModeError
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateError
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateState
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateStatus
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress

class SpecialistSerialFirmwareRepositoryTest {
    private val dataSource = FakeFirmwareUpdateDataSource()
    private val repository = SpecialistSerialFirmwareRepository(dataSource)
    private val file = File.createTempFile("firmware", ".frm").apply { writeBytes(byteArrayOf(1, 2, 3)) }

    @AfterTest
    fun deleteFile() {
        file.delete()
    }

    @Test
    fun `update sends the file and goes through all stages to finished`() = runTest {
        dataSource.serial = flowOf(
            state(SerialFirmwareUpdateStatus.WAITING_FOR_BOOT_MODE),
            state(SerialFirmwareUpdateStatus.CHECKING_BOOT_MODE),
            state(SerialFirmwareUpdateStatus.SENDING_DATA, progress = 40),
            state(SerialFirmwareUpdateStatus.CHECKING_CRC),
            state(SerialFirmwareUpdateStatus.APPLYING),
            state(SerialFirmwareUpdateStatus.FINISHED_SUCCESS),
        )

        val stages = update()

        assertEquals(
            listOf(
                SerialFirmwareProgress.WaitingForBoot,
                SerialFirmwareProgress.CheckingBoot,
                SerialFirmwareProgress.Sending(40),
                SerialFirmwareProgress.Verifying,
                SerialFirmwareProgress.Applying,
                SerialFirmwareProgress.Finished,
            ).map { Response.Success(it) },
            stages,
        )
        val request = dataSource.serialRequests.single()
        assertEquals("DX4G.frm", request.fileName)
        assertContentEquals(byteArrayOf(1, 2, 3), request.data)
    }

    @Test
    fun `sdk exception in the middle ends the flow with its failure`() = runTest {
        val cause = DeviceNotInBootModeError()
        dataSource.serial = flowOf(
            state(SerialFirmwareUpdateStatus.WAITING_FOR_BOOT_MODE),
            SpecialistResponse.Error.General(cause),
            state(SerialFirmwareUpdateStatus.FINISHED_SUCCESS),
        )

        val stages = update()

        assertEquals(2, stages.size)
        val error = stages.last().failure()
        assertEquals(SerialFirmwareFailure.NotInBoot, error.failure)
        assertSame(cause, error.cause)
    }

    @Test
    fun `transfer error of the block ends the flow`() = runTest {
        dataSource.serial = flowOf(
            state(SerialFirmwareUpdateStatus.SENDING_DATA, progress = 10),
            state(SerialFirmwareUpdateStatus.FINISHED_ERROR, error = SerialFirmwareUpdateError.CRC),
            state(SerialFirmwareUpdateStatus.FINISHED_SUCCESS),
        )

        val stages = update()

        assertEquals(2, stages.size)
        assertEquals(SerialFirmwareFailure.Crc, stages.last().failure().failure)
    }

    @Test
    fun `flow ended before finished or broken is an unknown failure`() = runTest {
        dataSource.serial = flowOf(state(SerialFirmwareUpdateStatus.APPLYING))
        assertEquals(SerialFirmwareFailure.Unknown, update().last().failure().failure)

        dataSource.serial = flow { throw IllegalStateException("port closed") }
        assertEquals(SerialFirmwareFailure.Unknown, update().single().failure().failure)
    }

    @Test
    fun `unreadable file fails without starting the sdk`() = runTest {
        val stages = repository.update("DX4G.frm", file.path + ".missing").toList()

        assertEquals(SerialFirmwareFailure.File, stages.single().failure().failure)
        assertEquals(emptyList(), dataSource.serialRequests)
    }

    @Test
    fun `unavailable interactor is an unknown failure`() = runTest {
        dataSource.serial = null

        assertEquals(SerialFirmwareFailure.Unknown, update().single().failure().failure)
    }

    @Test
    fun `not a frm file and a missing file are errors`() = runTest {
        assertIs<Response.Error>(repository.readFileInfo(file.path))
        assertIs<Response.Error>(repository.readFileInfo(file.path + ".missing"))
    }

    private suspend fun update(): List<Response<SerialFirmwareProgress>> =
        repository.update("DX4G.frm", file.path).toList()

    private fun Response<SerialFirmwareProgress>.failure(): SerialFirmwareException =
        assertIs<SerialFirmwareException>(assertIs<Response.Error>(this).exception)

    private fun state(
        status: SerialFirmwareUpdateStatus,
        progress: Int? = null,
        error: SerialFirmwareUpdateError? = null,
    ) = SpecialistResponse.Success(SerialFirmwareUpdateState(status, progress, error, null))
}
