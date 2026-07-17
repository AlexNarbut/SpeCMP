package ru.alarmtrade.specmp.feature.supportchat.domain.model

import kotlinx.datetime.LocalDateTime

data class SupportChatMessage(
    val id: String,
    val serverId: Long?,
    val text: String?,
    val attachment: SupportChatAttachment?,
    val time: LocalDateTime,
    val displayTime: String,
    val isMine: Boolean,
    val authorEmail: String?,
    val status: SupportChatMessageStatus,
)
