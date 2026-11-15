package ru.alarmtrade.specmp.feature.push.testing

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.feature.push.domain.usecase.RegisterPushTokenUseCase

/** Настоящий [RegisterPushTokenUseCase] поверх фейков: тест смотрит [pushTokens].`sends`. */
class TestPushRegistration(
    val tokenSource: FakePushTokenSource = FakePushTokenSource(),
    val pushTokens: FakePushTokenRepository = FakePushTokenRepository(),
    val sentTokens: FakeSentPushTokenRepository = FakeSentPushTokenRepository(),
    val localeStore: FakeAppLocaleStore = FakeAppLocaleStore(),
) {
    val useCase = RegisterPushTokenUseCase(
        tokenSource = tokenSource,
        pushTokenRepository = pushTokens,
        sentPushTokenRepository = sentTokens,
        localeStore = localeStore,
        clock = object : Clock {
            override fun now(): Instant = Instant.fromEpochMilliseconds(0)
        },
    )
}
