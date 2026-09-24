package ru.alarmtrade.specmp.feature.supportchat.domain.model

import kotlinx.datetime.LocalDateTime

/**
 * Сообщение чата техподдержки.
 *
 * ## Контракт идентичности
 * - Серверное сообщение: `id = serverId.toString()`.
 * - Локальное оптимистичное (ещё не подтверждено сервером): `serverId = null`,
 *   `id` — клиентский идентификатор, уникальный в пределах инстанса ViewModel.
 *
 * На этом контракте держатся дедупликация истории, сопоставление серверного
 * эха с оптимистичным сообщением и ретраи — менять только вместе с тестами.
 */
data class SupportChatMessage(
    val id: String,
    val serverId: Long?,
    val text: String?,
    val attachment: SupportChatAttachment?,
    val time: LocalDateTime,
    val isMine: Boolean,
    val authorEmail: String?,
    val status: SupportChatMessageStatus,
)
