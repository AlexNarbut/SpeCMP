package ru.alarmtrade.specmp.feature.supportchat.presentation.model

/**
 * Состояние соединения чата для UI.
 *
 * Ввод доступен только в [Online]: офлайн-отправка запрещена продуктово, а до
 * открытия сессии ([Connecting]) сообщение ушло бы в незакрытое соединение.
 */
enum class SupportChatConnection {
    Connecting,
    Online,
    Offline,
}

/**
 * Плашка соединения: [Connecting] — только первое подключение; [Reconnecting] —
 * соединение терялось: SDK переподключается или ждёт сеть (одна плашка, без мигания);
 * [AuthFailed] — сервер отверг авторизацию, SDK подключится после повторного входа.
 */
enum class SupportChatConnectionBanner {
    Connecting,
    Reconnecting,
    AuthFailed,
}
