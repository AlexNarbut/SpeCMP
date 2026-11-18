package ru.alarmtrade.specmp.feature.main.data.cache

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.testing.InMemorySettings
import ru.alarmtrade.specmp.feature.main.domain.model.MainUserProfile

class SettingsMainUserCacheTest {
    private val settings = InMemorySettings()
    private val cache = SettingsMainUserCache(settings)

    @Test
    fun `profile keeps the avatar url`() = runTest {
        val profile = MainUserProfile(name = "Иван", email = "ivan@mail.ru", avatarUrl = "https://example.com/a.jpg")

        cache.saveProfile(profile)

        assertEquals(profile, cache.profile())
    }

    @Test
    fun `profile cached without avatar is still read`() = runTest {
        settings.putString("profile", """{"name":"Иван","email":"ivan@mail.ru"}""")

        assertEquals(MainUserProfile(name = "Иван", email = "ivan@mail.ru"), cache.profile())
    }
}
