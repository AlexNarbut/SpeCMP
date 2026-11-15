package ru.alarmtrade.specmp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import org.jetbrains.compose.resources.getString
import org.koin.compose.koinInject
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.push_notification_msg_attachment
import ru.alarmtrade.specmp.core.localization.resources.push_notification_title_channel_chat
import ru.alarmtrade.specmp.feature.supportchat.domain.model.SupportChatEvent
import ru.alarmtrade.specmp.feature.supportchat.domain.repository.SupportChatRepository
import ru.alarmtrade.specmp.push.DesktopChatNotifier

@Composable
internal actual fun ChatNotificationsEffect(isChatOnTop: Boolean) {
    val repository = koinInject<SupportChatRepository>()
    val chatOnTop by rememberUpdatedState(isChatOnTop)
    LaunchedEffect(repository) {
        val notifier = DesktopChatNotifier(
            title = getString(Strings.string.push_notification_title_channel_chat),
            isChatVisible = { chatOnTop },
        ).apply { start() }
        val attachment = getString(Strings.string.push_notification_msg_attachment)
        try {
            // Сессия чата — с подсчётом подписчиков в SDK: экран чата и уведомления держат одно соединение.
            repository.session().collect { response ->
                val message = ((response as? Response.Success)?.value as? SupportChatEvent.MessageReceived)?.message
                if (message != null && !message.isMine) {
                    notifier.show(message.text?.takeIf { it.isNotBlank() } ?: attachment)
                }
            }
        } finally {
            notifier.dispose()
        }
    }
}
