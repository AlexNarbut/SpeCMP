package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_a11y_remove_attachment
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatUiAttachment

@Composable
internal fun SupportChatPendingAttachmentChip(
    attachment: SupportChatUiAttachment,
    onRemove: () -> Unit,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    CmpSurface(
        modifier = modifier,
        shape = RoundedCornerShape(percent = 50),
        color = colors.surfaceVariant,
        contentColor = colors.onSurface,
    ) {
        CmpRow(
            modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = when (attachment.kind) {
                    SupportChatUiAttachment.Kind.Image -> CmpIcons.Image
                    SupportChatUiAttachment.Kind.Document -> CmpIcons.Description
                },
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )

            // Не weight: в элементе LazyRow ширина не ограничена, и weight(fill = false)
            // схлопывал текст в ноль — имя файла не было видно.
            CmpText(
                text = attachment.name,
                modifier = Modifier.widthIn(max = CHIP_NAME_MAX_WIDTH),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            CmpIconButton(
                onClick = onRemove,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Strings.string.supportchat_chat_a11y_remove_attachment),
            )
        }
    }
}

private val CHIP_NAME_MAX_WIDTH = 180.dp
