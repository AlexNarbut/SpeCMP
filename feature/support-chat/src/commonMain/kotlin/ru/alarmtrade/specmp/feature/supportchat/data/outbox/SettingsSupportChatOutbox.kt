package ru.alarmtrade.specmp.feature.supportchat.data.outbox

import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatOutbox

/**
 * Outbox в именованном хранилище `multiplatform-settings` (JSON одной строкой).
 *
 * Каждая операция — read-modify-write под [mutex]: key-value хранилище не даёт
 * атомарности, а переходы приходят из разных корутин (коллектор событий и
 * интенты экрана). Очередь мала (единицы сообщений), поэтому полная перезапись
 * списка проще и надёжнее диффов. Диспетчер выбирает вызывающий.
 */
class SettingsSupportChatOutbox(private val settings: Settings) : SupportChatOutbox {

    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(): List<SupportChatMessage> = mutex.withLock { read() }.map { it.toMessage() }

    override suspend fun enqueue(message: SupportChatMessage) {
        val entry = message.toEntry() ?: return
        mutex.withLock {
            val entries = read()
            val index = entries.indexOfFirst { it.clientId == entry.clientId }
            write(
                if (index == -1) entries + entry else entries.toMutableList().apply { set(index, entry) }
            )
        }
    }

    override suspend fun markSent(clientId: String) {
        mutex.withLock {
            val entries = read()
            val index = entries.indexOfFirst { it.clientId == clientId }
            if (index != -1) {
                write(
                    entries.toMutableList().apply {
                        set(index, get(index).copy(status = SupportChatOutboxEntry.Status.Sent))
                    }
                )
            }
        }
    }

    override suspend fun remove(clientId: String) {
        mutex.withLock {
            val entries = read()
            if (entries.any { it.clientId == clientId }) {
                write(entries.filterNot { it.clientId == clientId })
            }
        }
    }

    override suspend fun release() {
        mutex.withLock { settings.remove(KEY) }
    }

    private fun read(): List<SupportChatOutboxEntry> {
        val raw = settings.getStringOrNull(KEY) ?: return emptyList()
        return try {
            json.decodeFromString<List<SupportChatOutboxEntry>>(raw)
        } catch (e: Exception) {
            // Битый снимок — не повод ломать экран: начинаем с чистой очереди.
            emptyList()
        }
    }

    private fun write(entries: List<SupportChatOutboxEntry>) {
        if (entries.isEmpty()) {
            settings.remove(KEY)
        } else {
            settings.putString(KEY, json.encodeToString(entries))
        }
    }

    private companion object {
        const val KEY = "outbox"
    }
}

@Serializable
internal data class SupportChatOutboxEntry(
    val clientId: String,
    val timeMillis: Long,
    val text: String?,
    val attachment: Attachment?,
    val status: Status
) {

    @Serializable
    data class Attachment(
        val id: String,
        val name: String,
        val kind: Kind,
        val localPath: String,
        val mimeType: String?,
        val sizeBytes: Long?
    )

    @Serializable
    enum class Kind { Image, Document }

    // Sent — ушло в сокет, эха нет; восстанавливается как Pending.
    @Serializable
    enum class Status { Pending, Failed, Sent }
}

// В очередь попадают только мои неподтверждённые сообщения в Pending/Failed.
// Вложение без локального файла сохранять бессмысленно — повторить его нельзя.
private fun SupportChatMessage.toEntry(): SupportChatOutboxEntry? {
    if (!isMine || serverId != null) return null
    val entryStatus = when (status) {
        SupportChatMessageStatus.Pending -> SupportChatOutboxEntry.Status.Pending

        SupportChatMessageStatus.Failed -> SupportChatOutboxEntry.Status.Failed

        SupportChatMessageStatus.Sending,
        SupportChatMessageStatus.Sent,
        SupportChatMessageStatus.Read -> return null
    }
    val source = attachment
    val storedAttachment = if (source != null) {
        val localPath = source.localPath ?: return null
        SupportChatOutboxEntry.Attachment(
            id = source.id,
            name = source.name,
            kind = when (source) {
                is SupportChatAttachment.Image -> SupportChatOutboxEntry.Kind.Image
                is SupportChatAttachment.Document -> SupportChatOutboxEntry.Kind.Document
            },
            localPath = localPath,
            mimeType = (source as? SupportChatAttachment.Document)?.mimeType,
            sizeBytes = (source as? SupportChatAttachment.Document)?.sizeBytes
        )
    } else {
        null
    }
    return SupportChatOutboxEntry(
        clientId = id,
        timeMillis = time.toEpochMilliseconds(),
        text = text,
        attachment = storedAttachment,
        status = entryStatus
    )
}

private fun SupportChatOutboxEntry.toMessage(): SupportChatMessage = SupportChatMessage(
    id = clientId,
    serverId = null,
    text = text,
    attachment = attachment?.let { stored ->
        when (stored.kind) {
            SupportChatOutboxEntry.Kind.Image -> SupportChatAttachment.Image(
                id = stored.id,
                name = stored.name,
                localPath = stored.localPath
            )

            SupportChatOutboxEntry.Kind.Document -> SupportChatAttachment.Document(
                id = stored.id,
                name = stored.name,
                localPath = stored.localPath,
                mimeType = stored.mimeType,
                sizeBytes = stored.sizeBytes
            )
        }
    },
    time = Instant.fromEpochMilliseconds(timeMillis),
    isMine = true,
    authorEmail = null,
    status = when (status) {
        // Доставка Sent неизвестна (эха не было) — повторяем.
        SupportChatOutboxEntry.Status.Pending,
        SupportChatOutboxEntry.Status.Sent -> SupportChatMessageStatus.Pending

        SupportChatOutboxEntry.Status.Failed -> SupportChatMessageStatus.Failed
    }
)
