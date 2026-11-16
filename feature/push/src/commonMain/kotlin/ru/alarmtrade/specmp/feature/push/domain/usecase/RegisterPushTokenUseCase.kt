package ru.alarmtrade.specmp.feature.push.domain.usecase

import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.mapIfSuccess
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.push.PushTokenSource
import ru.alarmtrade.specmp.feature.push.domain.model.SentPushToken
import ru.alarmtrade.specmp.feature.push.domain.repository.PushTokenRepository
import ru.alarmtrade.specmp.feature.push.domain.repository.SentPushTokenRepository

/**
 * Отправить push-токен устройства на сервер, если сервер его ещё не знает: новый токен (после входа / ротации),
 * сменился язык интерфейса или прошло [RESEND_INTERVAL]. Зовут главный экран на старте и настройки после смены
 * языка; неудача не запоминается — повтор при следующем вызове.
 */
class RegisterPushTokenUseCase(
    private val tokenSource: PushTokenSource,
    private val pushTokenRepository: PushTokenRepository,
    private val sentPushTokenRepository: SentPushTokenRepository,
    private val localeStore: AppLocaleStore,
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()

    suspend operator fun invoke(): Response<Unit> = mutex.withLock {
        tokenSource.token().mapIfSuccess { token -> if (token == null) Response.Success(Unit) else register(token) }
    }

    private suspend fun register(token: String): Response<Unit> {
        val locale = localeStore.current()
        val now = clock.now()
        val last = sentPushTokenRepository.lastSent().getValueIfSuccess()
        val known = last != null && last.token == token && last.locale == locale && now - last.sentAt < RESEND_INTERVAL
        if (known) return Response.Success(Unit)

        return pushTokenRepository.send(token, locale)
            .mapIfSuccess { sentPushTokenRepository.save(SentPushToken(token, locale, now)) }
    }

    private companion object {
        val RESEND_INTERVAL = 10.days
    }
}
