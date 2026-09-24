package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment

/**
 * Presentation-модель вложения для UI (U-12).
 *
 * Доменный `SupportChatAttachment` — sealed-иерархия; передавать его параметром
 * composable напрямую компилятор считает нестабильным. Плоская @Immutable-модель
 * маппится на границе экрана; `url`/`localPath`/`mimeType`/`sizeBytes`
 * nullability совпадает с доменной семантикой (см. SupportChatAttachment).
 */
@Immutable
data class SupportChatUiAttachment(
    val id: String,
    val name: String,
    val kind: Kind,
    val url: String?,
    val localPath: String?,
    val mimeType: String?,
    val sizeBytes: Long?,
) {
    enum class Kind { Image, Document }
}

/**
 * Маппинг доменного вложения в presentation-модель на границе UI.
 */
fun SupportChatAttachment.toUiAttachment(): SupportChatUiAttachment {
    return when (this) {
        is SupportChatAttachment.Image -> SupportChatUiAttachment(
            id = id,
            name = name,
            kind = SupportChatUiAttachment.Kind.Image,
            url = url,
            localPath = localPath,
            mimeType = null,
            sizeBytes = null,
        )
        is SupportChatAttachment.Document -> SupportChatUiAttachment(
            id = id,
            name = name,
            kind = SupportChatUiAttachment.Kind.Document,
            url = url,
            localPath = localPath,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
        )
    }
}
