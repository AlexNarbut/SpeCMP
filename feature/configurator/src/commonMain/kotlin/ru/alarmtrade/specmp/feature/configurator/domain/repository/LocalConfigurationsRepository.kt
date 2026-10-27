package ru.alarmtrade.specmp.feature.configurator.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

/** «Мои конфигурации» в хранилище SDK. */
interface LocalConfigurationsRepository {
    /** Сигнал «список изменился» (создание, переименование, удаление) — списки перечитываются. */
    val changes: Flow<Unit>

    /** Все конфигурации пользователя, новые (по дате изменения) сверху. */
    suspend fun all(): Response<List<LocalConfiguration>>

    /**
     * Новая конфигурация модели [deviceName] с настройками из [from] (текущие значения его сессии, в том числе
     * несохранённые). Ответ — источник для редактора.
     */
    suspend fun create(
        name: String,
        description: String,
        deviceName: String,
        from: SettingsSource
    ): Response<SettingsSource.LocalConfiguration>

    /**
     * Новые название и описание. SDK не умеет менять название (пишет вместо него модель), поэтому создаётся копия с
     * новыми названием и описанием, а старая удаляется: id меняется, ответ — новый источник.
     */
    suspend fun updateInfo(
        configuration: LocalConfiguration,
        name: String,
        description: String
    ): Response<SettingsSource.LocalConfiguration>

    suspend fun delete(id: Long): Response<Unit>
}

/** Источник редактора для «моей» конфигурации. */
fun LocalConfiguration.toSource(): SettingsSource.LocalConfiguration =
    SettingsSource.LocalConfiguration(id = id, deviceName = deviceName, title = name)
