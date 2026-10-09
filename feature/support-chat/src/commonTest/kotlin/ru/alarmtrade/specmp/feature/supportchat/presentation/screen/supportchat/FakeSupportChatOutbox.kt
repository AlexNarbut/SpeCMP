package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatOutbox

class FakeSupportChatOutbox : SupportChatOutbox {

    // Порядок вставки = порядок очереди (как у реализации на Settings).
    val entries = linkedMapOf<String, SupportChatMessage>()
    var releaseCallCount = 0
        private set

    override suspend fun load(): List<SupportChatMessage> = entries.values.toList()

    override suspend fun enqueue(message: SupportChatMessage) {
        entries[message.id] = message
    }

    override suspend fun markSent(clientId: String) {
        entries[clientId]?.let { entries[clientId] = it.copy(status = SupportChatMessageStatus.Sent) }
    }

    override suspend fun remove(clientId: String) {
        entries.remove(clientId)
    }

    override suspend fun release() {
        entries.clear()
        releaseCallCount++
    }
}
