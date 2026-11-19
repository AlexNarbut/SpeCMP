package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_attach
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_send
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_btn_attach_file
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_btn_attach_image
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_label_message_hint
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_subtitle_attach_file
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_subtitle_attach_image
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.button.CmpFabSize
import ru.alarmtrade.specmp.core.ui.view.button.CmpFloatingActionButton
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.input.CmpTextField
import ru.alarmtrade.specmp.core.ui.view.input.keepFocusOnTap
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.menu.CmpDropdownMenu
import ru.alarmtrade.specmp.core.ui.view.surface.CmpListItem
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
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
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    // Владелец черновика — composer, поэтому «есть что отправить» считается
    // здесь; доступность ввода (только Online) приходит из state единственным источником.
    val canSend = enabled && (draftText.isNotBlank() || pendingAttachments.isNotEmpty())
    // Кнопки 48 у многострочного поля (от 56) — по центру его первой строки.
    val sideButtonModifier = Modifier.padding(bottom = CmpSpacing.space4)

    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
    ) {
        if (pendingAttachments.isNotEmpty()) {
            PendingAttachmentsRow(
                attachments = pendingAttachments,
                onRemove = onRemoveAttachment,
            )
        }

        CmpRow(
            modifier = Modifier
                .fillMaxWidth()
                .keepFocusOnTap(),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            verticalAlignment = Alignment.Bottom,
        ) {
            SupportChatAttachMenu(
                onAttachFile = onAttachFile,
                onAttachPhoto = onAttachPhoto,
                enabled = enabled,
                modifier = sideButtonModifier,
            )

            CmpTextField(
                value = draftText,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
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
                valueStyle = CmpTextStyle.Paragraph2,
                enabled = enabled,
                singleLine = false,
                maxLines = MAX_INPUT_LINES,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (canSend) onSend()
                    },
                ),
            )

            CmpFloatingActionButton(
                onClick = onSend,
                modifier = sideButtonModifier,
                size = CmpFabSize.Small,
                enabled = canSend,
            ) {
                CmpIcon(
                    icon = CmpIcons.Send,
                    contentDescription = stringResource(Strings.string.supportchat_chat_a11y_send),
                    modifier = Modifier.size(CmpSize.iconM),
                )
            }
        }
    }
}

@Composable
private fun SupportChatAttachMenu(
    onAttachFile: () -> Unit,
    onAttachPhoto: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        CmpIconButton(
            onClick = { expanded = !expanded },
            icon = CmpIcons.AttachFile,
            contentDescription = stringResource(Strings.string.supportchat_chat_a11y_attach),
            enabled = enabled,
            selected = expanded && enabled,
        )
        CmpDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            CmpListItem(
                flat = true,
                icon = CmpIcons.Image,
                title = stringResource(Strings.string.supportchat_chat_btn_attach_image),
                subtitle = stringResource(Strings.string.supportchat_chat_subtitle_attach_image),
                onClick = {
                    expanded = false
                    onAttachPhoto()
                },
                trailing = {},
            )
            CmpListItem(
                flat = true,
                icon = CmpIcons.Description,
                title = stringResource(Strings.string.supportchat_chat_btn_attach_file),
                subtitle = stringResource(Strings.string.supportchat_chat_subtitle_attach_file),
                onClick = {
                    expanded = false
                    onAttachFile()
                },
                trailing = {},
            )
        }
    }
}

@Composable
private fun PendingAttachmentsRow(attachments: ImmutableList<SupportChatUiAttachment>, onRemove: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(
            items = attachments,
            key = { attachment -> attachment.id },
        ) { attachment ->
            SupportChatPendingAttachmentChip(
                attachment = attachment,
                onRemove = { onRemove(attachment.id) },
            )
        }
    }
}

private const val MAX_INPUT_LINES = 4
