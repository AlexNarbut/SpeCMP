package ru.alarmtrade.specmp.feature.supportchat.domain.model

import kotlinx.datetime.Instant

/**
 * Серверное: `id = serverId.toString()`; оптимистичное: `serverId = null`, `id = local-<uuid>` — ключ outbox,
 * переживает пересоздание VM и процесса. Серверное эхо моего заменяет оптимистичное (FIFO, `SupportChatTimeline`).
 */
data class SupportChatMessage(
    val id: String,
    val serverId: Long?,
    val text: String?,
    val attachment: SupportChatAttachment?,
    val time: Instant,
    val isMine: Boolean,
    val authorEmail: String?,
    val status: SupportChatMessageStatus,
)
