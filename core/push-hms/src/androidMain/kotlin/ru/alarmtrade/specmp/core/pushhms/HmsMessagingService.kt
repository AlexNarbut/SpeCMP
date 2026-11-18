package ru.alarmtrade.specmp.core.pushhms

import android.os.Bundle
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.coroutines.serviceScope
import ru.alarmtrade.specmp.core.push.AndroidPushReceiver

class HmsMessagingService : HmsMessageService() {

    // HMS зовёт onMessageReceived на main-потоке (onStartCommand), а показ грузит картинку по сети.
    override fun onMessageReceived(message: RemoteMessage?) {
        message ?: return
        val context = applicationContext
        scope.launch {
            AndroidPushReceiver.show(
                context = context,
                data = message.dataOfMap,
                title = message.notification?.title,
                body = message.notification?.body,
                messageKey = message.messageId ?: message.sentTime.toString(),
            )
        }
    }

    // Токен уходит на сервер при старте главного экрана; здесь — только для getToken на EMUI до 10.
    override fun onNewToken(token: String?) = HmsTokenUpdates.offer(token)

    override fun onNewToken(token: String?, bundle: Bundle?) = HmsTokenUpdates.offer(token)

    private companion object {
        val scope = serviceScope(AppDispatchers().io, HmsMessagingService::class.simpleName.orEmpty())
    }
}
