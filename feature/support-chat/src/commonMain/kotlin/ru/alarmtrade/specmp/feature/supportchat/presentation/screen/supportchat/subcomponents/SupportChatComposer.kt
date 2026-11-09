package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_attach_file
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_attach_photo
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_send
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_message_hint
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextFieldStyle
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpClickableSurface
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment

@Composable
internal fun SupportChatComposer(
    draftText: String,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachFile: () -> Unit,
    onAttachPhoto: () -> Unit,
    pendingAttachments: ImmutableList<SupportChatUiAttachment>,
    onRemoveAttachment: (String) -> Unit,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // Владелец черновика — composer, поэтому «есть что отправить» считается
    // здесь; доступность ввода (только Online) приходит из state единственным источником.
    val canSend = enabled && (draftText.isNotBlank() || pendingAttachments.isNotEmpty())

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (pendingAttachments.isNotEmpty()) {
            PendingAttachmentsRow(
                attachments = pendingAttachments,
                onRemove = onRemoveAttachment,
                colors = colors,
            )
        }

        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            CmpIconButton(
                onClick = onAttachFile,
                icon = CmpIcons.AttachFile,
                contentDescription = stringResource(Strings.string.supportchat_chat_a11y_attach_file),
                enabled = enabled,
            )

            CmpIconButton(
                onClick = onAttachPhoto,
                icon = CmpIcons.Image,
                contentDescription = stringResource(Strings.string.supportchat_chat_a11y_attach_photo),
                enabled = enabled,
            )

            CmpTextField(
                value = draftText,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .onPreviewKeyEvent { event ->
                        val isPlainEnter = event.type == KeyEventType.KeyDown &&
                            event.key == Key.Enter &&
                            !event.isShiftPressed
                        if (isPlainEnter && canSend) {
                            onSend()
                            true
                        } else {
                            false
                        }
                    },
                placeholder = stringResource(Strings.string.supportchat_chat_label_message_hint),
                enabled = enabled,
                singleLine = false,
                maxLines = 4,
                style = CmpTextFieldStyle.Filled,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (canSend) onSend()
                    },
                ),
            )

            SendButton(
                onClick = onSend,
                enabled = canSend,
                colors = colors,
            )
        }
    }
}

@Composable
private fun PendingAttachmentsRow(
    attachments: ImmutableList<SupportChatUiAttachment>,
    onRemove: (String) -> Unit,
    colors: CmpThemeColors,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(
            items = attachments,
            key = { attachment -> attachment.id },
        ) { attachment ->
            SupportChatPendingAttachmentChip(
                attachment = attachment,
                onRemove = { onRemove(attachment.id) },
                colors = colors,
            )
        }
    }
}

@Composable
private fun SendButton(onClick: () -> Unit, enabled: Boolean, colors: CmpThemeColors) {
    CmpClickableSurface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) colors.primary else colors.onSurface.copy(alpha = 0.1f),
        contentColor = if (enabled) colors.onPrimary else colors.onSurface.copy(alpha = 0.38f),
        modifier = Modifier.size(48.dp),
        enforceMinimumTouchTarget = false,
    ) {
        CmpBox(contentAlignment = Alignment.Center) {
            CmpIcon(
                icon = CmpIcons.Send,
                contentDescription = stringResource(Strings.string.supportchat_chat_a11y_send),
            )
        }
    }
}
