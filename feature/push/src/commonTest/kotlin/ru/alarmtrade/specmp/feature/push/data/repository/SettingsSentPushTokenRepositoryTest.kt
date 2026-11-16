package ru.alarmtrade.specmp.feature.push.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.localization.AppLocale
import ru.alarmtrade.specmp.core.testing.InMemorySettings
import ru.alarmtrade.specmp.feature.push.domain.model.SentPushToken

class SettingsSentPushTokenRepositoryTest {

    private val settings = InMemorySettings()
    private val sent = SentPushToken("token", AppLocale.English, Instant.fromEpochMilliseconds(1_700_000_000_000))

    @Test
    fun `nothing sent yet`() = runTest {
        assertEquals(Response.Success(null), SettingsSentPushTokenRepository(settings).lastSent())
    }

    @Test
    fun `saved token survives reopening`() = runTest {
        SettingsSentPushTokenRepository(settings).save(sent)

        assertEquals(Response.Success(sent), SettingsSentPushTokenRepository(settings).lastSent())
    }

    @Test
    fun `incomplete record means not sent`() = runTest {
        settings.putString("token", "token")
        settings.putString("language", "ru")

        assertEquals(Response.Success(null), SettingsSentPushTokenRepository(settings).lastSent())
    }

    @Test
    fun `logout forgets the sent token`() = runTest {
        val repository = SettingsSentPushTokenRepository(settings)
        repository.save(sent)

        repository.release()

        assertEquals(Response.Success(null), repository.lastSent())
    }
}
