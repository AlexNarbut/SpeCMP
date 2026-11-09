package ru.alarmtrade.specmp.feature.configurator.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.domain.model.LocalConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.repository.LocalConfigurationsRepository
import ru.alarmtrade.specmp.feature.device.settings.domain.model.SettingsSource

class FakeLocalConfigurationsRepository : LocalConfigurationsRepository {
    override val changes = MutableSharedFlow<Response<Unit>>(extraBufferCapacity = 1)
    var items: Response<List<LocalConfiguration>> = Response.Success(emptyList())
    var allCalls = 0
        private set
    var createResult: Response<SettingsSource.LocalConfiguration> =
        Response.Success(SettingsSource.LocalConfiguration(100, "DX-9X", "Новая"))

    /** Пока не завершён — создание «идёт» (проверка повторных нажатий). */
    var createGate: CompletableDeferred<Unit>? = null
    val createCalls = mutableListOf<Pair<String, SettingsSource>>()
    val deleted = mutableListOf<Long>()

    override suspend fun all(): Response<List<LocalConfiguration>> {
        allCalls++
        return items
    }

    override suspend fun create(
        name: String,
        description: String,
        deviceName: String,
        from: SettingsSource,
    ): Response<SettingsSource.LocalConfiguration> {
        createCalls += name to from
        createGate?.await()
        return createResult
    }

    override suspend fun updateInfo(
        configuration: LocalConfiguration,
        name: String,
        description: String,
    ): Response<SettingsSource.LocalConfiguration> =
        Response.Success(SettingsSource.LocalConfiguration(configuration.id + 1000, configuration.deviceName, name))

    override suspend fun delete(id: Long): Response<Unit> {
        deleted += id
        return Response.Success(Unit)
    }
}
