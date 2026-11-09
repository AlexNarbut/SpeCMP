package ru.alarmtrade.specmp.feature.supportchat.domain.model

/** Событие чат-сессии; сессию не удалось начать — `Response.Error` с [SupportChatUnavailableException]. */
sealed class SupportChatEvent {

    /** Изменение состояния соединения. */
    data class Connection(val state: SupportChatConnectionState) : SupportChatEvent()

    /**
     * @param isMine собственное событие набора (SDK эхит наш же Writing обратно) —
     * VM его игнорирует, чтобы не рисовать «собеседник печатает» за себя.
     */
    data class TypingChanged(val authorEmail: String?, val isTyping: Boolean, val isMine: Boolean) : SupportChatEvent()

    data class MessageReceived(val message: SupportChatMessage) : SupportChatEvent()
    data class MessagesRead(val ids: List<Long>) : SupportChatEvent()
}

/** Сессию не удалось начать (не получен профиль). Финал: поток `session()` завершается. */
class SupportChatUnavailableException(cause: Throwable? = null) : Exception("Support chat session unavailable", cause)
