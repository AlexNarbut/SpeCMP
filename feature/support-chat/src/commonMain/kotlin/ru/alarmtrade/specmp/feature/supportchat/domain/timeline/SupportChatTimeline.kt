package ru.alarmtrade.specmp.feature.supportchat.domain.timeline

import kotlin.time.Duration.Companion.minutes
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/**
 * Инварианты: порядок по `time` (стабильно), серверное сообщение — один раз по `serverId` (иначе дубль ключа в
 * `LazyColumn`), эхо моего заменяет самое раннее ожидающее с тем же содержимым, затем — той же формы (FIFO).
 */
class SupportChatTimeline private constructor(val messages: List<SupportChatMessage>) {

    val isEmpty: Boolean get() = messages.isEmpty()

    fun find(id: String): SupportChatMessage? = messages.firstOrNull { it.id == id }

    /**
     * Мои серверные сообщения заменяют неподтверждённые оптимистичные (FIFO): восстановленное из outbox сообщение,
     * которое на самом деле дошло, не уйдёт повторно.
     */
    fun withHistoryPage(page: List<SupportChatMessage>): SupportChatTimeline {
        val known = messages.mapNotNullTo(HashSet()) { it.serverId }
        val fresh = page.filter { it.serverId == null || known.add(it.serverId) }
        if (fresh.isEmpty()) return this
        val working = messages.toMutableList()
        fresh.sortedBy { it.time }.forEach { message ->
            // Старое моё сообщение с тем же текстом — не эхо свежего: сервер не мог получить его раньше,
            // чем мы его создали (с запасом на расхождение часов устройства и сервера).
            val echoIndex = if (message.isMine) {
                working.indexOfEcho(message) { optimistic -> message.time >= optimistic.time - CLOCK_SKEW }
            } else {
                -1
            }
            if (echoIndex != -1) working[echoIndex] = message else working += message
        }
        return SupportChatTimeline(sorted(working))
    }

    fun withIncoming(message: SupportChatMessage): SupportChatTimeline {
        val sameServer = message.serverId?.let { serverId ->
            messages.indexOfFirst { it.serverId == serverId }
        } ?: -1
        val index = when {
            sameServer != -1 -> sameServer
            message.isMine -> messages.indexOfEcho(message)
            else -> -1
        }
        val updated = if (index != -1) {
            messages.toMutableList().apply { set(index, message) }
        } else {
            messages + message
        }
        return SupportChatTimeline(sorted(updated))
    }

    fun withOptimistic(outgoing: List<SupportChatMessage>): SupportChatTimeline {
        if (outgoing.isEmpty()) return this
        return SupportChatTimeline(sorted(messages + outgoing))
    }

    fun withStatus(id: String, status: SupportChatMessageStatus): SupportChatTimeline = map(id) {
        it.copy(status = status)
    }

    fun withRead(serverIds: Collection<Long>): SupportChatTimeline {
        if (serverIds.isEmpty()) return this
        val ids = serverIds.toHashSet()
        return SupportChatTimeline(
            messages.map { message ->
                if (message.serverId != null && message.serverId in ids) {
                    message.copy(status = SupportChatMessageStatus.Read)
                } else {
                    message
                }
            },
        )
    }

    /** `null` — сообщение не в одном из [from] (уже отправляется, отправлено, нет): защита от двойного ретрая. */
    fun tryTransition(
        id: String,
        from: Set<SupportChatMessageStatus>,
        to: SupportChatMessageStatus,
    ): SupportChatTimeline? {
        val current = find(id) ?: return null
        if (current.status !in from) return null
        return withStatus(id, to)
    }

    fun without(id: String): SupportChatTimeline {
        if (find(id) == null) return this
        return SupportChatTimeline(messages.filterNot { it.id == id })
    }

    private fun map(id: String, transform: (SupportChatMessage) -> SupportChatMessage): SupportChatTimeline {
        if (find(id) == null) return this
        return SupportChatTimeline(messages.map { if (it.id == id) transform(it) else it })
    }

    override fun equals(other: Any?): Boolean = other is SupportChatTimeline && other.messages == messages

    override fun hashCode(): Int = messages.hashCode()

    override fun toString(): String = "SupportChatTimeline(${messages.size} messages)"

    companion object {
        val Empty = SupportChatTimeline(emptyList())

        fun of(messages: List<SupportChatMessage>): SupportChatTimeline = Empty.withHistoryPage(messages)
    }
}

private fun sorted(messages: List<SupportChatMessage>): List<SupportChatMessage> = messages.sortedBy { it.time }

private fun List<SupportChatMessage>.indexOfEcho(
    echo: SupportChatMessage,
    accepts: (SupportChatMessage) -> Boolean = { true },
): Int {
    // Обе ветки — FIFO: порядок подтверждений сервера соответствует порядку отправки.
    val byContent = indexOfFirst { it.isAwaitingEcho() && accepts(it) && it.contentMatches(echo) }
    if (byContent != -1) return byContent
    return indexOfFirst { it.isAwaitingEcho() && accepts(it) && it.shapeMatches(echo) }
}

private val CLOCK_SKEW = 2.minutes

private fun SupportChatMessage.isAwaitingEcho(): Boolean = isMine && serverId == null

private fun SupportChatMessage.contentMatches(other: SupportChatMessage): Boolean =
    text == other.text && attachment?.name == other.attachment?.name

// Сервер может переименовать вложение — сопоставляем по форме. Только вложения: у текста форма совпадает всегда,
// и чужой мой текст из окна CLOCK_SKEW «подтвердил» бы ожидающий, так и не ушедший на сервер.
private fun SupportChatMessage.shapeMatches(other: SupportChatMessage): Boolean {
    if (text.isNullOrBlank() != other.text.isNullOrBlank()) return false
    val a = attachment ?: return false
    val b = other.attachment ?: return false
    return (a is SupportChatAttachment.Image && b is SupportChatAttachment.Image) ||
        (a is SupportChatAttachment.Document && b is SupportChatAttachment.Document)
}
