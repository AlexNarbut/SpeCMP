package ru.alarmtrade.specmp.push

/**
 * Баннер macOS от имени приложения (`UNUserNotificationCenter`) через [MacNativeBridge]. Работает только
 * в собранном `.app`: из `gradle run` [create] вернёт `null`.
 */
internal class MacNativeNotifications private constructor(private val library: MacNativeLibrary) {

    /**
     * Уведомления не дойдут (запрещены или выключен показ): системный запрос повторно не появится — только
     * настройки.
     */
    val isDenied: Boolean get() = library.specmp_notifications_status() == STATUS_DENIED

    fun openSettings() {
        library.specmp_notifications_open_settings()
    }

    /** [data] — данные push для клика (`PushOpenRequests`). */
    fun show(id: String, title: String, body: String, data: Map<String, String>) {
        library.specmp_notifications_show(id, title, body, data.toUserInfoJson())
    }

    fun removeAll() {
        library.specmp_notifications_remove_all()
    }

    companion object {
        private const val STATUS_DENIED = 1

        // Заодно запрашивает разрешение — системное окно появится, только если пользователь ещё не решал.
        fun create(): MacNativeNotifications? = MacNativeBridge.library?.let { library ->
            library.specmp_notifications_request_authorization()
            MacNativeNotifications(library)
        }
    }
}
