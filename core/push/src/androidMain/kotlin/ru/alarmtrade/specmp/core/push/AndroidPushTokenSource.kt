package ru.alarmtrade.specmp.core.push

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import co.touchlab.kermit.Logger
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun

/** Токен push-сервиса магазина (`core/push-fcm`, `core/push-hms`); каналы и logout — общие. */
abstract class AndroidPushTokenSource(protected val context: Context) : PushTokenSource {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    protected abstract suspend fun requestToken(): String?

    protected abstract suspend fun deleteToken()

    // Каналы — на старте главного (вызов отсюда), чтобы push, показанный самим push-сервисом, попал в наш канал.
    final override suspend fun token(): Response<String?> = safeRun {
        PushChannel.ensureAll(context)
        requestToken()
    }

    final override suspend fun release() {
        PushOpenRequests.clear()
        NotificationManagerCompat.from(context).cancelAll()
        safeRun { deleteToken() }.onError { logger.w(it) { "Push token is not deleted" } }
    }
}
