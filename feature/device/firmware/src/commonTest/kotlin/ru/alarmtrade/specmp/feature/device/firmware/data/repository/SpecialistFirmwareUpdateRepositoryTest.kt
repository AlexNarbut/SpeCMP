package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.DataWritingError
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateState
import ru.alarmtrade.devices.domain.alarmDevice.firmware.FirmwareUpdateStatus
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.WriteFailureReason
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

class SpecialistFirmwareUpdateRepositoryTest {
    private val dataSource = FakeFirmwareUpdateDataSource()
    private val repository = SpecialistFirmwareUpdateRepository(dataSource)

    @Test
    fun `update goes through all stages to finished`() = runTest {
        dataSource.update = flowOf(
            state(FirmwareUpdateStatus.SENDING_DATA, progress = 50),
            state(FirmwareUpdateStatus.CHECKING_CRC),
            state(FirmwareUpdateStatus.APPLYING),
            state(FirmwareUpdateStatus.FINISHED_SUCCESS),
        )

        val stages = repository.update(BootModeTarget.MainProgram, VERSION).toList()

        assertEquals(
            listOf(
                FirmwareUpdateProgress.Preparing,
                FirmwareUpdateProgress.Sending(50),
                FirmwareUpdateProgress.Verifying,
                FirmwareUpdateProgress.Applying,
                FirmwareUpdateProgress.Finished,
            ).map { Response.Success(it) },
            stages,
        )
        assertEquals(listOf("main ${VERSION.url}"), dataSource.calls)
    }

    @Test
    fun `bluetooth module from file uses its own sdk flow and ends on applying without failure`() = runTest {
        dataSource.update = flowOf(state(FirmwareUpdateStatus.APPLYING))

        val stages = repository.updateFromFile(BootModeTarget.BluetoothModule, "/tmp/ble.bin").toList()

        assertEquals(
            listOf(FirmwareUpdateProgress.Preparing, FirmwareUpdateProgress.Applying).map { Response.Success(it) },
            stages,
        )
        assertEquals(listOf("bluetooth /tmp/ble.bin"), dataSource.calls)
    }

    @Test
    fun `error before the first stage is a preparation failure`() = runTest {
        val cause = IllegalStateException("download")
        dataSource.update = flowOf(SpecialistResponse.Error.General(cause))

        val failure = repository.update(BootModeTarget.MainProgram, VERSION).toList().last().failure()

        assertEquals(FirmwareFailure.Preparation, failure.failure)
        assertSame(cause, failure.cause)
    }

    @Test
    fun `error in the middle ends the flow with an unknown failure`() = runTest {
        dataSource.update = flowOf(
            state(FirmwareUpdateStatus.SENDING_DATA, progress = 10),
            SpecialistResponse.Error.General(IllegalStateException("link lost")),
            state(FirmwareUpdateStatus.FINISHED_SUCCESS),
        )

        val stages = repository.update(BootModeTarget.MainProgram, VERSION).toList()

        assertEquals(3, stages.size)
        assertEquals(FirmwareFailure.Unknown, stages.last().failure().failure)
    }

    @Test
    fun `block write error ends the flow with its reason`() = runTest {
        dataSource.update = flowOf(
            state(FirmwareUpdateStatus.SENDING_DATA, progress = 10),
            state(FirmwareUpdateStatus.FINISHED_ERROR, error = DataWritingError.CRC),
            state(FirmwareUpdateStatus.FINISHED_SUCCESS),
        )

        val stages = repository.update(BootModeTarget.MainProgram, VERSION).toList()

        assertEquals(3, stages.size)
        assertEquals(FirmwareFailure.Write(WriteFailureReason.Crc), stages.last().failure().failure)
    }

    @Test
    fun `flow ended before the result or broken is an unknown failure`() = runTest {
        dataSource.update = flowOf(state(FirmwareUpdateStatus.SENDING_DATA, progress = 90))
        val ended = repository.update(BootModeTarget.MainProgram, VERSION).toList()
        assertEquals(FirmwareFailure.Unknown, ended.last().failure().failure)

        dataSource.update = flow {
            emit(state(FirmwareUpdateStatus.SENDING_DATA, progress = 10))
            error("parse")
        }
        val stages = repository.update(BootModeTarget.MainProgram, VERSION).toList()
        assertEquals(3, stages.size)
        assertEquals(FirmwareFailure.Unknown, stages.last().failure().failure)
    }

    @Test
    fun `unavailable interactor fails after preparing`() = runTest {
        dataSource.update = null

        val stages = repository.update(BootModeTarget.MainProgram, VERSION).toList()

        assertEquals(Response.Success(FirmwareUpdateProgress.Preparing), stages.first())
        assertEquals(FirmwareFailure.Unknown, stages.single { it is Response.Error }.failure().failure)
    }

    private fun Response<FirmwareUpdateProgress>.failure(): FirmwareUpdateException =
        assertIs<FirmwareUpdateException>(assertIs<Response.Error>(this).exception)

    private fun state(status: FirmwareUpdateStatus, progress: Int? = null, error: DataWritingError? = null) =
        SpecialistResponse.Success(FirmwareUpdateState(status, stateProgress = progress, error = error))

    private companion object {
        val VERSION = FirmwareVersion(
            id = 3,
            version = "3.61",
            createdOn = LocalDate(2026, 10, 14),
            description = "notes",
            url = "https://example.com/fw.bin",
        )
    }
}
