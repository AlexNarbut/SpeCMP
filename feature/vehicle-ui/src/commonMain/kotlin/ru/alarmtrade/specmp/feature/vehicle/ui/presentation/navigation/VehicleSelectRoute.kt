package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceType

/**
 * Маршрут выбора автомобиля для app-level графа (`composeApp`): экран — [VehicleSelectEntry] с [request].
 * Сериализуется своё зеркало запроса ([RequestKey]): domain-модели каталога формата хранения не знают.
 */
@Serializable
@SerialName("vehicle-ui.VehicleSelectRoute")
data class VehicleSelectRoute internal constructor(private val key: RequestKey) : NavKey {
    constructor(request: VehicleSelectRequest) : this(RequestKey.of(request))

    val request: VehicleSelectRequest get() = key.toRequest()
}

@Serializable
@SerialName("vehicle-ui.VehicleSelectRoute.RequestKey")
internal data class RequestKey(
    val mode: VehicleSelectMode,
    val catalog: CatalogKey,
    val device: DeviceFilterKey?,
    val firmwareCanCode: String?,
    val warnOnCanMismatch: Boolean,
) {
    fun toRequest() = VehicleSelectRequest(
        mode = mode,
        catalog = when (catalog) {
            CatalogKey.ALL -> VehicleCatalog.ALL
            CatalogKey.WITH_CONFIGURATIONS -> VehicleCatalog.WITH_CONFIGURATIONS
        },
        device = when (device) {
            null -> null

            is DeviceFilterKey.Device -> VehicleDeviceFilter.Device(device.deviceName)

            is DeviceFilterKey.Type -> VehicleDeviceFilter.Type(
                when (device.type) {
                    DeviceTypeKey.ALARM -> VehicleDeviceType.ALARM
                    DeviceTypeKey.MICROBOT -> VehicleDeviceType.MICROBOT
                },
            )
        },
        firmwareCanCode = firmwareCanCode,
        warnOnCanMismatch = warnOnCanMismatch,
    )

    companion object {
        fun of(request: VehicleSelectRequest) = RequestKey(
            mode = request.mode,
            catalog = when (request.catalog) {
                VehicleCatalog.ALL -> CatalogKey.ALL
                VehicleCatalog.WITH_CONFIGURATIONS -> CatalogKey.WITH_CONFIGURATIONS
            },
            device = when (val device = request.device) {
                null -> null

                is VehicleDeviceFilter.Device -> DeviceFilterKey.Device(device.deviceName)

                is VehicleDeviceFilter.Type -> DeviceFilterKey.Type(
                    when (device.type) {
                        VehicleDeviceType.ALARM -> DeviceTypeKey.ALARM
                        VehicleDeviceType.MICROBOT -> DeviceTypeKey.MICROBOT
                    },
                )
            },
            firmwareCanCode = request.firmwareCanCode,
            warnOnCanMismatch = request.warnOnCanMismatch,
        )
    }
}

@Serializable
@SerialName("vehicle-ui.VehicleSelectRoute.CatalogKey")
internal enum class CatalogKey { ALL, WITH_CONFIGURATIONS }

@Serializable
@SerialName("vehicle-ui.VehicleSelectRoute.DeviceTypeKey")
internal enum class DeviceTypeKey { ALARM, MICROBOT }

@Serializable
@SerialName("vehicle-ui.VehicleSelectRoute.DeviceFilterKey")
internal sealed interface DeviceFilterKey {
    @Serializable
    @SerialName("vehicle-ui.VehicleSelectRoute.DeviceFilterKey.Type")
    data class Type(val type: DeviceTypeKey) : DeviceFilterKey

    @Serializable
    @SerialName("vehicle-ui.VehicleSelectRoute.DeviceFilterKey.Device")
    data class Device(val deviceName: String) : DeviceFilterKey
}
