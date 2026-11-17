package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_error_chat_auth
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_msg_reconnecting
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_title_connecting
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpRadius
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSize
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.effect.cmpGlassSurface
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.progress.CmpCircularProgressIndicator
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import ru.alarmtrade.specmp.core.ui.view.text.CmpTextStyle
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatConnectionBanner

/**
 * Короткие обрывы не показываются: плашка появляется, только если проблема держится дольше [SHOW_DELAY];
 * скрывается сразу после восстановления.
 */
@Composable
internal fun SupportChatConnectionStatus(banner: SupportChatConnectionBanner?, modifier: Modifier = Modifier) {
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
        ConnectionPill(current = shown ?: SupportChatConnectionBanner.Reconnecting)
    }
}

@Composable
private fun ConnectionPill(current: SupportChatConnectionBanner) {
    val colors = CmpTheme.colors
    val isProblem = current != SupportChatConnectionBanner.Connecting
    val contentColor = if (isProblem) colors.errorDefault else colors.foregroundOnDarkSecondaryDefault
    val shape = RoundedCornerShape(CmpRadius.round)

    // Плашка лежит поверх ленты: непрозрачная подложка страницы, иначе сообщения просвечивают сквозь стекло.
    CmpRow(
        modifier = Modifier
            .background(colors.backgroundPagePrimary, shape)
            .cmpGlassSurface(state = null, shape = shape)
            .background(if (isProblem) colors.errorHighlight else Color.Transparent, shape)
            .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space8),
        horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isProblem) {
            CmpIcon(
                icon = CmpIcons.WifiOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(CmpSize.iconS),
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
            style = CmpTextStyle.Paragraph3,
            color = contentColor,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        // Попыток при AuthFailed нет — и спиннера нет.
        if (current != SupportChatConnectionBanner.AuthFailed) {
            CmpCircularProgressIndicator(
                modifier = Modifier.size(CmpSize.iconS),
                strokeWidth = PROGRESS_STROKE,
                color = contentColor,
            )
        }
    }
}

private val SHOW_DELAY = 1_500.milliseconds
private val PROGRESS_STROKE = 2.dp
private const val VISIBILITY_LABEL = "connection_status"
