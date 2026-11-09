package ru.alarmtrade.specmp.feature.supportchat.domain.model

/** Семантика — для моих сообщений; у входящих статус — лишь `wasRead` на момент получения. */
enum class SupportChatMessageStatus {
    Sending,
    Sent,
    Read,
    Pending,
    Failed,
}
