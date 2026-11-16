package ru.alarmtrade.specmp.core.push

import co.touchlab.kermit.Logger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun

internal class IosPushTokenSource : PushTokenSource {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun token(): Response<String?> = safeRun {
        val client = IosPush.client.value ?: return@safeRun null
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
        IosPushNotifier.removeAll()
        val client = IosPush.client.value ?: return
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
