package ru.alarmtrade.specmp.push

import co.touchlab.kermit.Logger
import com.sun.jna.Callback
import com.sun.jna.Library
import com.sun.jna.Native
import java.io.File
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun

/**
 * Баннер macOS от имени приложения (`UNUserNotificationCenter`) через `libspecmp_notifications.dylib`
 * (`desktopApp/native/macos`). Работает только в собранном `.app`: из `gradle run` [create] вернёт `null`.
 */
internal class MacNativeNotifications private constructor(private val library: NotificationsLibrary) {

    /**
     * Уведомления не дойдут (запрещены или выключен показ): системный запрос повторно не появится — только
     * настройки.
     */
    val isDenied: Boolean get() = library.specmp_notifications_status() == STATUS_DENIED

    fun openSettings() {
        library.specmp_notifications_open_settings()
    }

    fun show(id: String, title: String, body: String) {
        library.specmp_notifications_show(id, title, body)
    }

    fun removeAll() {
        library.specmp_notifications_remove_all()
    }

    @Suppress("FunctionNaming", "FunctionName")
    private interface NotificationsLibrary : Library {
        fun specmp_notifications_start(onClick: ClickCallback?): Int

        fun specmp_notifications_status(): Int

        fun specmp_notifications_open_settings()

        fun specmp_notifications_show(id: String, title: String, body: String)

        fun specmp_notifications_remove_all()
    }

    fun interface ClickCallback : Callback {
        fun invoke(id: String?)
    }

    companion object {
        private const val LIBRARY_FILE = "libspecmp_notifications.dylib"
        private const val STATUS_DENIED = 1

        private val logger = Logger.withTag(MacNativeNotifications::class.simpleName.orEmpty())

        // Колбэк держит сильная ссылка вызывающего: JNA не держит его сама, а сборщик мусора сломал бы клик.
        // Заодно запрашивает разрешение — системное окно появится, только если пользователь ещё не решал.
        fun create(onClick: ClickCallback): MacNativeNotifications? {
            val file = System.getProperty("compose.application.resources.dir")
                ?.let { File(it, LIBRARY_FILE) }
                ?.takeIf { it.isFile } ?: return null
            return safeRun {
                val library = Native.load(file.absolutePath, NotificationsLibrary::class.java)
                if (library.specmp_notifications_start(onClick) == 1) MacNativeNotifications(library) else null
            }.onError { logger.w(it) { "Native macOS notifications are unavailable" } }.getValueIfSuccess()
        }
    }
}
