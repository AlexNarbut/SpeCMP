package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.system

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheralTypes
import ru.alarmtrade.specmp.feature.device.presentation.core.pin.ServicePinDialogState

/** Устройство на экране: тип + адрес (номер внутри типа может смениться между опросами). */
@Immutable
data class DeviceKey(val type: PeripheralType, val address: String)

val SystemPeripheral.key: DeviceKey get() = DeviceKey(type, address)

@Immutable
data class DeviceGroup(val type: PeripheralType, val devices: List<SystemPeripheral>)

/**
 * @property polled сколько типов опрошено в первом проходе (прогресс «N из M»); после прохода — не меняется.
 * @property pollingType тип, который опрашивается сейчас (только в первом проходе).
 * @property highlight подсветка строки по «Показать»: [Highlight.seq] перезапускает вспышку.
 */
@Immutable
data class SystemDevicesState(
    val deviceName: String? = null,
    val systemId: Long? = null,
    val firmwareVersion: String? = null,
    val bluetoothVersion: String? = null,
    val types: List<PeripheralType> = SystemPeripheralTypes,
    val groups: List<DeviceGroup> = emptyList(),
    val devices: List<SystemPeripheral> = emptyList(),
    val summary: DevicesSummary = DevicesSummary(),
    val polled: Int = 0,
    val pollingType: PeripheralType? = null,
    val firstPassDone: Boolean = false,
    val pollFailed: Boolean = false,
    val pollGeneration: Int = 0,
    val canOpenPairing: Boolean = false,
    val selected: DeviceKey? = null,
    val detailsOpen: Boolean = false,
    val highlight: Highlight? = null,
    val blinking: DeviceKey? = null,
    val firmware: FirmwareStep? = null,
    val pin: ServicePinDialogState? = null,
) {
    fun device(key: DeviceKey?): SystemPeripheral? = key?.let { wanted -> devices.firstOrNull { it.key == wanted } }
}

/** Сводка после прохода; [focus] — куда ведёт «Показать»: сначала без связи, затем остальные проблемные. */
@Immutable
data class DevicesSummary(
    val total: Int = 0,
    val online: Int = 0,
    val attention: Int = 0,
    val anyOffline: Boolean = false,
    val focus: DeviceKey? = null,
)

@Immutable
data class Highlight(val key: DeviceKey, val seq: Int)

/** «Обновить» (режим прошивки устройства): подтверждение → ожидание → закрытие сессии; или неудача. */
@Immutable
sealed interface FirmwareStep {
    val device: DeviceKey

    data class Confirm(override val device: DeviceKey) : FirmwareStep

    data class Waiting(override val device: DeviceKey) : FirmwareStep

    data class Closing(override val device: DeviceKey) : FirmwareStep

    data class Failed(override val device: DeviceKey, val reason: FirmwareFailure) : FirmwareStep
}

enum class FirmwareFailure { NO_ANSWER, REFUSED }
