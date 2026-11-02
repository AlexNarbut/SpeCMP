package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specialist.interactors.bluetoothAlarmDevice.BluetoothAlarmDeviceInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository

/** [ProgramModeRepository] над `setProgramModeUseCase` / `exitProgramModeUseCase`: неверный PIN — исход, не ошибка. */
class SpecialistProgramModeRepository(
    private val bluetoothAlarmDeviceInteractor: () -> BluetoothAlarmDeviceInteractor
) : ProgramModeRepository {

    override suspend fun enter(): Response<ProgramModeResult> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return interactorUnavailable()
        return when (val response = interactor.setProgramModeUseCase.execute().toResponse()) {
            is Response.Success -> Response.Success(ProgramModeResult.ENTERED)

            is Response.Error ->
                if (response.exception is IncorrectServicePinException) {
                    Response.Success(ProgramModeResult.WRONG_PIN)
                } else {
                    response
                }
        }
    }

    override suspend fun exit(): Response<Unit> {
        val interactor = bluetoothAlarmDeviceInteractor.resolveOrNull() ?: return interactorUnavailable()
        return interactor.exitProgramModeUseCase.execute().toResponse()
    }
}
