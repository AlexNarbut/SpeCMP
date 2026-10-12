package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Статус доставки сообщения.
 *
 * Семантика выстроена вокруг МОИХ сообщений: `Sending`/`Pending`/`Failed`
 * и переход `Sent → Read` по событию `MessagesRead`. У входящих статус — лишь
 * факт `wasRead` от сервера на момент получения; логика прочтения входящих
 * (`onMessagesVisible`) опирается на `isMine`, а не на этот статус (A-7).
 * UI иконку доставки рисует только при `isMine`.
 */
enum class SupportChatMessageStatus {
    Sending,
    Sent,
    Read,
    Pending,
    Failed,
}
