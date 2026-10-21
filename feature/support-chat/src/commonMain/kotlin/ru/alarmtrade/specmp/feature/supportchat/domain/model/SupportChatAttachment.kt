package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Вложение сообщения чата.
 *
 * Единая иерархия для входящих, отправленных и ожидающих отправки вложений:
 * состояние выражается nullability полей, а не отдельными типами.
 *
 * - Входящее/подтверждённое сервером: [url] != null, [localPath] == null.
 * - Оптимистичное (ещё не отправлено): [localPath] != null, [url] == null.
 * - Отправка возможна, только пока жив [localPath]: повтор после потери
 *   локального файла невозможен — такое сообщение уходит в `Failed`.
 */
sealed class SupportChatAttachment {
    abstract val id: String
    abstract val name: String
    abstract val url: String?
    abstract val localPath: String?

    data class Image(
        override val id: String,
        override val name: String,
        override val url: String? = null,
        override val localPath: String? = null
    ) : SupportChatAttachment()

    data class Document(
        override val id: String,
        override val name: String,
        override val url: String? = null,
        override val localPath: String? = null,
        val mimeType: String? = null,
        val sizeBytes: Long? = null
    ) : SupportChatAttachment()
}
