package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.ScanError

sealed interface ScanStep {
    data object PermissionCheck : ScanStep

    data class Denied(val canOpenSettings: Boolean) : ScanStep

    data object AdapterOff : ScanStep

    /** Служба геолокации выключена (нужна для BLE-скана на Android < 12). */
    data object LocationOff : ScanStep

    data object Scanning : ScanStep

    data class Connecting(val device: DeviceUiItem) : ScanStep
}

@Immutable
data class ScanDeviceState(
    val step: ScanStep = ScanStep.PermissionCheck,
    val requirements: List<DeviceAccessRequirement> = emptyList(),
    val supportedTransports: Set<DeviceTransport> = emptySet(),
    val devices: LCEState<List<DeviceUiItem>, ScanError> = LCEState.None,
    val selectedId: String? = null,
    val searchedLongEnough: Boolean = false,
    /** Идёт попытка подключения: повторное «Подключить» её не прерывает. */
    val connectAttempt: Boolean = false,
)

@Immutable
internal data class DeviceInfoRow(val label: String, val value: String, val strong: Boolean = false)
