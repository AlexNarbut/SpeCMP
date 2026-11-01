package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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

    override fun update(fileName: String, path: String): Flow<SerialFirmwareProgress> = flow {
        val interactor = alarmDevice.resolveOrNull()
        if (interactor == null) {
            emit(SerialFirmwareProgress.Failed(SerialFirmwareFailure.Unknown))
            return@flow
        }
        val data = safeResponseRun { Response.Success(readFileBytes(path)) }
        if (data !is Response.Success) {
            emit(SerialFirmwareProgress.Failed(SerialFirmwareFailure.File))
            return@flow
        }
        val request = UpdateSerialFirmwareRequest(fileName, data.value, BOOT_ENTRY_TIMEOUT_MILLIS)
        var last: SerialFirmwareProgress? = null
        safeFlowRun {
            interactor.updateSerialDeviceFirmwareFlowUseCase.execute(request).map { it.toResponse() }
        }.collect { response ->
            val progress = when (response) {
                is Response.Success -> response.value.toProgress()
                is Response.Error -> SerialFirmwareProgress.Failed(response.exception.toFailure())
            }
            last = progress
            emit(progress)
        }
        if (last !is SerialFirmwareProgress.Finished && last !is SerialFirmwareProgress.Failed) {
            emit(SerialFirmwareProgress.Failed(SerialFirmwareFailure.Unknown))
        }
    }

    private companion object {
        /** Сколько SDK шлёт GOBOOT: пользователь успевает перезагрузить блок по питанию (дефолт SDK 10 с — мало). */
        const val BOOT_ENTRY_TIMEOUT_MILLIS = 30_000L
    }
}

internal fun SerialFirmwareUpdateState.toProgress(): SerialFirmwareProgress = when (status) {
    SerialFirmwareUpdateStatus.WAITING_FOR_BOOT_MODE -> SerialFirmwareProgress.WaitingForBoot

    SerialFirmwareUpdateStatus.CHECKING_BOOT_MODE -> SerialFirmwareProgress.CheckingBoot

    SerialFirmwareUpdateStatus.SENDING_DATA -> SerialFirmwareProgress.Sending(stateProgress)

    SerialFirmwareUpdateStatus.CHECKING_CRC -> SerialFirmwareProgress.Verifying

    SerialFirmwareUpdateStatus.APPLYING -> SerialFirmwareProgress.Applying

    SerialFirmwareUpdateStatus.FINISHED_SUCCESS -> SerialFirmwareProgress.Finished

    SerialFirmwareUpdateStatus.FINISHED_ERROR -> SerialFirmwareProgress.Failed(
        when (error) {
            SerialFirmwareUpdateError.TRANSFER -> SerialFirmwareFailure.Transfer
            SerialFirmwareUpdateError.CRC -> SerialFirmwareFailure.Crc
            SerialFirmwareUpdateError.APPLYING -> SerialFirmwareFailure.Applying
            SerialFirmwareUpdateError.CONNECTION_LOST -> SerialFirmwareFailure.ConnectionLost
            null -> SerialFirmwareFailure.Unknown
        }
    )
}

/** Ошибки до передачи SDK отдаёт исключениями (см. `SerialFirmwareUpdateError`). */
internal fun Throwable.toFailure(): SerialFirmwareFailure = when (this) {
    is FrmFileParsingException -> SerialFirmwareFailure.File
    is BootModeEntryTimeoutError, is PcBaseBootModeEntryTimeoutException -> SerialFirmwareFailure.BootTimeout
    is DeviceNotInBootModeError -> SerialFirmwareFailure.NotInBoot
    else -> SerialFirmwareFailure.Unknown
}
