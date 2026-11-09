package ru.alarmtrade.specmp.feature.device.interaction.data.repository

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.devices.domain.alarmDevice.commands.SetProgramModeResponse
import ru.alarmtrade.specmp.feature.device.interaction.data.datasource.ProgramModeDataSource

internal class FakeProgramModeDataSource : ProgramModeDataSource {
    var enter: SpecialistResponse<SetProgramModeResponse>? = SpecialistResponse.Success(SetProgramModeResponse.SUCCESS)
    var exit: SpecialistResponse<Unit>? = SpecialistResponse.Success(Unit)

    override suspend fun setProgramMode(): SpecialistResponse<SetProgramModeResponse>? = enter

    override suspend fun exitProgramMode(): SpecialistResponse<Unit>? = exit
}
