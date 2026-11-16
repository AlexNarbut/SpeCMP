package ru.alarmtrade.specmp.core.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Точка входа из `AppDelegate`: до старта Koin (он поднимается в композиции), как `PushOpenRequests`. */
object IosPush {

    private val _client = MutableStateFlow<IosMessagingClient?>(null)
    internal val client: StateFlow<IosMessagingClient?> = _client.asStateFlow()

    fun install(client: IosMessagingClient) {
        _client.update { client }
    }

    fun onNotificationOpened(userInfo: Map<Any?, *>) {
        PushOpenRequests.offer(ApsUserInfo.data(userInfo))
    }

    /** Push без `aps.alert` система не показывает — показываем сами; с alert — уже показан, второй не нужен. */
    fun onRemoteMessage(userInfo: Map<Any?, *>) {
        if (ApsUserInfo.hasAlert(userInfo)) return
        val data = ApsUserInfo.data(userInfo)
        PushPayload.parse(data)?.let { IosPushNotifier.show(it, data) }
    }
}
