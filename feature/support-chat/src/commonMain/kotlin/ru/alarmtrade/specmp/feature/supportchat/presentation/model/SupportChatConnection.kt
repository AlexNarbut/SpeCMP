package ru.alarmtrade.specmp.feature.supportchat.presentation.model

/** Ввод только в [Online]: до открытия сессии сообщение ушло бы в незакрытое соединение. */
enum class SupportChatConnection {
    Connecting,
    Online,
    Offline,
}

/** [Reconnecting] — одна плашка на переподключение и ожидание сети, без мигания. */
enum class SupportChatConnectionBanner {
    Connecting,
    Reconnecting,
    AuthFailed,
}
