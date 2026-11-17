package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_error_chat_auth
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_msg_reconnecting
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_title_connecting
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnectionBanner

/**
 * Короткие обрывы не показываются: плашка появляется, только если проблема держится дольше [SHOW_DELAY];
 * скрывается сразу после восстановления.
 */
@Composable
internal fun SupportChatConnectionStatus(
    banner: SupportChatConnectionBanner?,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    var shown by remember { mutableStateOf<SupportChatConnectionBanner?>(null) }
    LaunchedEffect(banner) {
        if (banner == null) {
            shown = null
        } else {
            // Уже показана — меняем содержимое без повторной задержки.
            if (shown == null) delay(SHOW_DELAY)
            shown = banner
        }
    }

    AnimatedVisibility(
        visible = shown != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
        label = VISIBILITY_LABEL,
    ) {
        val current = shown ?: SupportChatConnectionBanner.Reconnecting
        val isProblem = current != SupportChatConnectionBanner.Connecting
        CmpSurface(
            shape = RoundedCornerShape(percent = 50),
            color = if (isProblem) colors.errorContainer else colors.surfaceContainerHigh,
            contentColor = if (isProblem) colors.onErrorContainer else colors.onSurfaceVariant,
            shadowElevation = 2.dp,
        ) {
            CmpRow(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isProblem) {
                    CmpIcon(
                        icon = CmpIcons.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
                CmpText(
                    text = stringResource(
                        when (current) {
                            SupportChatConnectionBanner.Connecting -> Strings.string.supportchat_chat_title_connecting
                            SupportChatConnectionBanner.Reconnecting -> Strings.string.supportchat_chat_msg_reconnecting
                            SupportChatConnectionBanner.AuthFailed -> Strings.string.supportchat_chat_error_chat_auth
                        },
                    ),
                    style = CmpTextStyle.Paragraph2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                // Попыток при AuthFailed нет — и спиннера нет.
                if (current != SupportChatConnectionBanner.AuthFailed) {
                    CmpCircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = if (isProblem) colors.onErrorContainer else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val SHOW_DELAY = 1_500.milliseconds
private const val VISIBILITY_LABEL = "connection_status"
