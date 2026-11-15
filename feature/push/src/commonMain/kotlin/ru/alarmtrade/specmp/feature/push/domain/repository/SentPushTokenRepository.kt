package ru.alarmtrade.specmp.feature.push.domain.repository

import ru.alarmtrade.specmp.core.common.AuthCloseable
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.push.domain.model.SentPushToken

/** Привязан к аккаунту: на logout запись стирается ([AuthCloseable]), после входа токен отправляется заново. */
interface SentPushTokenRepository : AuthCloseable {

    /** `null` — ещё не отправляли (или вышли из аккаунта). */
    suspend fun lastSent(): Response<SentPushToken?>

    suspend fun save(sent: SentPushToken): Response<Unit>
}
