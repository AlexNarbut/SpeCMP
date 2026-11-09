package ru.alarmtrade.specmp.feature.configurator.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.CreateLocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationsPageRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponseOrUnavailable
import ru.alarmtrade.specmp.feature.configurator.data.datasource.LocalConfigurationsDataSource
import ru.alarmtrade.specmp.feature.configurator.data.mapper.toDomain
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * [LocalConfigurationsRepository] над `localConfigurationInteractor` (записи хранилища,
 * [LocalConfigurationsDataSource]) и сессиями настроек ([DeviceSettingsRepository.copy] — настройки записи).
 *
 * SDK создаёт запись с заводскими настройками модели, настройки источника копируются в неё следом; не скопировались —
 * запись удаляется. Страницы SDK идут по id по возрастанию — читаются все и сортируются здесь.
 */
internal class SpecialistLocalConfigurationsRepository(
    private val dataSource: LocalConfigurationsDataSource,
    private val settings: DeviceSettingsRepository,
) : LocalConfigurationsRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    private val _changes = MutableSharedFlow<Response<Unit>>(extraBufferCapacity = 1)
    override val changes: Flow<Response<Unit>> = _changes.asSharedFlow()

    override suspend fun all(): Response<List<LocalConfiguration>> {
        val result = mutableListOf<LocalConfiguration>()
        var page = 0
        while (true) {
            val response = dataSource.page(LocalConfigurationsPageRequest(page = page, count = PAGE_SIZE))
                .toResponseOrUnavailable()
            when (response) {
                is Response.Error -> return response
                is Response.Success -> result += response.value.map { it.toDomain() }
            }
            if (response.value.size < PAGE_SIZE) break
            page++
        }
        return Response.Success(result.sortedByDescending { it.updated })
    }

    override suspend fun create(
        name: String,
        description: String,
        deviceName: String,
        from: SettingsSource,
    ): Response<SettingsSource.LocalConfiguration> {
        val created = dataSource
            .create(CreateLocalConfigurationRequest(name = name, deviceName = deviceName, description = description))
            .toResponseOrUnavailable()
        if (created !is Response.Success) return created as Response.Error
        val target = SettingsSource.LocalConfiguration(id = created.value.long, deviceName = deviceName, title = name)
        return when (val copied = settings.copy(from = from, to = target, saveChanges = true)) {
            is Response.Error -> {
                dataSource.delete(LocalConfigurationRequest(target.id))
                copied
            }

            is Response.Success -> {
                _changes.tryEmit(Response.Success(Unit))
                Response.Success(target)
            }
        }
    }

    override suspend fun updateInfo(
        configuration: LocalConfiguration,
        name: String,
        description: String,
    ): Response<SettingsSource.LocalConfiguration> {
        val old = configuration.toSource()
        val created = create(name, description, configuration.deviceName, from = old)
        if (created !is Response.Success) return created
        // Сессию старой записи открыло копирование — закрыть до удаления.
        settings.close(old).onError { logger.w(it) { "Failed to close the settings session of ${configuration.id}" } }
        return when (val deleted = delete(configuration.id)) {
            // Старая не удалилась — убираем копию, иначе в списке окажутся обе (старое и новое название).
            is Response.Error -> {
                delete(created.value.id).onError { logger.w(it) { "Failed to delete the copy ${created.value.id}" } }
                deleted
            }

            is Response.Success -> created
        }
    }

    override suspend fun delete(id: Long): Response<Unit> =
        dataSource.delete(LocalConfigurationRequest(id)).toResponseOrUnavailable().also {
            if (it is Response.Success) _changes.tryEmit(Response.Success(Unit))
        }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
