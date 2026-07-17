package ru.alarmtrade.specmp.feature.supportchat.domain.picker

/**
 * Платформенная абстракция для выбора файлов из галереи/файловой системы.
 *
 * Реализация живёт в платформенном слое `composeApp` (activity-result launchers, desktop dialogs,
 * iOS UIDocumentPicker). Feature-модуль получает только готовый к отправке путь и мета-информацию.
 */
data class SupportChatPickedFile(
    val id: String,
    val name: String,
    val path: String,
    val mimeType: String?,
    val sizeBytes: Long? = null,
)

interface SupportChatFilePicker {
    fun launchImage(onPicked: (SupportChatPickedFile?) -> Unit)
    fun launchDocument(onPicked: (SupportChatPickedFile?) -> Unit)
}
