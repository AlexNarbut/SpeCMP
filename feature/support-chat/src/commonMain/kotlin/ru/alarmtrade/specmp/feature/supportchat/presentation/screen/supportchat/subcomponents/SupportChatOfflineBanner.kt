package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.ui.theme.CmpThemeColors
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcon
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpColumn
import ru.alarmtrade.specmp.core.ui.view.layout.CmpRow
import ru.alarmtrade.specmp.core.ui.view.surface.CmpSurface
import ru.alarmtrade.specmp.core.ui.view.text.CmpText
import specmp.feature.support_chat.generated.resources.Res
import specmp.feature.support_chat.generated.resources.supportchat_common_btn_retry
import specmp.feature.support_chat.generated.resources.supportchat_chat_banner_offline_subtitle
import specmp.feature.support_chat.generated.resources.supportchat_chat_banner_offline_title

@Composable
internal fun SupportChatOfflineBanner(
    onRetry: () -> Unit,
    colors: CmpThemeColors,
    modifier: Modifier = Modifier,
) {
    CmpSurface(
        modifier = modifier.fillMaxWidth(),
        color = colors.errorContainer,
        contentColor = colors.onErrorContainer,
    ) {
        CmpRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CmpIcon(
                icon = CmpIcons.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )

            CmpColumn(modifier = Modifier.weight(1f)) {
                CmpText(
                    text = stringResource(Res.string.supportchat_chat_banner_offline_title),
                    fontWeight = FontWeight.Medium,
                )
                CmpText(text = stringResource(Res.string.supportchat_chat_banner_offline_subtitle))
            }

            CmpButton(
                text = stringResource(Res.string.supportchat_common_btn_retry),
                onClick = onRetry,
            )
        }
    }
}
