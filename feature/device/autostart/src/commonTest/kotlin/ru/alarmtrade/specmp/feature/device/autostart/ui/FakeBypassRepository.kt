package ru.alarmtrade.specmp.feature.device.autostart.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassEvent
import ru.alarmtrade.specmp.feature.device.autostart.domain.model.BypassLearningException
import ru.alarmtrade.specmp.feature.device.autostart.domain.repository.BypassLearningRepository

/** Обход: шаги шлёт тест ([events]); каждый запуск — новый канал. */
internal class FakeBypassRepository : BypassLearningRepository {
    var runs = 0
    var events = Channel<Response<BypassEvent>>(Channel.UNLIMITED)
        private set

    override fun learn(): Flow<Response<BypassEvent>> {
        runs++
        events = Channel(Channel.UNLIMITED)
        return events.receiveAsFlow()
    }

    fun send(vararg event: BypassEvent) = event.forEach { events.trySend(Response.Success(it)) }

    fun fail() = events.trySend(Response.Error(BypassLearningException()))
}
