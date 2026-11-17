package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_msg_typing_author
import ru.alarmtrade.specmp.core.ui.theme.CmpTheme
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpBorderWidth
import ru.alarmtrade.specmp.core.ui.theme.tokens.CmpSpacing
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow

private const val TYPING_DOT_COUNT = 3

@Composable
internal fun SupportChatTypingIndicator(author: String, modifier: Modifier = Modifier) {
    val colors = CmpTheme.colors
    val shape = supportChatBubbleShape(isMine = false)
    CmpColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
    ) {
        // Одна фраза целиком в ресурсе (правило 6 localization.md):
        // порядок слов и разделитель — часть перевода.
        SupportChatAuthorLabel(text = stringResource(Strings.string.supportchat_chat_msg_typing_author, author))

        CmpRow(
            modifier = Modifier
                .background(colors.backgroundSurfaceCard, shape)
                .border(CmpBorderWidth.listItem, colors.borderHighlight, shape)
                .padding(horizontal = CmpSpacing.space16, vertical = CmpSpacing.space16),
            horizontalArrangement = Arrangement.spacedBy(CmpSpacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(TYPING_DOT_COUNT) { index ->
                TypingDot(
                    color = colors.foregroundOnDarkSecondaryDefault,
                    delayMillis = index * 180,
                )
            }
        }
    }
}

@Composable
private fun TypingDot(color: Color, delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing_dot")
    val alpha = transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delayMillis),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "typing_dot_alpha",
    )

    // Альфа читается в фазе draw: точки не рекомпозируются каждый кадр.
    CmpBox(
        modifier = Modifier
            .size(TYPING_DOT_SIZE)
            .drawBehind { drawCircle(color = color.copy(alpha = alpha.value)) },
    )
}

private val TYPING_DOT_SIZE = 8.dp
