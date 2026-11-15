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
        PushOpenRequests.offer(userInfo.pushData())
    }

    /** Push без `aps.alert` система не показывает — показываем сами; с alert — уже показан, второй не нужен. */
    fun onRemoteMessage(userInfo: Map<Any?, *>) {
        if ((userInfo[KEY_APS] as? Map<*, *>)?.get(KEY_ALERT) != null) return
        val data = userInfo.pushData()
        PushPayload.parse(data)?.let { IosPushNotifier.show(it, data) }
    }

    private fun Map<Any?, *>.pushData(): Map<String, String> = entries
        .mapNotNull { (key, value) -> (key as? String)?.let { k -> value?.toString()?.let { k to it } } }
        .filter { (key, _) -> key != KEY_APS }
        .toMap()

    private const val KEY_APS = "aps"
    private const val KEY_ALERT = "alert"
}
