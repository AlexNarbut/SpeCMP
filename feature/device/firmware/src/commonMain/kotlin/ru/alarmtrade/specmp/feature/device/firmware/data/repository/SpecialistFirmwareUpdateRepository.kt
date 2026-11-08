package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateFirmwareRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

/**
 * [FirmwareUpdateRepository] над `bluetoothAlarmDeviceInteractor`: основная программа и Bluetooth-модуль обновляются
 * разными потоками SDK. Файл по `url` SDK скачивает сам — это стадия [FirmwareUpdateProgress.Preparing].
 */
class SpecialistFirmwareUpdateRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor,
) : FirmwareUpdateRepository {

    override fun update(target: BootModeTarget, firmware: FirmwareVersion): Flow<FirmwareUpdateProgress> =
        run(target, firmware.url)

    override fun updateFromFile(target: BootModeTarget, path: String): Flow<FirmwareUpdateProgress> = run(target, path)

    private fun run(target: BootModeTarget, filePathOrUrl: String): Flow<FirmwareUpdateProgress> = flow {
        emit(FirmwareUpdateProgress.Preparing)
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull()
        if (interactor == null) {
            emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Unknown))
            return@flow
        }
        val request = UpdateFirmwareRequest(filePathOrUrl)
        var last: FirmwareUpdateProgress = FirmwareUpdateProgress.Preparing
        safeFlowRun {
            val source = when (target) {
                BootModeTarget.MainProgram -> interactor.updateAlarmDeviceFirmwareFlowUseCase.execute(request)
                BootModeTarget.BluetoothModule -> interactor.updateBluetoothFirmwareFlowUseCase.execute(request)
            }
            source.map { it.toResponse() }
        }.collect { response ->
            last = when (response) {
                is Response.Success -> response.value.toProgress()

                // Ошибка до первой стадии — не дошли до блока (скачивание/разбор файла); позже — неизвестный сбой.
                is Response.Error -> FirmwareUpdateProgress.Failed(
                    if (last ==
                        FirmwareUpdateProgress.Preparing
                    ) {
                        FirmwareFailure.Preparation
                    } else {
                        FirmwareFailure.Unknown
                    },
                )
            }
            emit(last)
        }
        // Поток закончился без результата: на «Применении» это перезагрузка блока (итог — возврат блока), иначе сбой.
        if (last !is FirmwareUpdateProgress.Finished && last !is FirmwareUpdateProgress.Failed &&
            last != FirmwareUpdateProgress.Applying
        ) {
            emit(FirmwareUpdateProgress.Failed(FirmwareFailure.Unknown))
        }
    }
}
