package ru.alarmtrade.specmp.feature.device.autostart.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureException
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.CloneProcedureFailure
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.CloneRepository

/** Clone: события процедуры шлёт тест ([events]); каждый запуск — новый канал. */
internal class FakeCloneRepository : CloneRepository {
    var attempts: Response<Int?> = Response.Success(3)
    var attemptsCalls = 0
    var runs = 0
    var events = Channel<Response<CloneEvent>>(Channel.UNLIMITED)
        private set

    override suspend fun attempts(): Response<Int?> {
        attemptsCalls++
        return attempts
    }

    override fun run(): Flow<Response<CloneEvent>> {
        runs++
        events = Channel(Channel.UNLIMITED)
        return events.receiveAsFlow()
    }

    fun send(vararg event: CloneEvent) = event.forEach { events.trySend(Response.Success(it)) }

    fun fail(failure: CloneProcedureFailure) = events.trySend(Response.Error(CloneProcedureException(failure)))

    fun finish() = events.close()
}
