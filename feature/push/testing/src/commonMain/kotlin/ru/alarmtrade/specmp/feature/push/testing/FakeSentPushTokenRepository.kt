package ru.alarmtrade.specmp.feature.push.testing

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.push.domain.model.SentPushToken
import ru.alarmtrade.specmp.feature.push.domain.repository.SentPushTokenRepository

class FakeSentPushTokenRepository(var sent: SentPushToken? = null) : SentPushTokenRepository {

    override suspend fun lastSent(): Response<SentPushToken?> = Response.Success(sent)

    override suspend fun save(sent: SentPushToken): Response<Unit> {
        this.sent = sent
        return Response.Success(Unit)
    }

    override suspend fun release() {
        sent = null
    }
}
