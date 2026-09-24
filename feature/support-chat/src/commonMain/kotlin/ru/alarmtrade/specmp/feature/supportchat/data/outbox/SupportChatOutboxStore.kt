package ru.alarmtrade.specmp.feature.supportchat.data.outbox

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessage
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus

/**
 * Персистентная очередь неотправленных исходящих сообщений (P-10).
 *
 * Текст баннера «Сообщения будут отправлены при восстановлении связи» —
 * заявленное поведение фичи, поэтому очередь переживает уход с экрана,
 * пересоздание Activity/процесса и смерть ViewModel. Живут здесь только
 * исходящие сообщения в статусе `Pending`/`Failed`; подтверждённые сервером
 * (`Sent`/`Read`) удаляются сразу.
 *
 * Наследует [AuthCloseable]: `LogoutUseCase` очищает очередь при смене
 * аккаунта, чтобы чужие сообщения не протекали в новую сессию.
 */
interface SupportChatOutboxStore : AuthCloseable {

    fun load(): List<SupportChatOutboxEntry>

    fun save(entries: List<SupportChatOutboxEntry>)

    fun clear()

    override suspend fun release() {
        clear()
    }
}

@Serializable
data class SupportChatOutboxEntry(
    val clientId: String,
    val timeMillis: Long,
    val text: String?,
    val attachment: Attachment?,
    val status: Status,
) {

    @Serializable
    data class Attachment(
        val id: String,
        val name: String,
        val kind: Kind,
        val localPath: String,
        val mimeType: String?,
        val sizeBytes: Long?,
    )

    @Serializable
    enum class Kind { Image, Document }

    @Serializable
    enum class Status { Pending, Failed }
}

/**
 * Снимок текущей очереди из ленты сообщений: все мои неподтверждённые
 * сообщения (Pending/Failed) с сохранённым локальным вложением.
 */
fun List<SupportChatMessage>.toOutboxEntries(): List<SupportChatOutboxEntry> {
    return filter { message ->
        message.isMine &&
            message.serverId == null &&
            (message.status == SupportChatMessageStatus.Pending ||
                message.status == SupportChatMessageStatus.Failed)
    }.map { message ->
        val attachment = message.attachment
        SupportChatOutboxEntry(
            clientId = message.id,
            timeMillis = message.time.toMillis(),
            text = message.text,
            attachment = attachment?.localPath?.let { localPath ->
                SupportChatOutboxEntry.Attachment(
                    id = attachment.id,
                    name = attachment.name,
                    kind = when (attachment) {
                        is SupportChatAttachment.Image -> SupportChatOutboxEntry.Kind.Image
                        is SupportChatAttachment.Document -> SupportChatOutboxEntry.Kind.Document
                    },
                    localPath = localPath,
                    mimeType = (attachment as? SupportChatAttachment.Document)?.mimeType,
                    sizeBytes = (attachment as? SupportChatAttachment.Document)?.sizeBytes,
                )
            },
            status = when (message.status) {
                SupportChatMessageStatus.Pending -> SupportChatOutboxEntry.Status.Pending
                else -> SupportChatOutboxEntry.Status.Failed
            },
        )
    }
}

private fun LocalDateTime.toMillis(): Long {
    return toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
