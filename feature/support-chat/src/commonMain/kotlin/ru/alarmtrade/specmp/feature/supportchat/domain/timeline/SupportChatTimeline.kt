package ru.alarmtrade.specmp.feature.supportchat.domain.timeline

import kotlin.time.Duration.Companion.minutes
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/**
 * Лента сообщений чата — неизменяемое значение с инвариантами, которые держатся
 * здесь, а не в местах вызова:
 *
 * 1. **Порядок** — по возрастанию `time` (стабильно: равные сохраняют порядок вставки).
 * 2. **Уникальность** — серверное сообщение встречается один раз по `serverId`
 *    независимо от автора: повторная доставка заменяет, а не дублирует (иначе
 *    дубликат ключа в `LazyColumn`).
 * 3. **Эхо — FIFO**: серверное эхо моего сообщения заменяет самое раннее
 *    неподтверждённое оптимистичное с тем же содержимым, затем — той же формы.
 *
 * Все операции — чистые функции без корутин; конкурентность — забота вызывающего
 * (VM меняет ленту только внутри `MutableStateFlow.update {}`).
 */
class SupportChatTimeline private constructor(val messages: List<SupportChatMessage>) {

    val isEmpty: Boolean get() = messages.isEmpty()

    fun find(id: String): SupportChatMessage? = messages.firstOrNull { it.id == id }

    /**
     * Страница истории: известные по `serverId` сообщения не дублируются, а мои
     * серверные сообщения заменяют неподтверждённые оптимистичные (FIFO, как эхо) —
     * после рестарта восстановленное из outbox сообщение, которое на самом деле
     * дошло, не будет отправлено повторно.
     */
    fun withHistoryPage(page: List<SupportChatMessage>): SupportChatTimeline {
        val known = messages.mapNotNullTo(HashSet()) { it.serverId }
        val fresh = page.filter { it.serverId == null || known.add(it.serverId) }
        if (fresh.isEmpty()) return this
        val working = messages.toMutableList()
        fresh.sortedBy { it.time }.forEach { message ->
            // Старое моё сообщение с тем же текстом («ок» неделю назад) — не эхо
            // свежего неотправленного: сервер не может получить его раньше, чем
            // мы его создали (с запасом на расхождение часов устройства и сервера).
            val echoIndex = if (message.isMine) {
                working.indexOfEcho(message) { optimistic -> message.time >= optimistic.time - CLOCK_SKEW }
            } else {
                -1
            }
            if (echoIndex != -1) working[echoIndex] = message else working += message
        }
        return SupportChatTimeline(sorted(working))
    }

    /** Живое сообщение: замена по `serverId`, иначе FIFO-эхо моего, иначе вставка. */
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

    /** Оптимистичные исходящие (порядок списка сохраняется при равном времени). */
    fun withOptimistic(outgoing: List<SupportChatMessage>): SupportChatTimeline {
        if (outgoing.isEmpty()) return this
        return SupportChatTimeline(sorted(messages + outgoing))
    }

    fun withStatus(id: String, status: SupportChatMessageStatus): SupportChatTimeline = map(id) {
        it.copy(status = status)
    }

    /** Перевод статусов по серверным id (событие «прочитано»). */
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

    /**
     * Атомарный (в пределах одного `update {}`) переход статуса: возвращает новую
     * ленту, только если сообщение сейчас в одном из [from]. `null` — переход не
     * состоялся (уже отправляется, уже отправлено, нет такого сообщения): вызывающий
     * не должен ничего отправлять. Защита от двойного ретрая.
     */
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

// Сервер может переименовать вложение — тогда сопоставляем по форме контента. Только
// вложения: у текста «форма» совпадает всегда, и чужой мой текст из окна CLOCK_SKEW
// «подтвердил» бы ожидающий — тот удалился бы из очереди, так и не уйдя на сервер.
private fun SupportChatMessage.shapeMatches(other: SupportChatMessage): Boolean {
    if (text.isNullOrBlank() != other.text.isNullOrBlank()) return false
    val a = attachment ?: return false
    val b = other.attachment ?: return false
    return (a is SupportChatAttachment.Image && b is SupportChatAttachment.Image) ||
        (a is SupportChatAttachment.Document && b is SupportChatAttachment.Document)
}
