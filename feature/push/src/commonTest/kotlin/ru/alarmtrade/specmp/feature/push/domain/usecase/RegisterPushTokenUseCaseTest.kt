package ru.alarmtrade.specmp.feature.push.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.localization.AppLocaleStore
import ru.alarmtrade.specmp.core.push.PushTokenSource
import ru.alarmtrade.specmp.core.testing.FixedClock
import ru.alarmtrade.specmp.feature.push.domain.model.SentPushToken
import ru.alarmtrade.specmp.feature.push.domain.repository.PushTokenRepository
import ru.alarmtrade.specmp.feature.push.domain.repository.SentPushTokenRepository

class RegisterPushTokenUseCaseTest {

    private val start = Instant.fromEpochMilliseconds(1_700_000_000_000)
    private val clock = FixedClock(start)
    private var token: Response<String?> = Response.Success("token-1")
    private var locale = AppLocale.Russian
    private var sendResult: Response<Unit> = Response.Success(Unit)
    private val sends = mutableListOf<Pair<String, AppLocale>>()
    private var lastSent: SentPushToken? = null

    private val useCase = RegisterPushTokenUseCase(
        tokenSource = object : PushTokenSource {
            override suspend fun token(): Response<String?> = token
            override suspend fun release() = error("not used")
        },
        pushTokenRepository = object : PushTokenRepository {
            override suspend fun send(token: String, locale: AppLocale): Response<Unit> {
                sends += token to locale
                return sendResult
            }
        },
        sentPushTokenRepository = object : SentPushTokenRepository {
            override suspend fun lastSent(): Response<SentPushToken?> = Response.Success(lastSent)
            override suspend fun save(sent: SentPushToken): Response<Unit> {
                lastSent = sent
                return Response.Success(Unit)
            }

            override suspend fun release() = error("not used")
        },
        localeStore = object : AppLocaleStore {
            override fun current(): AppLocale = locale
            override fun save(locale: AppLocale) = error("not used")
        },
        clock = clock,
    )

    @Test
    fun `first registration sends the token and remembers it`() = runTest {
        assertEquals(Response.Success(Unit), useCase())

        assertEquals(listOf("token-1" to AppLocale.Russian), sends)
        assertEquals(SentPushToken("token-1", AppLocale.Russian, start), lastSent)
    }

    @Test
    fun `known token is not sent again`() = runTest {
        useCase()
        clock.instant = start + 9.days

        assertEquals(Response.Success(Unit), useCase())

        assertEquals(1, sends.size)
    }

    @Test
    fun `token is resent after ten days`() = runTest {
        useCase()
        clock.instant = start + 10.days

        useCase()

        assertEquals(2, sends.size)
        assertEquals(start + 10.days, lastSent?.sentAt)
    }

    @Test
    fun `new token after login is sent`() = runTest {
        useCase()
        token = Response.Success("token-2")

        useCase()

        assertEquals(listOf("token-1" to AppLocale.Russian, "token-2" to AppLocale.Russian), sends)
    }

    @Test
    fun `interface language change resends the same token`() = runTest {
        useCase()
        locale = AppLocale.English

        useCase()

        assertEquals("token-1" to AppLocale.English, sends.last())
        assertEquals(AppLocale.English, lastSent?.locale)
    }

    @Test
    fun `failed send is not remembered and retried next time`() = runTest {
        sendResult = Response.Error(IllegalStateException("offline"))

        assertIs<Response.Error>(useCase())
        assertEquals(null, lastSent)

        sendResult = Response.Success(Unit)
        useCase()

        assertEquals(2, sends.size)
        assertEquals("token-1", lastSent?.token)
    }

    @Test
    fun `platform without push does nothing`() = runTest {
        token = Response.Success(null)

        assertEquals(Response.Success(Unit), useCase())

        assertEquals(emptyList(), sends)
    }

    @Test
    fun `token failure is the result`() = runTest {
        token = Response.Error(IllegalStateException("no services"))

        assertIs<Response.Error>(useCase())

        assertEquals(emptyList(), sends)
    }

    @Test
    fun `parallel registrations send the token once`() = runTest {
        withContext(Dispatchers.Default) {
            List(32) { async { useCase() } }.awaitAll()
        }

        assertEquals(1, sends.size)
    }
}
