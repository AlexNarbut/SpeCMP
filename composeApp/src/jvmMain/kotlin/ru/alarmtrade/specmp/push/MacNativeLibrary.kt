package ru.alarmtrade.specmp.push

import com.sun.jna.Callback
import com.sun.jna.Library

/** C ABI `libspecmp_notifications.dylib` (`desktopApp/native/macos`). */
@Suppress("FunctionNaming", "FunctionName")
internal interface MacNativeLibrary : Library {
    fun specmp_notifications_install(onClick: ClickCallback, shouldPresent: PresentCallback): Int

    fun specmp_notifications_request_authorization()

    fun specmp_notifications_status(): Int

    fun specmp_notifications_open_settings()

    fun specmp_notifications_show(id: String, title: String, body: String, userInfoJson: String?)

    fun specmp_notifications_remove_all()

    fun specmp_push_configure(plistPath: String, onMessage: StringCallback): Int

    fun specmp_push_token(callback: TokenCallback)

    fun specmp_push_delete_token(callback: StringCallback)

    fun interface ClickCallback : Callback {
        fun invoke(id: String?, userInfoJson: String?)
    }

    fun interface PresentCallback : Callback {
        fun invoke(userInfoJson: String?): Int
    }

    fun interface StringCallback : Callback {
        fun invoke(value: String?)
    }

    fun interface TokenCallback : Callback {
        fun invoke(token: String?, error: String?)
    }
}
