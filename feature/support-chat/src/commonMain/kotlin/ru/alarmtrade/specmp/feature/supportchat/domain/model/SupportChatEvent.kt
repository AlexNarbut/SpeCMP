package ru.alarmtrade.specmp.feature.supportchat.domain.model

sealed class SupportChatEvent {
    data class ConnectionChanged(val isConnected: Boolean) : SupportChatEvent()

    /**
     * @param isMine собственное событие набора (SDK эхит наш же Writing обратно) —
     * VM его игнорирует, чтобы не рисовать «собеседник печатает» за себя.
     */
    data class TypingChanged(val authorEmail: String?, val isTyping: Boolean, val isMine: Boolean) : SupportChatEvent()

    data class MessageReceived(val message: SupportChatMessage) : SupportChatEvent()
    data class MessagesRead(val ids: List<Long>) : SupportChatEvent()
}
