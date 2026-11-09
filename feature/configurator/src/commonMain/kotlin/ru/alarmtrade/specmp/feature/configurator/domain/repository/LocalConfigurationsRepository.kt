package ru.alarmtrade.specmp.feature.configurator.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

interface LocalConfigurationsRepository {
    /** Сигнал «список изменился» (создание, переименование, удаление) — списки перечитываются. */
    val changes: Flow<Response<Unit>>

    suspend fun all(): Response<List<LocalConfiguration>>

    /** [from] — текущие значения его сессии, в том числе несохранённые. Ответ — источник для редактора. */
    suspend fun create(
        name: String,
        description: String,
        deviceName: String,
        from: SettingsSource,
    ): Response<SettingsSource.LocalConfiguration>

    /**
     * SDK не умеет менять название — создаётся копия с новыми названием и описанием, старая удаляется: id меняется,
     * ответ — новый источник.
     */
    suspend fun updateInfo(
        configuration: LocalConfiguration,
        name: String,
        description: String,
    ): Response<SettingsSource.LocalConfiguration>

    suspend fun delete(id: Long): Response<Unit>
}

fun LocalConfiguration.toSource(): SettingsSource.LocalConfiguration =
    SettingsSource.LocalConfiguration(id = id, deviceName = deviceName, title = name)
