package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import ru.alarmtrade.specialist.domain.errors.IncorrectServicePinException
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.ProgramModeDataSource
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository

/** [ProgramModeRepository]: неверный PIN — исход, не ошибка. */
internal class SpecialistProgramModeRepository(private val dataSource: ProgramModeDataSource) : ProgramModeRepository {

    override suspend fun enter(): Response<ProgramModeResult> =
        when (val response = dataSource.setProgramMode().toResponseOrUnavailable()) {
            is Response.Success -> Response.Success(ProgramModeResult.ENTERED)

            is Response.Error ->
                if (response.exception is IncorrectServicePinException) {
                    Response.Success(ProgramModeResult.WRONG_PIN)
                } else {
                    response
                }
        }

    override suspend fun exit(): Response<Unit> = dataSource.exitProgramMode().toResponseOrUnavailable()
}
