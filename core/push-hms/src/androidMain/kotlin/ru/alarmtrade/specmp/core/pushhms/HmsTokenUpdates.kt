package ru.alarmtrade.specmp.core.pushhms

import kotlin.time.Duration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull

/** Токен из `onNewToken`: на EMUI до 10 `getToken` возвращает пустую строку, а токен приходит сервису. */
internal object HmsTokenUpdates {

    private val latest = MutableStateFlow<String?>(null)

    fun offer(token: String?) {
        if (!token.isNullOrEmpty()) latest.update { token }
    }

    fun clear() {
        latest.update { null }
    }

    suspend fun await(timeout: Duration): String? = withTimeoutOrNull(timeout) { latest.filterNotNull().first() }
}
