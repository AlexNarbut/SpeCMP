package ru.alarmtrade.specmp.feature.device.firmware.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareFailure
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateException
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareUpdateProgress
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareVersion
import ru.alarmtrade.specmp.feature.device.firmware.domain.repository.FirmwareUpdateRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

/** Процесс обновления из тестов: шаги подаются в [progress]; параметры запуска запоминаются. */
internal class FakeUpdateRepository : FirmwareUpdateRepository {
    val progress = MutableSharedFlow<Response<FirmwareUpdateProgress>>(extraBufferCapacity = 16)
    val calls = mutableListOf<Pair<BootModeTarget, FirmwareVersion>>()

    override fun update(target: BootModeTarget, firmware: FirmwareVersion): Flow<Response<FirmwareUpdateProgress>> {
        calls += target to firmware
        return progress
    }

    val fileCalls = mutableListOf<Pair<BootModeTarget, String>>()

    override fun updateFromFile(target: BootModeTarget, path: String): Flow<Response<FirmwareUpdateProgress>> {
        fileCalls += target to path
        return progress
    }

    suspend fun emit(step: FirmwareUpdateProgress) = progress.emit(Response.Success(step))

    suspend fun fail(failure: FirmwareFailure) = progress.emit(Response.Error(FirmwareUpdateException(failure)))
}
