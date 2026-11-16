package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import org.jetbrains.compose.resources.stringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.push_notification_msg_attachment
import ru.alarmtrade.specmp.core.localization.resources.push_notification_title_channel_chat
import ru.alarmtrade.specmp.feature.supportchat.presentation.navigation.SupportChatNotificationsEntry
import ru.alarmtrade.specmp.push.DesktopChatNotifier

@Composable
internal actual fun ChatNotificationsEffect(isChatOnTop: Boolean) {
    val chatOnTop by rememberUpdatedState(isChatOnTop)
    val title = stringResource(Strings.string.push_notification_title_channel_chat)
    val attachment = stringResource(Strings.string.push_notification_msg_attachment)
    val notifier = remember { DesktopChatNotifier(title, attachment, isChatVisible = { chatOnTop }) }
    SupportChatNotificationsEntry(notifier)
}
