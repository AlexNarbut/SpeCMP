package ru.alarmtrade.specmp.push

import ru.alarmtrade.specmp.core.push.IosMessagingClient
import ru.alarmtrade.specmp.core.push.IosPush

/** Реализует Swift (`FirebasePushMessaging`): типы `core/push` в framework не экспортируются без префикса модуля. */
interface IosFirebaseMessaging : IosMessagingClient

/** Вызовы из `AppDelegate` (Swift: `PushBridge.shared`). */
object PushBridge {

    fun install(messaging: IosFirebaseMessaging) {
        IosPush.install(messaging)
    }

    fun onNotificationOpened(userInfo: Map<Any?, *>) {
        IosPush.onNotificationOpened(userInfo)
    }

    fun onRemoteMessage(userInfo: Map<Any?, *>) {
        IosPush.onRemoteMessage(userInfo)
    }
}
