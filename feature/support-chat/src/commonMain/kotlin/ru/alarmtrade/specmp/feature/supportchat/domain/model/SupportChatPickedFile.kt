package ru.alarmtrade.specmp.feature.supportchat.domain.model

/**
 * Файл, выбранный платформенным пикером для отправки в чат.
 *
 * Модель доменного слоя: feature получает его от платформенного порта
 * (`presentation/platform/SupportChatFilePicker`, actual в `composeApp`) и
 * передаёт в репозиторий как есть.
 */
data class SupportChatPickedFile(
    val id: String,
    val name: String,
    val path: String,
    val mimeType: String?,
    val sizeBytes: Long? = null,
)
