package ru.alarmtrade.specmp.feature.supportchat.domain.model

import kotlinx.datetime.Instant

/**
 * Сообщение чата техподдержки.
 *
 * ## Контракт идентичности
 * - Серверное сообщение: `id = serverId.toString()`, [serverId] != null. Уникально по
 *   [serverId] в ленте — независимо от автора (см. `SupportChatTimeline`).
 * - Локальное оптимистичное (ещё не подтверждено сервером): `serverId = null`,
 *   `id` — клиентский идентификатор (`local-<uuid>`), уникальный глобально: он же
 *   ключ записи в outbox и переживает пересоздание VM и процесса.
 *
 * Серверное эхо моего сообщения заменяет оптимистичное (FIFO, см. `SupportChatTimeline`).
 *
 * [time] — момент времени, а не локальная дата: в местное время его переводит UI,
 * поэтому смена часового пояса не рассинхронизирует серверные и локальные сообщения.
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
