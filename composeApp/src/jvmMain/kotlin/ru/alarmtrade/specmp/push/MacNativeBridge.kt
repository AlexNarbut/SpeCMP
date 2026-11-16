package ru.alarmtrade.specmp.push

import co.touchlab.kermit.Logger
import com.sun.jna.Native
import java.awt.EventQueue
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.push.DesktopPush

/**
 * `libspecmp_notifications.dylib` из `compose.application.resources.dir`: баннеры и push macOS. Только в собранном
 * `.app` — из `gradle run` и не на macOS [library] остаётся `null`.
 */
object MacNativeBridge {

    private const val LIBRARY_FILE = "libspecmp_notifications.dylib"
    private const val FIREBASE_CONFIG = "GoogleService-Info.plist"

    private val logger = Logger.withTag(MacNativeBridge::class.simpleName.orEmpty())

    private val _library = MutableStateFlow<MacNativeLibrary?>(null)
    internal val library: MacNativeLibrary? get() = _library.value

    // Поля, а не лямбды в вызове: нативная сторона хранит указатели, а JNA не держит колбэки от сборщика мусора.
    private val click = MacNativeLibrary.ClickCallback { _, userInfo ->
        EventQueue.invokeLater {
            DesktopWindow.bringToFront()
            DesktopPush.onNotificationOpened(userInfo.toUserInfo())
        }
    }
    private val present = MacNativeLibrary.PresentCallback { userInfo ->
        if (DesktopPush.shouldPresent(userInfo.toUserInfo())) 1 else 0
    }
    private val message = MacNativeLibrary.StringCallback { userInfo ->
        EventQueue.invokeLater { DesktopPush.onRemoteMessage(userInfo.toUserInfo()) }
    }

    /** Из `main` до старта окна: клик по уведомлению, запустивший приложение, приходит сразу после запуска AppKit. */
    fun install() {
        val dir = System.getProperty("compose.application.resources.dir")?.let(::File) ?: return
        val file = File(dir, LIBRARY_FILE).takeIf { it.isFile } ?: return
        val library = safeRun { Native.load(file.absolutePath, MacNativeLibrary::class.java) }
            .onError { logger.w(it) { "Native macOS library is unavailable" } }
            .getValueIfSuccess() ?: return
        if (library.specmp_notifications_install(click, present) != 1) return
        _library.update { library }
        File(dir, FIREBASE_CONFIG).takeIf { it.isFile }
            ?.let { DesktopPush.install(MacMessagingClient(library, it, message)) }
    }

    private fun String?.toUserInfo(): Map<String, Any?> = this
        ?.let { json -> safeRun { Json.parseToJsonElement(json) as? JsonObject }.getValueIfSuccess() }
        ?.toPlain()
        .orEmpty()

    private fun JsonObject.toPlain(): Map<String, Any?> = mapValues { (_, value) ->
        when (value) {
            is JsonObject -> value.toPlain()
            is JsonPrimitive -> value.contentOrNull
            else -> value.toString()
        }
    }
}

internal fun Map<String, String>.toUserInfoJson(): String = JsonObject(mapValues { JsonPrimitive(it.value) }).toString()
