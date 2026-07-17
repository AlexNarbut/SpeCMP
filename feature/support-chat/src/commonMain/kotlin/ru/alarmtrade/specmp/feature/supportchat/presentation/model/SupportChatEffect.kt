package ru.alarmtrade.specmp.feature.supportchat.presentation.model

/**
 * One-shot события экрана чата техподдержки (snackbar и т.п.).
 */
sealed interface SupportChatEffect {

    /** Ошибка отправки сообщения; по action снекбара сообщение ретраится через onRetry. */
    data class MessageSendFailed(val messageId: String) : SupportChatEffect

    /** Ошибка загрузки страницы истории. */
    data object HistoryLoadFailed : SupportChatEffect

    /** Вложение сохранено в Загрузки/Pandora Spec. */
    data class AttachmentDownloaded(val filePath: String) : SupportChatEffect

    /** Ошибка скачивания/сохранения вложения. */
    data object AttachmentDownloadFailed : SupportChatEffect
}
