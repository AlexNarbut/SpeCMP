package ru.alarmtrade.specmp.feature.vehicle.domain.model

/** Какой каталог показывать: все автомобили или только те, для которых есть готовые конфигурации. */
enum class VehicleCatalog { ALL, WITH_CONFIGURATIONS }

/** Тип устройства, под который фильтруется каталог, когда блок не подключён. */
enum class VehicleDeviceType { ALARM, MICROBOT }

/** Под какое устройство фильтровать каталог. */
sealed interface VehicleDeviceFilter {
    /** Блок не подключён — тип выбран пользователем. */
    data class Type(val type: VehicleDeviceType) : VehicleDeviceFilter

    /** Модель блока (`systemName`, например `DX-9X`): подсистемы берутся по ней с сервера. */
    data class Device(val deviceName: String) : VehicleDeviceFilter
}

/** Параметры запроса уровня каталога. [groups] пустой — без фильтра функций. */
data class CatalogQuery(
    val catalog: VehicleCatalog,
    val device: VehicleDeviceFilter?,
    val groups: Set<CarFeatureGroup> = emptySet(),
)
