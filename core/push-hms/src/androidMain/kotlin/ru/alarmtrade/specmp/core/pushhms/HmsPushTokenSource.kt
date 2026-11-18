package ru.alarmtrade.specmp.core.pushhms

import android.content.Context
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.push.HmsMessaging
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.push.AndroidPushTokenSource

internal class HmsPushTokenSource(context: Context, private val dispatchers: AppDispatchers) :
    AndroidPushTokenSource(context) {

    // getToken/deleteToken — блокирующие вызовы HMS Core: не на main.
    override suspend fun requestToken(): String? {
        val token = withContext(dispatchers.io) {
            HmsMessaging.getInstance(context).isAutoInitEnabled = true
            HmsInstanceId.getInstance(context).getToken(appId(), TOKEN_SCOPE)
        }
        return token.takeUnless { it.isNullOrEmpty() } ?: HmsTokenUpdates.await(NEW_TOKEN_TIMEOUT)
    }

    override suspend fun deleteToken() {
        HmsTokenUpdates.clear()
        withContext(dispatchers.io) {
            HmsInstanceId.getInstance(context).deleteToken(appId(), TOKEN_SCOPE)
            HmsMessaging.getInstance(context).isAutoInitEnabled = false
        }
    }

    // Без плагина agcp конфиг AGC не попадает в ресурсы приложения: agconnect-services.json — из assets флейвора
    // (так же его читает AGCUtils внутри HMS).
    private fun appId(): String = context.assets.open(AGC_CONFIG).use { config ->
        AGConnectOptionsBuilder().setInputStream(config).build(context).getString(APP_ID_KEY)
    }

    private companion object {
        const val TOKEN_SCOPE = "HCM"
        const val AGC_CONFIG = "agconnect-services.json"
        const val APP_ID_KEY = "client/app_id"
        val NEW_TOKEN_TIMEOUT = 10.seconds
    }
}
