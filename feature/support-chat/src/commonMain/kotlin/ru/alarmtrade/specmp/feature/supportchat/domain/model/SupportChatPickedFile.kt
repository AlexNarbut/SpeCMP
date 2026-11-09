package ru.alarmtrade.specmp.feature.supportchat.domain.model

data class SupportChatPickedFile(
    val id: String,
    val name: String,
    val path: String,
    val mimeType: String?,
    val sizeBytes: Long? = null,
)
