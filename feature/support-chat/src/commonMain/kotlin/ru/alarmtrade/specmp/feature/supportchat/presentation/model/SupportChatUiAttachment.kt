package ru.alarmtrade.specmp.feature.supportchat.presentation.model

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatAttachment
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPickedFile

/** Плоская `@Immutable`-модель: доменная sealed-иерархия параметром composable нестабильна. */
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

fun SupportChatAttachment.toUiAttachment(): SupportChatUiAttachment = when (this) {
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

fun SupportChatUiAttachment.toAttachment(): SupportChatAttachment = when (kind) {
    SupportChatUiAttachment.Kind.Image -> SupportChatAttachment.Image(
        id = id,
        name = name,
        url = url,
        localPath = localPath,
    )

    SupportChatUiAttachment.Kind.Document -> SupportChatAttachment.Document(
        id = id,
        name = name,
        url = url,
        localPath = localPath,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )
}

/** Тип задаёт источник выбора: картинка, выбранная как документ, уходит документом. */
fun SupportChatPickedFile.toUiAttachment(kind: SupportChatUiAttachment.Kind): SupportChatUiAttachment =
    SupportChatUiAttachment(
        id = id,
        name = name,
        kind = kind,
        url = null,
        localPath = path,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )
