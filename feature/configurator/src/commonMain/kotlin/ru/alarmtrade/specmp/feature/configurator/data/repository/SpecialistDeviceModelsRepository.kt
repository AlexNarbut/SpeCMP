package ru.alarmtrade.specmp.feature.configurator.data.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.core.specialist.sdk.interactorUnavailable
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository

/**
 * [DeviceModelsRepository]: модели с заводскими настройками — `deviceInformationInteractor` (с сервера), недавние —
 * JSON-список в именованном хранилище приложения (битые данные — пустой список).
 */
class SpecialistDeviceModelsRepository(
    private val interactor: () -> DeviceInformationInteractor,
    private val settings: Settings
) : DeviceModelsRepository {

    private val recentLock = Mutex()

    override suspend fun models(): Response<List<String>> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        return sdk.getSubsystemNamesUseCase.execute(EmptyRequest()).toResponse()
            .map { names -> names.filter { it.isNotBlank() }.distinct().sortedBy { it.lowercase() } }
    }

    override fun recent(): List<String> {
        val raw = settings.getStringOrNull(RECENT_KEY) ?: return emptyList()
        return safeRun { json.decodeFromString(serializer, raw) }.getValueIfSuccess().orEmpty()
    }

    override suspend fun pushRecent(deviceName: String): List<String> = updateRecent { recent ->
        (listOf(deviceName) + recent.filter { it != deviceName }).take(DeviceModelsRepository.MAX_RECENT)
    }

    override suspend fun removeRecent(deviceName: String): List<String> = updateRecent { recent ->
        recent.filter { it != deviceName }
    }

    // Settings не атомарен: две быстрые правки (выбор и «✕») без замка потеряли бы одну.
    private suspend fun updateRecent(change: (List<String>) -> List<String>): List<String> = recentLock.withLock {
        change(recent()).also { settings.putString(RECENT_KEY, json.encodeToString(serializer, it)) }
    }

    private companion object {
        const val RECENT_KEY = "recent_models"
        val json = Json { ignoreUnknownKeys = true }
        val serializer = ListSerializer(String.serializer())
    }
}
