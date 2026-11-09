package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.alarmtrade.alarmCpu.pcbaseProtocol.exception.PcBaseBootModeEntryTimeoutException
import ru.alarmtrade.devices.domain.errors.BootModeEntryTimeoutError
import ru.alarmtrade.devices.domain.errors.DeviceNotInBootModeError
import ru.alarmtrade.devices.domain.errors.FrmFileParsingException
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateError
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateState
import ru.alarmtrade.specialist.domain.alarmDevice.firmware.SerialFirmwareUpdateStatus
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateSerialFirmwareRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.common.safeResponseRun
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.firmware.data.file.parseFrmFileInfo
import ru.alarmtrade.specmp.feature.device.firmware.data.file.readFileBytes
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FrmFileInfo
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.SerialFirmwareProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.SerialFirmwareRepository

/**
 * [SerialFirmwareRepository] над `bluetoothAlarmDeviceInteractor.updateSerialDeviceFirmwareFlowUseCase` (обновление
 * `.frm` в текущей serial-сессии SDK). Интерактор — лениво: на iOS COM-порт в SDK не реализован.
 */
class SpecialistSerialFirmwareRepository(private val alarmDevice: () -> BluetoothAlarmDeviceInteractor) :
    SerialFirmwareRepository {

    override suspend fun readFileInfo(path: String): Response<FrmFileInfo> = safeResponseRun {
        parseFrmFileInfo(readFileBytes(path))?.let { Response.Success(it) }
            ?: Response.Error(IllegalArgumentException("Not a .frm file"))
    }

    override fun update(fileName: String, path: String): Flow<Response<SerialFirmwareProgress>> = flow {
        val interactor = alarmDevice.resolveOrNull()
        if (interactor == null) {
            emit(failure(SerialFirmwareFailure.Unknown))
            return@flow
        }
        val data = when (val read = safeResponseRun { Response.Success(readFileBytes(path)) }) {
            is Response.Success -> read.value

            is Response.Error -> {
                emit(failure(SerialFirmwareFailure.File, read.exception))
                return@flow
            }
        }
        val request = UpdateSerialFirmwareRequest(fileName, data, BOOT_ENTRY_TIMEOUT_MILLIS)
        var last: Response<SerialFirmwareProgress>? = null
        val steps = safeFlowRun {
            interactor.updateSerialDeviceFirmwareFlowUseCase.execute(request).map { it.toResponse() }
        }.transformWhile { response ->
            val step = when (response) {
                is Response.Success -> response.value.toProgress()
                is Response.Error -> failure(response.exception.toFailure(), response.exception)
            }
            last = step
            emit(step)
            step is Response.Success
        }
        emitAll(steps)
        val ended = last
        if (ended !is Response.Error && ended != Response.Success(SerialFirmwareProgress.Finished)) {
            emit(failure(SerialFirmwareFailure.Unknown))
        }
    }

    private fun failure(failure: SerialFirmwareFailure, cause: Throwable? = null): Response<SerialFirmwareProgress> =
        Response.Error(SerialFirmwareException(failure, cause))

    private companion object {
        /** Сколько SDK шлёт GOBOOT: пользователь успевает перезагрузить блок по питанию (дефолт SDK 10 с — мало). */
        const val BOOT_ENTRY_TIMEOUT_MILLIS = 30_000L
    }
}

internal fun SerialFirmwareUpdateState.toProgress(): Response<SerialFirmwareProgress> = when (status) {
    SerialFirmwareUpdateStatus.WAITING_FOR_BOOT_MODE -> Response.Success(SerialFirmwareProgress.WaitingForBoot)

    SerialFirmwareUpdateStatus.CHECKING_BOOT_MODE -> Response.Success(SerialFirmwareProgress.CheckingBoot)

    SerialFirmwareUpdateStatus.SENDING_DATA -> Response.Success(SerialFirmwareProgress.Sending(stateProgress))

    SerialFirmwareUpdateStatus.CHECKING_CRC -> Response.Success(SerialFirmwareProgress.Verifying)

    SerialFirmwareUpdateStatus.APPLYING -> Response.Success(SerialFirmwareProgress.Applying)

    SerialFirmwareUpdateStatus.FINISHED_SUCCESS -> Response.Success(SerialFirmwareProgress.Finished)

    SerialFirmwareUpdateStatus.FINISHED_ERROR -> Response.Error(
        SerialFirmwareException(
            when (error) {
                SerialFirmwareUpdateError.TRANSFER -> SerialFirmwareFailure.Transfer
                SerialFirmwareUpdateError.CRC -> SerialFirmwareFailure.Crc
                SerialFirmwareUpdateError.APPLYING -> SerialFirmwareFailure.Applying
                SerialFirmwareUpdateError.CONNECTION_LOST -> SerialFirmwareFailure.ConnectionLost
                null -> SerialFirmwareFailure.Unknown
            },
        ),
    )
}

/** Ошибки до передачи SDK отдаёт исключениями (см. `SerialFirmwareUpdateError`). */
internal fun Throwable.toFailure(): SerialFirmwareFailure = when (this) {
    is FrmFileParsingException -> SerialFirmwareFailure.File
    is BootModeEntryTimeoutError, is PcBaseBootModeEntryTimeoutException -> SerialFirmwareFailure.BootTimeout
    is DeviceNotInBootModeError -> SerialFirmwareFailure.NotInBoot
    else -> SerialFirmwareFailure.Unknown
}
