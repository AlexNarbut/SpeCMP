package ru.alarmtrade.specmp.feature.supportchat.domain.model

sealed class SupportChatAttachment {
    abstract val id: String
    abstract val name: String

    data class Image(
        override val id: String,
        override val name: String,
        val url: String,
        val localPath: String? = null,
    ) : SupportChatAttachment()

    data class Document(
        override val id: String,
        override val name: String,
        val url: String?,
        val localPath: String? = null,
        val mimeType: String? = null,
        val sizeBytes: Long? = null,
    ) : SupportChatAttachment()
}
