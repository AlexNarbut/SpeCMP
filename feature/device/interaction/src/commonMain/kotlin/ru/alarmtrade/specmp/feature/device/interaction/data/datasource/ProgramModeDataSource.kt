package ru.alarmtrade.specmp.feature.device.interaction.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.SetProgramModeResponse
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface ProgramModeDataSource {
    suspend fun setProgramMode(): SpecialistResponse<SetProgramModeResponse>?
    suspend fun exitProgramMode(): SpecialistResponse<Unit>?
}

internal class SpecialistProgramModeDataSource(private val alarmDevice: () -> BluetoothAlarmDeviceInteractor) :
    ProgramModeDataSource {
    override suspend fun setProgramMode(): SpecialistResponse<SetProgramModeResponse>? =
        alarmDevice.resolveOrNull()?.setProgramModeUseCase?.execute()

    override suspend fun exitProgramMode(): SpecialistResponse<Unit>? =
        alarmDevice.resolveOrNull()?.exitProgramModeUseCase?.execute()
}
