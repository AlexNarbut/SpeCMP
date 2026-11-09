package ru.alarmtrade.specmp.feature.device.interaction.testing

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ProgramModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.ProgramModeRepository

/** Ответы [enter] — [results] по порядку, последний повторяется. */
class FakeProgramModeRepository : ProgramModeRepository {
    var results: List<Response<ProgramModeResult>> = listOf(Response.Success(ProgramModeResult.ENTERED))
    var calls = 0
        private set
    var exits = 0
        private set

    override suspend fun enter(): Response<ProgramModeResult> = results[minOf(calls++, results.lastIndex)]

    override suspend fun exit(): Response<Unit> {
        exits++
        return Response.Success(Unit)
    }
}
