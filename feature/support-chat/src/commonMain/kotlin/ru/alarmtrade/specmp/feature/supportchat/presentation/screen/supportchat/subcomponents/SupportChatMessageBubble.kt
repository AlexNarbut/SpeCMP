package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_document_actions
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_download
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_status_failed
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_status_pending
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_status_read
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_status_sending
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_status_sent
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_btn_delete
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_document_meta
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_size_bytes
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_size_gb
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_size_kb
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_size_mb
import ru.alarmtrade.specmp.core.localization.resources.supportchat_common_btn_retry
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpTextButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.image.CmpImage
import ru.alarmtrade.specmp.core.ui.view.image.CmpSubcomposeAsyncImage
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.layout.CmpSpacer
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatMessageStatus
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiMessage

@Composable
internal fun SupportChatMessageBubble(
    message: SupportChatUiMessage,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
    maxWidthFraction: Float = 0.82f,
    isDownloading: Boolean = false,
    onRetry: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onImageClick: (String) -> Unit = {},
    onDocumentClick: (SupportChatUiAttachment) -> Unit = {},
    onDownloadAttachment: (SupportChatUiAttachment) -> Unit = {},
) {
    val isMine = message.isMine
    val horizontalAlignment = if (isMine) Alignment.End else Alignment.Start

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        if (!isMine && message.authorEmail != null) {
            CmpText(
                text = message.authorEmail,
                style = CmpTextStyle.Paragraph2,
                modifier = Modifier.padding(horizontal = 6.dp),
                color = colors.primary,
            )
        }

        CmpSurface(
            modifier = Modifier.maxWidthFraction(maxWidthFraction),
            shape = bubbleShape(isMine = isMine),
            color = if (isMine) colors.primaryContainer else colors.surfaceContainerLow,
            contentColor = if (isMine) colors.onPrimaryContainer else colors.onSurface,
            border = if (isMine) {
                null
            } else {
                BorderStroke(
                    width = 1.dp,
                    color = colors.outlineVariant,
                )
            },
        ) {
            CmpColumn(
                modifier = Modifier.padding(
                    start = if (isImage(message)) 6.dp else 14.dp,
                    end = if (isImage(message)) 6.dp else 14.dp,
                    top = if (isImage(message)) 6.dp else 10.dp,
                    bottom = 8.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                message.text?.let { text ->
                    CmpText(text = text, style = CmpTextStyle.Paragraph2)
                }

                message.attachment?.let { attachment ->
                    SupportChatAttachmentContent(
                        attachment = attachment,
                        colors = colors,
                        contentColor = if (isMine) colors.onPrimaryContainer else colors.onSurface,
                        onImageClick = onImageClick,
                        onDocumentClick = onDocumentClick,
                        isDownloading = isDownloading,
                        downloadAction = attachment.downloadAction(
                            isMine = isMine,
                            onDownloadAttachment = onDownloadAttachment,
                        ),
                    )
                }

                MessageMetaRow(
                    message = message,
                    colors = colors,
                    isImage = isImage(message),
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }

        // Действия — явные кнопки под пузырём, а не кликабельный пузырь: не конфликтуют с тапом по картинке
        // и озвучиваются скринридером как кнопки.
        if (message.canDelete) {
            FailedMessageActions(
                canRetry = message.canRetry,
                onRetry = { onRetry(message.id) },
                onDelete = { onDelete(message.id) },
            )
        }
    }
}

/**
 * Ширина не больше доли [fraction] от доступной. Вместо `BoxWithConstraints`: тот — `SubcomposeLayout`,
 * дорогой в каждом элементе ленты при скролле.
 */
private fun Modifier.maxWidthFraction(fraction: Float): Modifier = layout { measurable, constraints ->
    val maxWidth = if (constraints.hasBoundedWidth) {
        (constraints.maxWidth * fraction).roundToInt()
    } else {
        constraints.maxWidth
    }
    val placeable = measurable.measure(
        constraints.copy(minWidth = minOf(constraints.minWidth, maxWidth), maxWidth = maxWidth),
    )
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

@Composable
private fun FailedMessageActions(canRetry: Boolean, onRetry: () -> Unit, onDelete: () -> Unit) {
    CmpRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (canRetry) {
            CmpTextButton(
                text = stringResource(Strings.string.supportchat_common_btn_retry),
                onClick = onRetry,
            )
        }
        CmpTextButton(
            text = stringResource(Strings.string.supportchat_chat_btn_delete),
            onClick = onDelete,
        )
    }
}

@Composable
private fun MessageMetaRow(
    message: SupportChatUiMessage,
    colors: CmpThemeColors,
    isImage: Boolean,
    modifier: Modifier = Modifier,
) {
    val metaColor = if (message.isMine) {
        colors.onPrimaryContainer.copy(alpha = 0.7f)
    } else {
        colors.onSurfaceVariant
    }

    CmpRow(
        modifier = modifier
            .padding(horizontal = if (isImage) 8.dp else 0.dp)
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpText(
            text = message.timeLabel,
            style = CmpTextStyle.Paragraph2,
            color = metaColor,
        )

        if (message.isMine) {
            CmpSpacer(modifier = Modifier.size(4.dp))
            MessageStatusIcon(
                status = message.status,
                metaColor = metaColor,
                readColor = colors.primary,
                errorColor = colors.error,
            )
        }
    }
}

@Composable
private fun MessageStatusIcon(status: SupportChatMessageStatus, metaColor: Color, readColor: Color, errorColor: Color) {
    val (icon, tint, description) = when (status) {
        SupportChatMessageStatus.Sending -> Triple(
            CmpIcons.Done,
            metaColor,
            stringResource(Strings.string.supportchat_chat_a11y_status_sending),
        )

        SupportChatMessageStatus.Sent -> Triple(
            CmpIcons.Done,
            metaColor,
            stringResource(Strings.string.supportchat_chat_a11y_status_sent),
        )

        SupportChatMessageStatus.Read -> Triple(
            CmpIcons.DoneAll,
            readColor,
            stringResource(Strings.string.supportchat_chat_a11y_status_read),
        )

        SupportChatMessageStatus.Pending -> Triple(
            CmpIcons.Schedule,
            metaColor,
            stringResource(Strings.string.supportchat_chat_a11y_status_pending),
        )

        SupportChatMessageStatus.Failed -> Triple(
            CmpIcons.Warning,
            errorColor,
            stringResource(Strings.string.supportchat_chat_a11y_status_failed),
        )
    }

    CmpIcon(
        icon = icon,
        contentDescription = description,
        tint = tint,
        modifier = Modifier.size(16.dp),
    )
}

@Composable
private fun SupportChatAttachmentContent(
    attachment: SupportChatUiAttachment,
    colors: CmpThemeColors,
    contentColor: Color,
    onImageClick: (String) -> Unit,
    onDocumentClick: (SupportChatUiAttachment) -> Unit,
    isDownloading: Boolean,
    downloadAction: (() -> Unit)?,
) {
    when (attachment.kind) {
        // Действия с документом («Открыть» / «Скачать») — диалог по тапу; байты
        // скачиваются только по выбору пользователя, не при показе ленты.
        SupportChatUiAttachment.Kind.Document -> {
            SupportChatDocumentAttachment(
                name = attachment.name,
                mimeType = attachment.mimeType,
                sizeBytes = attachment.sizeBytes,
                contentColor = contentColor,
                isDownloading = isDownloading,
                onClick = { onDocumentClick(attachment) },
            )
        }

        SupportChatUiAttachment.Kind.Image -> {
            SupportChatImageAttachment(
                name = attachment.name,
                source = attachment.imageSource(),
                colors = colors,
                onClick = onImageClick,
                isDownloading = isDownloading,
                downloadAction = downloadAction,
            )
        }
    }
}

private fun SupportChatUiAttachment.downloadAction(
    isMine: Boolean,
    onDownloadAttachment: (SupportChatUiAttachment) -> Unit,
): (() -> Unit)? {
    if (isMine) return null
    if (url == null) return null
    return { onDownloadAttachment(this) }
}

@Composable
private fun SupportChatDocumentAttachment(
    name: String,
    mimeType: String?,
    sizeBytes: Long?,
    contentColor: Color,
    isDownloading: Boolean,
    onClick: () -> Unit,
) {
    val metadata = documentMetadata(name = name, mimeType = mimeType, sizeBytes = sizeBytes)

    CmpRow(
        modifier = Modifier
            .widthIn(min = 200.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                enabled = !isDownloading,
                onClickLabel = stringResource(Strings.string.supportchat_chat_a11y_document_actions),
                role = Role.Button,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CmpBox(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(contentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            CmpIcon(
                icon = CmpIcons.Description,
                contentDescription = null,
                tint = contentColor,
            )
        }

        CmpColumn(modifier = Modifier.weight(1f, fill = false)) {
            CmpText(
                text = name,
                style = CmpTextStyle.Paragraph2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (metadata.isNotEmpty()) {
                CmpText(
                    text = metadata,
                    style = CmpTextStyle.Paragraph2,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }
        }

        if (isDownloading) {
            CmpCircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun SupportChatImageAttachment(
    name: String,
    source: String?,
    colors: CmpThemeColors,
    onClick: (String) -> Unit,
    isDownloading: Boolean,
    downloadAction: (() -> Unit)?,
) {
    CmpSubcomposeAsyncImage(
        model = source,
        contentDescription = name,
    ) {
        // Высота фиксирована, ширина следует за пропорциями изображения —
        // пустот по бокам пузыря нет ни у портретных, ни у панорамных картинок.
        val imagePainter = painter
        val intrinsicSize = imagePainter.intrinsicSize
        val ratio = when {
            intrinsicSize.isSpecified && intrinsicSize.height > 0 -> intrinsicSize.width / intrinsicSize.height
            else -> DEFAULT_IMAGE_RATIO
        }.coerceIn(MIN_IMAGE_RATIO, MAX_IMAGE_RATIO)

        CmpBox(
            modifier = Modifier
                .heightIn(max = IMAGE_MAX_HEIGHT)
                .aspectRatio(ratio)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            colors.surfaceVariant,
                            colors.surfaceContainerHighest,
                        ),
                    ),
                )
                .then(
                    if (source != null) Modifier.clickable { onClick(source) } else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CmpImage(
                painter = imagePainter,
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            if (isDownloading || downloadAction != null) {
                CmpSurface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surface.copy(alpha = 0.85f),
                    contentColor = colors.onSurface,
                ) {
                    if (isDownloading) {
                        CmpCircularProgressIndicator(
                            modifier = Modifier
                                .padding(6.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else if (downloadAction != null) {
                        CmpIconButton(
                            onClick = downloadAction,
                            icon = CmpIcons.Download,
                            contentDescription = stringResource(Strings.string.supportchat_chat_a11y_download),
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
            }
        }
    }
}

private val IMAGE_MAX_HEIGHT = 170.dp
private const val DEFAULT_IMAGE_RATIO = 4f / 3f
private const val MIN_IMAGE_RATIO = 0.75f
private const val MAX_IMAGE_RATIO = 2f

private const val FILE_URI_SCHEME = "file://"
private const val TENTHS_PER_UNIT = 10L

private fun SupportChatUiAttachment.imageSource(): String? = localPath?.let { FILE_URI_SCHEME + it } ?: url

private fun bubbleShape(isMine: Boolean): RoundedCornerShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = if (isMine) 16.dp else 4.dp,
    bottomEnd = if (isMine) 4.dp else 16.dp,
)

private fun isImage(message: SupportChatUiMessage): Boolean =
    message.attachment?.kind == SupportChatUiAttachment.Kind.Image

@Composable
private fun documentMetadata(name: String, mimeType: String?, sizeBytes: Long?): String {
    val extension = name.substringAfterLast('.', "").uppercase()
    val type = extension.ifBlank {
        mimeType
            ?.substringAfterLast('/')
            ?.uppercase()
            .orEmpty()
    }
    val size = sizeBytes?.let { formatFileSize(it) }
    // Разделитель — часть перевода (правило 6 localization.md); когда известна
    // только одна часть, разделителя нет — это разные ветки, а не joinToString.
    return when {
        size != null && type.isNotBlank() ->
            stringResource(Strings.string.supportchat_chat_label_document_meta, size, type)

        size != null -> size

        else -> type
    }
}

// Единицы и десятичный разделитель — часть перевода (`%1$s.%2$s MB` / `%1$s,%2$s МБ`), в Kotlin только арифметика.
@Composable
private fun formatFileSize(sizeBytes: Long): String {
    val kilobyte = 1_024L
    val megabyte = kilobyte * 1_024L
    val gigabyte = megabyte * 1_024L
    return when {
        sizeBytes < kilobyte -> stringResource(Strings.string.supportchat_chat_label_size_bytes, sizeBytes)
        sizeBytes < megabyte -> stringResource(Strings.string.supportchat_chat_label_size_kb, sizeBytes / kilobyte)
        sizeBytes < gigabyte -> formatWithTenths(sizeBytes, megabyte, Strings.string.supportchat_chat_label_size_mb)
        else -> formatWithTenths(sizeBytes, gigabyte, Strings.string.supportchat_chat_label_size_gb)
    }
}

@Composable
private fun formatWithTenths(value: Long, unit: Long, resource: StringResource): String {
    val tenths = value * TENTHS_PER_UNIT / unit
    return stringResource(resource, tenths / TENTHS_PER_UNIT, tenths % TENTHS_PER_UNIT)
}
