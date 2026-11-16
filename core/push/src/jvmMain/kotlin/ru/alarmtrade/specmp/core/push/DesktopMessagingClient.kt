package ru.alarmtrade.specmp.core.push

/** Firebase Messaging и баннеры macOS (`composeApp` jvmMain, нативная библиотека `desktopApp`). */
interface DesktopMessagingClient : PushMessagingClient {
    fun show(id: String, payload: PushPayload, data: Map<String, String>)

    fun removeAll()
}
