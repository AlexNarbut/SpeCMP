package ru.alarmtrade.specmp.core.push

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.common.safeRun

internal class FirebasePushTokenSource(private val context: Context) : PushTokenSource {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    // Каналы — на старте главного (вызов отсюда), чтобы push, показанный самим FCM, попал в наш канал.
    override suspend fun token(): Response<String?> = safeRun {
        PushChannel.ensureAll(context)
        FirebaseMessaging.getInstance().token.await()
    }

    override suspend fun release() {
        PushOpenRequests.clear()
        NotificationManagerCompat.from(context).cancelAll()
        safeRun { FirebaseMessaging.getInstance().deleteToken().await() }
            .onError { logger.w(it) { "Push token is not deleted" } }
    }
}
