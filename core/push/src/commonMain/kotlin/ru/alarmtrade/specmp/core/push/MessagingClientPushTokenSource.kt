package ru.alarmtrade.specmp.core.push

import co.touchlab.kermit.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun

/** Токен через [PushMessagingClient] платформы; клиента нет (не поставлен, не macOS) — `null`, как без push. */
internal class MessagingClientPushTokenSource(
    private val client: () -> PushMessagingClient?,
    private val removeDelivered: () -> Unit,
) : PushTokenSource {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun token(): Response<String?> = safeRun {
        val client = client() ?: return@safeRun null
        suspendCancellableCoroutine { continuation ->
            client.fetchToken { token, error ->
                if (error != null) {
                    continuation.resumeWithException(IllegalStateException("FCM token: $error"))
                } else {
                    continuation.resume(token)
                }
            }
        }
    }

    override suspend fun release() {
        PushOpenRequests.clear()
        removeDelivered()
        val client = client() ?: return
        safeRun {
            suspendCancellableCoroutine { continuation ->
                client.deleteToken { error ->
                    if (error != null) {
                        continuation.resumeWithException(IllegalStateException("FCM token delete: $error"))
                    } else {
                        continuation.resume(Unit)
                    }
                }
            }
        }.onError { logger.w(it) { "Push token is not deleted" } }
    }
}
