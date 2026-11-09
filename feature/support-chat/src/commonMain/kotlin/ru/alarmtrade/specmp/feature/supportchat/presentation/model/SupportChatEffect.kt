package ru.alarmtrade.specmp.feature.supportchat.presentation.model

sealed interface SupportChatEffect {

    /** Без [canRetry] (локальный файл вложения пропал) — только сообщение, убрать можно удалением. */
    data class MessageSendFailed(val messageId: String, val canRetry: Boolean) : SupportChatEffect

    data object HistoryLoadFailed : SupportChatEffect

    data class AttachmentDownloaded(val filePath: String) : SupportChatEffect

    data object AttachmentDownloadFailed : SupportChatEffect

    data object AttachmentSavePermissionDenied : SupportChatEffect

    data class AttachmentOpenFailed(val noApp: Boolean) : SupportChatEffect

    data class AttachmentRejected(val tooLarge: Boolean) : SupportChatEffect
}
