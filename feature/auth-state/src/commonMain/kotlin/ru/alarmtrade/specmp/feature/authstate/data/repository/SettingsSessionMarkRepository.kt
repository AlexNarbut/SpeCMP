package ru.alarmtrade.specmp.feature.authstate.data.repository

import com.russhwolf.settings.Settings
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.authstate.domain.repository.SessionMarkRepository

internal class SettingsSessionMarkRepository(private val settings: Settings) : SessionMarkRepository {

    override suspend fun isMarked(): Response<Boolean> = safeRun { settings.getBoolean(KEY_MARKED, false) }

    override suspend fun mark(): Response<Unit> = safeRun { settings.putBoolean(KEY_MARKED, true) }

    override suspend fun clear(): Response<Unit> = safeRun { settings.remove(KEY_MARKED) }

    private companion object {
        const val KEY_MARKED = "marked"
    }
}
