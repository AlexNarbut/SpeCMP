package ru.alarmtrade.specmp.core.push

import co.touchlab.kermit.Logger
import platform.Foundation.NSUUID
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

internal object IosPushNotifier {

    private const val CHAT_ID = "push_chat"

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    fun show(payload: PushPayload, data: Map<String, String>) {
        val content = UNMutableNotificationContent().apply {
            payload.title?.let { setTitle(it) }
            payload.body?.let { setBody(it) }
            setSound(UNNotificationSound.defaultSound())
            setThreadIdentifier(payload.type.name)
            setUserInfo(data.mapKeys { it.key as Any? })
        }
        // Чат — один идентификатор: новое сообщение заменяет прежнее, как на Android.
        val id = if (payload.type == PushType.CHAT) CHAT_ID else NSUUID().UUIDString
        UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(
            UNNotificationRequest.requestWithIdentifier(id, content, trigger = null),
        ) { error -> error?.let { logger.w { "Push is not shown: ${it.localizedDescription}" } } }
    }

    fun removeAll() {
        UNUserNotificationCenter.currentNotificationCenter().apply {
            removeAllDeliveredNotifications()
            removeAllPendingNotificationRequests()
        }
    }
}
