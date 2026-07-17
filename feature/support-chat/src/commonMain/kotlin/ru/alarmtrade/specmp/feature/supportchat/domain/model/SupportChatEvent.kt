package ru.alarmtrade.specmp.feature.supportchat.domain.model

sealed class SupportChatEvent {
    data class ConnectionChanged(val isConnected: Boolean) : SupportChatEvent()
    data class TypingChanged(val authorEmail: String?, val isTyping: Boolean) : SupportChatEvent()
    data class MessageReceived(val message: SupportChatMessage) : SupportChatEvent()
    data class HistoryPageReceived(val messages: List<SupportChatMessage>) : SupportChatEvent()
    data class MessagesRead(val ids: List<Long>) : SupportChatEvent()
}
