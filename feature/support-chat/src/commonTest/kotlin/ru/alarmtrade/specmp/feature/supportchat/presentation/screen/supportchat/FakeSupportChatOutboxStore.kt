package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SupportChatOutboxEntry
import ru.alarmtrade.specmp.feature.supportchat.data.outbox.SupportChatOutboxStore

class FakeSupportChatOutboxStore : SupportChatOutboxStore {

    var entries: List<SupportChatOutboxEntry> = emptyList()
    var clearCallCount = 0
        private set

    override fun load(): List<SupportChatOutboxEntry> = entries

    override fun save(entries: List<SupportChatOutboxEntry>) {
        this.entries = entries
    }

    override fun clear() {
        entries = emptyList()
        clearCallCount++
    }
}
