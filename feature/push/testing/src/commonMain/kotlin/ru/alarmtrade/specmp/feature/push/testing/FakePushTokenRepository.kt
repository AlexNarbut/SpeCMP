package ru.alarmtrade.specmp.feature.push.testing

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.feature.push.domain.repository.PushTokenRepository

class FakePushTokenRepository(var result: Response<Unit> = Response.Success(Unit)) : PushTokenRepository {
    val sends = mutableListOf<Pair<String, AppLocale>>()

    override suspend fun send(token: String, locale: AppLocale): Response<Unit> {
        sends += token to locale
        return result
    }
}
