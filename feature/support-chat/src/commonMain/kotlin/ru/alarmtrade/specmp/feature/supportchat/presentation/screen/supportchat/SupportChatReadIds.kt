package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat

/**
 * Прочтения входящих: [marked] — отправлены или в отправке, [pending] — ждут батча; [isFlushScheduled] — батч
 * запланирован или отправляется (новые id копятся в [pending], таймер не перезапускается).
 */
data class SupportChatReadIds(
    val marked: Set<Long> = emptySet(),
    val pending: Set<Long> = emptySet(),
    val isFlushScheduled: Boolean = false,
) {
    fun isKnown(serverId: Long): Boolean = serverId in marked || serverId in pending

    internal fun withPending(ids: Collection<Long>): SupportChatReadIds =
        copy(pending = pending + ids, isFlushScheduled = true)

    internal fun pendingTaken(): SupportChatReadIds = copy(marked = marked + pending, pending = emptySet())

    // Ошибка отправки: id возвращаются в буфер — уйдут со следующим батчем.
    internal fun returned(ids: Set<Long>): SupportChatReadIds = copy(marked = marked - ids, pending = pending + ids)
}
