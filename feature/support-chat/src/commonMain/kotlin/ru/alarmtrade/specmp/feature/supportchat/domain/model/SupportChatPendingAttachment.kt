package ru.alarmtrade.specmp.feature.supportchat.domain.model

sealed class SupportChatPendingAttachment {
    abstract val id: String
    abstract val name: String
    abstract val localPath: String
    abstract val mimeType: String?
    abstract val sizeBytes: Long?

    data class Image(
        override val id: String,
        override val name: String,
        override val localPath: String,
        override val mimeType: String? = null,
        override val sizeBytes: Long? = null,
    ) : SupportChatPendingAttachment()

    data class Document(
        override val id: String,
        override val name: String,
        override val localPath: String,
        override val mimeType: String? = null,
        override val sizeBytes: Long? = null,
    ) : SupportChatPendingAttachment()
}
