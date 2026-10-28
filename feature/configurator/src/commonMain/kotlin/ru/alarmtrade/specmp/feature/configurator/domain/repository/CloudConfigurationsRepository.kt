package ru.alarmtrade.specmp.feature.configurator.domain.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfiguration
import ru.alarmtrade.specmp.feature.configurator.domain.model.CloudConfigurationSummary

/** Облачные конфигурации (сервер): списки приходят целиком, поиска на сервере нет. */
interface CloudConfigurationsRepository {
    /** Конфигурации для автомобиля [carId]; [deviceName] пустой — для всех моделей блока. */
    suspend fun byCar(carId: Int, deviceName: String): Response<List<CloudConfigurationSummary>>

    suspend fun card(id: Int): Response<CloudConfiguration>
}

/** Модели блоков, для которых есть заводские настройки (K2), и недавно выбранные. */
interface DeviceModelsRepository {
    suspend fun models(): Response<List<String>>

    fun recent(): List<String>

    fun pushRecent(deviceName: String)

    fun removeRecent(deviceName: String)

    companion object {
        const val MAX_RECENT = 5
    }
}
