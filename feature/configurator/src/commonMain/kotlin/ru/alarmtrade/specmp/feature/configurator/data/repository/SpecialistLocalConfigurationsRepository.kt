package ru.alarmtrade.specmp.feature.configurator.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.alarmtrade.specialist.domain.alarmDevice.settings.LocalSettingsConfigurationInfo
import ru.alarmtrade.specialist.interactors.localConfigurations.LocalSettingsConfigurationInteractor
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.CreateLocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationRequest
import ru.alarmtrade.specialist.interactors.localConfigurations.requests.LocalConfigurationsPageRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.configurator.domain.repository.toSource
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource
import ru.alarmtrade.specmp.feature.device.settings.domain.repository.DeviceSettingsRepository

/**
 * [LocalConfigurationsRepository] над `localConfigurationInteractor` (записи хранилища) и сессиями настроек
 * ([DeviceSettingsRepository.copy] — настройки записи). Интерактор — лениво.
 *
 * SDK создаёт запись с заводскими настройками модели, настройки источника копируются в неё следом; не скопировались —
 * запись удаляется. Страницы SDK идут по id по возрастанию — читаются все и сортируются здесь.
 */
class SpecialistLocalConfigurationsRepository(
    private val interactor: () -> LocalSettingsConfigurationInteractor,
    private val settings: DeviceSettingsRepository
) : LocalConfigurationsRepository {

    private val _changes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val changes: Flow<Unit> = _changes.asSharedFlow()

    override suspend fun all(): Response<List<LocalConfiguration>> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        val result = mutableListOf<LocalConfiguration>()
        var page = 0
        while (true) {
            val response = sdk.getLocalConfigurationListUseCase
                .execute(LocalConfigurationsPageRequest(page = page, count = PAGE_SIZE))
                .toResponse()
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
        from: SettingsSource
    ): Response<SettingsSource.LocalConfiguration> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        val created = sdk.createLocalConfigurationUseCase
            .execute(CreateLocalConfigurationRequest(name = name, deviceName = deviceName, description = description))
            .toResponse()
        if (created !is Response.Success) return created as Response.Error
        val target = SettingsSource.LocalConfiguration(id = created.value.long, deviceName = deviceName, title = name)
        return when (val copied = settings.copy(from = from, to = target, saveChanges = true)) {
            is Response.Error -> {
                sdk.deleteLocalConfigurationUseCase.execute(LocalConfigurationRequest(target.id))
                copied
            }

            is Response.Success -> {
                _changes.tryEmit(Unit)
                Response.Success(target)
            }
        }
    }

    override suspend fun updateInfo(
        configuration: LocalConfiguration,
        name: String,
        description: String
    ): Response<SettingsSource.LocalConfiguration> {
        val old = configuration.toSource()
        val created = create(name, description, configuration.deviceName, from = old)
        if (created !is Response.Success) return created
        // Сессию старой записи открыло копирование — закрыть до удаления.
        settings.close(old)
        return when (val deleted = delete(configuration.id)) {
            // Старая не удалилась — убираем копию, иначе в списке окажутся обе (старое и новое название).
            is Response.Error -> {
                delete(created.value.id)
                deleted
            }

            is Response.Success -> created
        }
    }

    override suspend fun delete(id: Long): Response<Unit> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        return sdk.deleteLocalConfigurationUseCase.execute(LocalConfigurationRequest(id)).toResponse().also {
            if (it is Response.Success) _changes.tryEmit(Unit)
        }
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}

internal fun LocalSettingsConfigurationInfo.toDomain() = LocalConfiguration(
    id = id,
    name = name,
    description = description,
    deviceName = deviceName,
    created = createdDate,
    updated = updatedDate
)
