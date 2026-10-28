package ru.alarmtrade.specmp.feature.vehicle.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Какой каталог показывать: все автомобили или только те, для которых есть готовые конфигурации. */
@Serializable
@SerialName("vehicle.VehicleCatalog")
enum class VehicleCatalog { ALL, WITH_CONFIGURATIONS }

/** Тип устройства, под который фильтруется каталог, когда блок не подключён. */
@Serializable
@SerialName("vehicle.VehicleDeviceType")
enum class VehicleDeviceType { ALARM, MICROBOT }

/** Под какое устройство фильтровать каталог. */
@Serializable
@SerialName("vehicle.VehicleDeviceFilter")
sealed interface VehicleDeviceFilter {
    /** Блок не подключён — тип выбран пользователем. */
    @Serializable
    @SerialName("vehicle.VehicleDeviceFilter.Type")
    data class Type(val type: VehicleDeviceType) : VehicleDeviceFilter

    /** Модель блока (`systemName`, например `DX-9X`): подсистемы берутся по ней с сервера. */
    @Serializable
    @SerialName("vehicle.VehicleDeviceFilter.Device")
    data class Device(val deviceName: String) : VehicleDeviceFilter
}

/** Параметры запроса уровня каталога. [groups] пустой — без фильтра функций. */
data class CatalogQuery(
    val catalog: VehicleCatalog,
    val device: VehicleDeviceFilter?,
    val groups: Set<CarFeatureGroup> = emptySet()
)
