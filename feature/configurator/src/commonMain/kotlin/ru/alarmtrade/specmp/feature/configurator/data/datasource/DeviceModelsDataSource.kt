package ru.alarmtrade.specmp.feature.configurator.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.usecase.EmptyRequest
import ru.alarmtrade.specialist.interactors.deviceInformation.DeviceInformationInteractor
import ru.alarmtrade.specmp.core.specialist.sdk.resolveOrNull

// Ответ null — интерактор недоступен на платформе.
internal interface DeviceModelsDataSource {
    suspend fun deviceNames(): SpecialistResponse<List<String>>?
}

internal class SpecialistDeviceModelsDataSource(private val interactor: () -> DeviceInformationInteractor) :
    DeviceModelsDataSource {
    override suspend fun deviceNames(): SpecialistResponse<List<String>>? =
        interactor.resolveOrNull()?.getSubsystemNamesUseCase?.execute(EmptyRequest())
}
