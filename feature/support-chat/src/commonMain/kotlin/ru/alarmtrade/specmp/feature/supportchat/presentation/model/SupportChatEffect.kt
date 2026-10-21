package ru.alarmtrade.specmp.feature.supportchat.presentation.model

/**
 * One-shot события экрана чата техподдержки (snackbar и т.п.).
 */
sealed interface SupportChatEffect {

    /**
     * Ошибка отправки сообщения. При [canRetry] у снекбара есть action «Повторить»
     * (`onRetry`); без него (локальный файл вложения пропал) — только сообщение,
     * убрать такое сообщение можно удалением.
     */
    data class MessageSendFailed(val messageId: String, val canRetry: Boolean) : SupportChatEffect

    /** Ошибка загрузки страницы истории при живой ленте. */
    data object HistoryLoadFailed : SupportChatEffect

    /** Вложение сохранено в Загрузки/Pandora Spec. */
    data class AttachmentDownloaded(val filePath: String) : SupportChatEffect

    /** Ошибка скачивания/сохранения вложения. */
    data object AttachmentDownloadFailed : SupportChatEffect

    /** Нет разрешения на запись в «Загрузки» (Android 7–9). */
    data object AttachmentSavePermissionDenied : SupportChatEffect

    /** Вложение не открылось: нет приложения для типа ([noApp]) либо ошибка скачивания/записи. */
    data class AttachmentOpenFailed(val noApp: Boolean) : SupportChatEffect

    /** Выбранный файл не прикреплён: больше лимита ([tooLarge]) либо не удалось скопировать. */
    data class AttachmentRejected(val tooLarge: Boolean) : SupportChatEffect
}
