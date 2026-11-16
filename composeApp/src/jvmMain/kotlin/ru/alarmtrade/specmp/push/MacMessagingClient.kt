package ru.alarmtrade.specmp.push

import co.touchlab.kermit.Logger
import com.sun.jna.Callback
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import ru.alarmtrade.specmp.core.push.DesktopMessagingClient
import ru.alarmtrade.specmp.core.push.PushPayload

/** Firebase настраивается при первом запросе токена: колбэки APNs ставятся в делегат NSApp, а его создаёт окно AWT. */
internal class MacMessagingClient(
    private val library: MacNativeLibrary,
    private val firebaseConfig: File,
    private val onMessage: MacNativeLibrary.StringCallback,
) : DesktopMessagingClient {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val configured = AtomicBoolean(false)

    // Одноразовые колбэки живут до ответа: иначе сборщик мусора освободит их раньше, чем ответит Firebase.
    private val pending: MutableSet<Callback> = ConcurrentHashMap.newKeySet()

    override fun fetchToken(completion: (token: String?, error: String?) -> Unit) {
        configure()
        val callback = object : MacNativeLibrary.TokenCallback {
            override fun invoke(token: String?, error: String?) {
                pending.remove(this)
                completion(token, error)
            }
        }
        pending.add(callback)
        library.specmp_push_token(callback)
    }

    override fun deleteToken(completion: (error: String?) -> Unit) {
        configure()
        val callback = object : MacNativeLibrary.StringCallback {
            override fun invoke(value: String?) {
                pending.remove(this)
                completion(value)
            }
        }
        pending.add(callback)
        library.specmp_push_delete_token(callback)
    }

    override fun show(id: String, payload: PushPayload, data: Map<String, String>) {
        library.specmp_notifications_show(id, payload.title.orEmpty(), payload.body.orEmpty(), data.toUserInfoJson())
    }

    override fun removeAll() {
        library.specmp_notifications_remove_all()
    }

    private fun configure() {
        if (!configured.compareAndSet(false, true)) return
        if (library.specmp_push_configure(firebaseConfig.absolutePath, onMessage) != 1) {
            logger.w { "Firebase is not configured from ${firebaseConfig.name}" }
        }
    }
}
