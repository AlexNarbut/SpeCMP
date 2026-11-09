package ru.alarmtrade.specmp.feature.configurator.data.repository

import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specmp.core.common.RecentList
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
class SpecialistDeviceModelsRepository(private val interactor: () -> DeviceInformationInteractor, settings: Settings) :
    DeviceModelsRepository {

    private val recent = RecentList<String, String>(
        max = DeviceModelsRepository.MAX_RECENT,
        key = { it },
        read = {
            settings.getStringOrNull(RECENT_KEY)
                ?.let { raw -> safeRun { json.decodeFromString(serializer, raw) }.getValueIfSuccess() }
                .orEmpty()
        },
        write = { settings.putString(RECENT_KEY, json.encodeToString(serializer, it)) },
    )

    override suspend fun models(): Response<List<String>> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        return sdk.getSubsystemNamesUseCase.execute(EmptyRequest()).toResponse()
            .map { names -> names.filter { it.isNotBlank() }.distinct().sortedBy { it.lowercase() } }
    }

    override suspend fun recent(): Response<List<String>> = safeRun { recent.all() }

    override suspend fun pushRecent(deviceName: String): Response<List<String>> = safeRun { recent.push(deviceName) }

    override suspend fun removeRecent(deviceName: String): Response<List<String>> =
        safeRun { recent.remove(deviceName) }

    private companion object {
        const val RECENT_KEY = "recent_models"
        val json = Json { ignoreUnknownKeys = true }
        val serializer = ListSerializer(String.serializer())
    }
}
