package ru.alarmtrade.specmp.feature.device.selection.ui.presentation.screen.scan

import androidx.compose.runtime.Immutable
import ru.alarmtrade.specmp.core.permissions.DeviceAccessRequirement
import ru.alarmtrade.specmp.core.ui.lce.LCEState
import ru.alarmtrade.specmp.feature.device.selection.domain.model.DeviceTransport
import ru.alarmtrade.specmp.feature.device.selection.ui.presentation.model.DeviceUiItem

/** Шаг экрана поиска устройства (state machine). */
sealed interface ScanStep {
    /** Проверка/запрос разрешений (VM запрашивает напрямую, системный диалог). */
    data object PermissionCheck : ScanStep

    /** Доступ отклонён; [canOpenSettings] — отклонено навсегда → вести в системные настройки. */
    data class Denied(val canOpenSettings: Boolean) : ScanStep

    /** Bluetooth выключен. */
    data object AdapterOff : ScanStep

    /** Служба геолокации выключена (нужна для BLE-скана на Android < 12). */
    data object LocationOff : ScanStep

    /** Идёт сканирование; список/пусто выводятся из [ScanDeviceState.devices]. */
    data object Scanning : ScanStep

    /** Подключение к выбранному устройству (заглушка до interaction-ui). */
    data class Connecting(val device: DeviceUiItem) : ScanStep
}

@Immutable
data class ScanDeviceState(
    val step: ScanStep = ScanStep.PermissionCheck,
    /** Требования доступа платформы (для подсказок «как починить» на экране denied). */
    val requirements: List<DeviceAccessRequirement> = emptyList(),
    /** Транспорты, поддерживаемые платформой (для подсказок «как найти»). */
    val supportedTransports: Set<DeviceTransport> = emptySet(),
    val devices: LCEState<List<DeviceUiItem>> = LCEState.None(),
    /** Выбранное устройство в split-раскладке (master-detail). */
    val selectedId: String? = null,
    /** На стадии Scanning без найденных устройств прошло достаточно времени → показать «пусто». */
    val searchedLongEnough: Boolean = false
)
