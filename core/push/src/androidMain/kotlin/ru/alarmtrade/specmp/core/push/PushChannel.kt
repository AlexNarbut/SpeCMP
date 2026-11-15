package ru.alarmtrade.specmp.core.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import ru.alarmtrade.specmp.core.localization.prepareAppLocale
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.push_notification_btn_open
import ru.alarmtrade.specmp.core.localization.resources.push_notification_btn_reply
import ru.alarmtrade.specmp.core.localization.resources.push_notification_title_channel_chat
import ru.alarmtrade.specmp.core.localization.resources.push_notification_title_channel_main
import ru.alarmtrade.specmp.core.localization.resources.push_notification_title_channel_warning

/** id каналов — как в старом приложении: при обновлении сохраняются настройки каналов пользователя. */
internal enum class PushChannel(
    val id: String,
    val title: StringResource,
    val icon: Int,
    val action: StringResource?,
) {
    MAIN(
        "FcmNotificationType_NOTIFICATION",
        Strings.string.push_notification_title_channel_main,
        R.drawable.ic_push_notification,
        Strings.string.push_notification_btn_open,
    ),
    WARNING(
        "FcmNotificationType_ATTENTION",
        Strings.string.push_notification_title_channel_warning,
        R.drawable.ic_push_warning,
        null,
    ),
    CHAT(
        "FcmNotificationType_SUPPORT",
        Strings.string.push_notification_title_channel_chat,
        R.drawable.ic_push_chat,
        Strings.string.push_notification_btn_reply,
    ),
    ;

    companion object {
        fun of(type: PushType): PushChannel = when (type) {
            PushType.NOTIFICATION -> MAIN
            PushType.WARNING -> WARNING
            PushType.CHAT -> CHAT
        }

        /**
         * Создать или переименовать каналы на языке приложения. Процесс, разбуженный FCM, мог не пройти через
         * активити — язык для compose-resources ставим сами.
         */
        suspend fun ensureAll(context: Context) {
            prepareAppLocale()
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            entries.forEach { channel ->
                manager.createNotificationChannel(
                    NotificationChannel(channel.id, getString(channel.title), NotificationManager.IMPORTANCE_HIGH),
                )
            }
        }
    }
}
