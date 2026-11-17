package ru.alarmtrade.specmp.feature.supportchat.presentation.screen.supportchat.subcomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.supportchat_chat_error_history_title
import ru.alarmtrade.specmp.core.localization.resources.supportchat_common_btn_retry
import ru.alarmtrade.specmp.core.ui.error.message
import ru.alarmtrade.specmp.core.ui.view.button.CmpButton
import ru.alarmtrade.specmp.core.ui.view.icon.CmpIcons
import ru.alarmtrade.specmp.core.ui.view.layout.CmpBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateBox
import ru.alarmtrade.specmp.core.ui.view.state.CmpStateTone
import ru.alarmtrade.specmp.feature.supportchat.presentation.model.SupportChatError

/** Только когда ленты нет; ошибки последующей пагинации уходят тостом. */
@Composable
internal fun SupportChatErrorState(error: SupportChatError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val offline = error is SupportChatError.Common && error.error == ApiError.NoConnection
    CmpBox(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CmpStateBox(
            icon = if (offline) CmpIcons.WifiOff else CmpIcons.CloudOff,
            title = when (error) {
                is SupportChatError.Common -> error.error.message()

                SupportChatError.HistoryUnavailable -> stringResource(
                    Strings.string.supportchat_chat_error_history_title,
                )
            },
            tone = if (offline) CmpStateTone.Neutral else CmpStateTone.Error,
        ) {
            CmpButton(
                text = stringResource(Strings.string.supportchat_common_btn_retry),
                onClick = onRetry,
            )
        }
    }
}
