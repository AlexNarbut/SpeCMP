package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Состояние единственного соединения чата. Владелец соединения — SDK: он подключается,
 * ждёт подписку комнаты, переподключается и ждёт сеть; фича только отображает состояние.
 */
sealed interface SupportChatConnectionState {
    /** Первое подключение сессии. */
    data object Connecting : SupportChatConnectionState

    /** Сокет открыт и подписка комнаты подтверждена — можно отправлять. Каждый вход сюда — перечитать историю. */
    data object Ready : SupportChatConnectionState

    /** Соединение потеряно, SDK переподключается. */
    data object Reconnecting : SupportChatConnectionState

    /** Сети нет или ОС её блокирует (фон) — SDK ждёт сеть без попыток. */
    data object WaitingForNetwork : SupportChatConnectionState

    /** Сервер отверг авторизацию. SDK подключится сам после повторного входа. */
    data object AuthFailed : SupportChatConnectionState

    /** Сессию не удалось начать (не получен профиль). Финал: поток `session()` завершается. */
    data object Unavailable : SupportChatConnectionState
}
