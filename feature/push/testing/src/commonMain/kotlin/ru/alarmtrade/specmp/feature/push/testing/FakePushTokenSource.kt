package ru.alarmtrade.specmp.feature.push.testing

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.push.PushTokenSource

class FakePushTokenSource(var token: Response<String?> = Response.Success("token")) : PushTokenSource {
    var releases = 0

    override suspend fun token(): Response<String?> = token

    override suspend fun release() {
        releases++
    }
}
