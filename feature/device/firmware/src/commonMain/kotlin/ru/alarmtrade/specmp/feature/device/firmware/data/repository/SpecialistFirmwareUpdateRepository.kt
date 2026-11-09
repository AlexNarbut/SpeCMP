package ru.alarmtrade.specmp.feature.device.firmware.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformWhile
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.requests.UpdateFirmwareRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeFlowRun
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.firmware.data.datasource.FirmwareUpdateDataSource
import ru.alarmtrade.specmp.feature.device.firmware.data.mapper.toProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

/**
 * [FirmwareUpdateRepository]: основная программа и Bluetooth-модуль обновляются разными потоками SDK. Файл по `url`
 * SDK скачивает сам — это стадия [FirmwareUpdateProgress.Preparing].
 */
internal class SpecialistFirmwareUpdateRepository(private val dataSource: FirmwareUpdateDataSource) :
    FirmwareUpdateRepository {

    override fun update(target: BootModeTarget, firmware: FirmwareVersion): Flow<Response<FirmwareUpdateProgress>> =
        run(target, firmware.url)

    override fun updateFromFile(target: BootModeTarget, path: String): Flow<Response<FirmwareUpdateProgress>> =
        run(target, path)

    private fun run(target: BootModeTarget, filePathOrUrl: String): Flow<Response<FirmwareUpdateProgress>> = flow {
        emit(Response.Success(FirmwareUpdateProgress.Preparing))
        val request = UpdateFirmwareRequest(filePathOrUrl)
        val source = when (target) {
            BootModeTarget.MainProgram -> dataSource.updateMainProgram(request)
            BootModeTarget.BluetoothModule -> dataSource.updateBluetoothModule(request)
        }
        if (source == null) {
            emit(failure(FirmwareFailure.Unknown))
            return@flow
        }
        var last: Response<FirmwareUpdateProgress> = Response.Success(FirmwareUpdateProgress.Preparing)
        val steps = safeFlowRun { source.map { it.toResponse() } }.transformWhile { response ->
            last = when (response) {
                is Response.Success -> response.value.toProgress()

                // Ошибка до первой стадии — не дошли до блока (скачивание/разбор файла); позже — неизвестный сбой.
                is Response.Error -> failure(
                    if (last == Response.Success(FirmwareUpdateProgress.Preparing)) {
                        FirmwareFailure.Preparation
                    } else {
                        FirmwareFailure.Unknown
                    },
                    response.exception,
                )
            }
            emit(last)
            last is Response.Success
        }
        emitAll(steps)
        // Поток закончился без результата: на «Применении» это перезагрузка блока (итог — возврат блока), иначе сбой.
        val stage = (last as? Response.Success)?.value ?: return@flow
        if (stage != FirmwareUpdateProgress.Finished && stage != FirmwareUpdateProgress.Applying) {
            emit(failure(FirmwareFailure.Unknown))
        }
    }

    private fun failure(failure: FirmwareFailure, cause: Throwable? = null): Response<FirmwareUpdateProgress> =
        Response.Error(FirmwareUpdateException(failure, cause))
}
