package ru.alarmtrade.specmp.core.pushfcm

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import ru.alarmtrade.specmp.core.push.AndroidPushTokenSource

internal class FcmPushTokenSource(context: Context) : AndroidPushTokenSource(context) {

    override suspend fun requestToken(): String? = FirebaseMessaging.getInstance().token.await()

    override suspend fun deleteToken() {
        FirebaseMessaging.getInstance().deleteToken().await()
    }
}
