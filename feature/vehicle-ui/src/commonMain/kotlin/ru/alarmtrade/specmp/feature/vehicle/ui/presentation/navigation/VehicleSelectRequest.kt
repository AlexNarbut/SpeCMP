package ru.alarmtrade.specmp.feature.vehicle.ui.presentation.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleCatalog
import ru.alarmtrade.specmp.feature.vehicle.domain.model.VehicleDeviceFilter

/** Режим экрана: справочник (без «Выбрать») или выбор автомобиля с результатом. */
@Serializable
@SerialName("vehicle-ui.VehicleSelectMode")
enum class VehicleSelectMode { VIEW, PICK }

/**
 * Запрос на открытие выбора автомобиля ([VehicleSelectEntry]). В ключ маршрута — через [VehicleSelectRoute].
 *
 * @property device под какое устройство фильтровать каталог; `null` — сначала экран выбора типа устройства.
 * @property firmwareCanCode CAN-код прошивки подключённого блока: подзаголовок и предзаполненный поиск по CAN.
 * @property warnOnCanMismatch в режиме выбора предупредить, если CAN-код автомобиля не совпадает с [firmwareCanCode].
 */
data class VehicleSelectRequest(
    val mode: VehicleSelectMode,
    val catalog: VehicleCatalog,
    val device: VehicleDeviceFilter?,
    val firmwareCanCode: String? = null,
    val warnOnCanMismatch: Boolean = false,
)
