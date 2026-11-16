package ru.alarmtrade.specmp.core.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking

class PushMessagingService : FirebaseMessagingService() {

    // Рабочий поток FCM: показ должен завершиться до выхода из метода, иначе процесс могут остановить.
    override fun onMessageReceived(message: RemoteMessage) {
        val payload = PushPayload.parse(message.data, message.notification?.title, message.notification?.body)
            ?: return
        val notificationId = if (payload.type == PushType.CHAT) CHAT_NOTIFICATION_ID else message.notificationId()
        runBlocking { AndroidPushNotifier(applicationContext).show(payload, notificationId) }
    }

    // Новый токен уходит на сервер при следующем старте главного экрана (RegisterPushTokenUseCase): здесь нет SDK.
    override fun onNewToken(token: String) = Unit

    private fun RemoteMessage.notificationId(): Int = (messageId ?: sentTime.toString()).hashCode()

    private companion object {
        // Как в старом приложении: новое сообщение чата заменяет прежнее уведомление.
        const val CHAT_NOTIFICATION_ID = 12345
    }
}
