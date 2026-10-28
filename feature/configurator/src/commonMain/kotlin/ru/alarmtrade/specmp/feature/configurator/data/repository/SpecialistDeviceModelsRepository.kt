package ru.alarmtrade.specmp.feature.configurator.data.repository

import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.getValueIfSuccess
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.safeRun
import ru.alarmtrade.specmp.feature.configurator.domain.repository.DeviceModelsRepository

/**
 * [DeviceModelsRepository]: модели с заводскими настройками — `deviceInformationInteractor` (с сервера), недавние —
 * JSON-список в именованном хранилище приложения (битые данные — пустой список).
 */
class SpecialistDeviceModelsRepository(
    private val interactor: () -> DeviceInformationInteractor,
    private val settings: Settings
) : DeviceModelsRepository {

    override suspend fun models(): Response<List<String>> {
        val sdk = interactor.resolveOrNull() ?: return interactorUnavailable()
        return sdk.getSubsystemNamesUseCase.execute(EmptyRequest()).toResponse()
            .map { names -> names.filter { it.isNotBlank() }.distinct().sortedBy { it.lowercase() } }
    }

    override fun recent(): List<String> {
        val raw = settings.getStringOrNull(RECENT_KEY) ?: return emptyList()
        return safeRun { json.decodeFromString(serializer, raw) }.getValueIfSuccess().orEmpty()
    }

    override fun pushRecent(deviceName: String) {
        val updated = (
            listOf(deviceName) + recent().filter {
                it != deviceName
            }
            ).take(DeviceModelsRepository.MAX_RECENT)
        settings.putString(RECENT_KEY, json.encodeToString(serializer, updated))
    }

    override fun removeRecent(deviceName: String) {
        settings.putString(RECENT_KEY, json.encodeToString(serializer, recent().filter { it != deviceName }))
    }

    private companion object {
        const val RECENT_KEY = "recent_models"
        val json = Json { ignoreUnknownKeys = true }
        val serializer = ListSerializer(String.serializer())
    }
}
