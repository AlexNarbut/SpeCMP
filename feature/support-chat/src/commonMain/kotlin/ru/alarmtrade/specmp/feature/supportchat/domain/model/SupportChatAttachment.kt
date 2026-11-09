package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Подтверждённое сервером: [url] != null; оптимистичное: [localPath] != null. Повтор после потери локального файла
 * невозможен — такое сообщение уходит в `Failed`.
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
        override val localPath: String? = null,
    ) : SupportChatAttachment()

    data class Document(
        override val id: String,
        override val name: String,
        override val url: String? = null,
        override val localPath: String? = null,
        val mimeType: String? = null,
        val sizeBytes: Long? = null,
    ) : SupportChatAttachment()
}
