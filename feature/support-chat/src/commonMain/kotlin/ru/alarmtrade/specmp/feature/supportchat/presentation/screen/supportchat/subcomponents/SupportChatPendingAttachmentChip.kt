package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpIconButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatPendingAttachment
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.support_chat_a11y_remove_attachment

@Composable
internal fun SupportChatPendingAttachmentChip(
    attachment: SupportChatPendingAttachment,
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
                icon = when (attachment) {
                    is SupportChatPendingAttachment.Image -> CmpIcons.Image
                    is SupportChatPendingAttachment.Document -> CmpIcons.Description
                },
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )

            CmpText(
                text = attachment.name,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            CmpIconButton(
                onClick = onRemove,
                icon = CmpIcons.Close,
                contentDescription = stringResource(Res.string.support_chat_a11y_remove_attachment),
            )
        }
    }
}
