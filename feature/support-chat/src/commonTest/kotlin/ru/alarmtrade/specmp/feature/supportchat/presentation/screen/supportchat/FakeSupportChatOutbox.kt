package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatOutbox

class FakeSupportChatOutbox : SupportChatOutbox {

    // Порядок вставки = порядок очереди (как у реализации на Settings).
    val entries = linkedMapOf<String, SupportChatMessage>()
    var releaseCallCount = 0
        private set

    override suspend fun load(): Response<List<SupportChatMessage>> = Response.Success(entries.values.toList())

    override suspend fun enqueue(message: SupportChatMessage): Response<Unit> {
        entries[message.id] = message
        return Response.Success(Unit)
    }

    override suspend fun markSent(clientId: String): Response<Unit> {
        entries[clientId]?.let { entries[clientId] = it.copy(status = SupportChatMessageStatus.Sent) }
        return Response.Success(Unit)
    }

    override suspend fun remove(clientId: String): Response<Unit> {
        entries.remove(clientId)
        return Response.Success(Unit)
    }

    override suspend fun release() {
        entries.clear()
        releaseCallCount++
    }
}
