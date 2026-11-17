package ru.alarmtrade.specmp.feature.authstate.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.testing.InMemorySettings

class SettingsSessionMarkRepositoryTest {

    private val settings = InMemorySettings()

    @Test
    fun `not marked initially`() = runTest {
        assertEquals(Response.Success(false), SettingsSessionMarkRepository(settings).isMarked())
    }

    @Test
    fun `mark survives reopening`() = runTest {
        SettingsSessionMarkRepository(settings).mark()

        assertEquals(Response.Success(true), SettingsSessionMarkRepository(settings).isMarked())
    }

    @Test
    fun `clear removes the mark`() = runTest {
        val repository = SettingsSessionMarkRepository(settings)
        repository.mark()

        repository.clear()

        assertEquals(Response.Success(false), repository.isMarked())
    }
}
