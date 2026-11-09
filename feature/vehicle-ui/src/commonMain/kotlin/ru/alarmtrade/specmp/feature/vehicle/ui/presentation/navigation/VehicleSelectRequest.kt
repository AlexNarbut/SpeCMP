package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter

@Serializable
@SerialName("vehicle-ui.VehicleSelectMode")
enum class VehicleSelectMode { VIEW, PICK }

/**
 * [device] `null` — сначала экран выбора типа устройства; [warnOnCanMismatch] — в режиме выбора предупредить,
 * если CAN-код автомобиля не совпадает с [firmwareCanCode].
 */
data class VehicleSelectRequest(
    val mode: VehicleSelectMode,
    val catalog: VehicleCatalog,
    val device: VehicleDeviceFilter?,
    val firmwareCanCode: String? = null,
    val warnOnCanMismatch: Boolean = false,
)
