package ru.alarmtrade.specmp.core.push

import android.content.Context
import kotlinx.coroutines.runBlocking

/** Приём push сервисом магазина: показ без Koin и SDK — процесс мог быть разбужен push-сервисом без UI. */
object AndroidPushReceiver {

    // Как в старом приложении: новое сообщение чата заменяет прежнее уведомление.
    private const val CHAT_NOTIFICATION_ID = 12345

    // Рабочий поток сервиса: показ должен завершиться до выхода из метода, иначе процесс могут остановить.
    fun onMessage(context: Context, data: Map<String, String>, title: String?, body: String?, messageKey: String) {
        val payload = PushPayload.parse(data, title, body) ?: return
        val notificationId = if (payload.type == PushType.CHAT) CHAT_NOTIFICATION_ID else messageKey.hashCode()
        runBlocking { AndroidPushNotifier(context.applicationContext).show(payload, notificationId) }
    }
}
