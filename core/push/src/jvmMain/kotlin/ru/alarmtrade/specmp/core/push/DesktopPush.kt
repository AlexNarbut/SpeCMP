package ru.alarmtrade.specmp.core.push

import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Push на macOS (APNs через FCM), точка входа нативного моста — из `main` до Koin, как [PushOpenRequests].
 * Пока приложение открыто, о сообщениях чата сообщает `DesktopChatNotifier` по живому соединению — push чата
 * тогда не показываем, иначе баннера два.
 */
object DesktopPush {

    private val _client = MutableStateFlow<DesktopMessagingClient?>(null)
    internal val client: StateFlow<DesktopMessagingClient?> = _client.asStateFlow()

    fun install(client: DesktopMessagingClient) {
        _client.update { client }
    }

    fun onNotificationOpened(userInfo: Map<*, *>) {
        PushOpenRequests.offer(ApsUserInfo.data(userInfo))
    }

    /** Push с `aps.alert` при открытом приложении: показать ли системный баннер. */
    fun shouldPresent(userInfo: Map<*, *>): Boolean =
        PushPayload.typeOf(ApsUserInfo.data(userInfo)).let { it != null && it != PushType.CHAT }

    /** Push без `aps.alert` система не показывает — показываем сами. */
    fun onRemoteMessage(userInfo: Map<*, *>) {
        if (ApsUserInfo.hasAlert(userInfo)) return
        val data = ApsUserInfo.data(userInfo)
        val payload = PushPayload.parse(data)?.takeIf { it.type != PushType.CHAT } ?: return
        client.value?.show(data.notificationId(), payload, data)
    }

    private fun Map<String, String>.notificationId(): String = this[KEY_MESSAGE_ID] ?: UUID.randomUUID().toString()

    private const val KEY_MESSAGE_ID = "gcm.message_id"
}
