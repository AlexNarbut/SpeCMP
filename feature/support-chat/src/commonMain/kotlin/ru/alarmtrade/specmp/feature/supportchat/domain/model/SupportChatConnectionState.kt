package ru.alarmtrade.specmp.feature.supportchat.domain.model

/** Владелец соединения — SDK; фича только отображает состояние. */
sealed interface SupportChatConnectionState {
    data object Connecting : SupportChatConnectionState

    /** Сокет открыт и подписка комнаты подтверждена — можно отправлять. Каждый вход сюда — перечитать историю. */
    data object Ready : SupportChatConnectionState

    data object Reconnecting : SupportChatConnectionState

    /** Сети нет или ОС её блокирует (фон) — SDK ждёт сеть без попыток. */
    data object WaitingForNetwork : SupportChatConnectionState

    /** Сервер отверг авторизацию. SDK подключится сам после повторного входа. */
    data object AuthFailed : SupportChatConnectionState
}
