package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.supportchat_chat_error_history_title
import specmp.feature.support_chat.generated.resources.supportchat_common_btn_retry

/**
 * Полноэкранная ошибка первой загрузки истории: показывается ровно тогда,
 * когда ленты нет (history = LCEState.Error). Ошибки последующей пагинации
 * сюда не попадают — они уходят тостом, лента остаётся на экране.
 */
@Composable
internal fun SupportChatErrorState(onRetry: () -> Unit, colors: CmpThemeColors, modifier: Modifier = Modifier) {
    CmpBox(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CmpColumn(
            modifier = Modifier.padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CmpIcon(
                icon = CmpIcons.WifiOff,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(40.dp)
            )

            CmpText(
                text = stringResource(Res.string.supportchat_chat_error_history_title),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = colors.onSurface
            )

            CmpButton(
                text = stringResource(Res.string.supportchat_common_btn_retry),
                onClick = onRetry
            )
        }
    }
}
