package ru.alarmtrade.specmp.feature.configurator.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary

interface CloudConfigurationsRepository {
    /** Конфигурации для автомобиля [carId]; [deviceName] пустой — для всех моделей блока. */
    suspend fun byCar(carId: Int, deviceName: String): Response<List<CloudConfigurationSummary>>

    suspend fun card(id: Int): Response<CloudConfiguration>
}

interface DeviceModelsRepository {
    suspend fun models(): Response<List<String>>

    suspend fun recent(): Response<List<String>>

    /** Атомарно. Ответ — новый список. */
    suspend fun pushRecent(deviceName: String): Response<List<String>>

    suspend fun removeRecent(deviceName: String): Response<List<String>>

    companion object {
        const val MAX_RECENT = 5
    }
}
