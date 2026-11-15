package ru.alarmtrade.specmp.core.push

import ru.alarmtrade.specmp.core.common.Response

internal class NoOpPushTokenSource : PushTokenSource {

    override suspend fun token(): Response<String?> = Response.Success(null)

    override suspend fun release() {
        PushOpenRequests.clear()
    }
}
