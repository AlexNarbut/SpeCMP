package ru.alarmtrade.specmp.core.pushfcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking
import ru.alarmtrade.specmp.core.push.AndroidPushReceiver

class FcmMessagingService : FirebaseMessagingService() {

    // Рабочий поток FCM: показ должен завершиться до выхода из метода, иначе процесс могут остановить.
    override fun onMessageReceived(message: RemoteMessage) = runBlocking {
        AndroidPushReceiver.show(
            context = applicationContext,
            data = message.data,
            title = message.notification?.title,
            body = message.notification?.body,
            messageKey = message.messageId ?: message.sentTime.toString(),
        )
    }

    // Новый токен уходит на сервер при следующем старте главного экрана (RegisterPushTokenUseCase): здесь нет SDK.
    override fun onNewToken(token: String) = Unit
}
