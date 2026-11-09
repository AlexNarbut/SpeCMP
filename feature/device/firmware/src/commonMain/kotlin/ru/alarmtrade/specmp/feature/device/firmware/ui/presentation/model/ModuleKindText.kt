package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model

import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.core.localization.resources.Strings
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_alarm_main
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_battery_charger
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_bluetooth_module
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_bmw_bypass
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_can_module
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_charging_station_tester
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_fuel_sensor
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_gps_tracker
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_microbot
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_remote_control
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_unknown
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_watch_bluetooth
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_watch_interface
import ru.alarmtrade.specmp.core.localization.resources.firmware_common_value_kind_watch_main
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind

// Таблица соответствия типов: сложность — число веток, делить нечего
@Suppress("CyclomaticComplexMethod")
internal fun FirmwareModuleKind.titleRes(): StringResource = when (this) {
    FirmwareModuleKind.AlarmMain -> Strings.string.firmware_common_value_kind_alarm_main
    FirmwareModuleKind.BluetoothModule -> Strings.string.firmware_common_value_kind_bluetooth_module
    FirmwareModuleKind.BatteryCharger -> Strings.string.firmware_common_value_kind_battery_charger
    FirmwareModuleKind.WatchMain -> Strings.string.firmware_common_value_kind_watch_main
    FirmwareModuleKind.WatchInterface -> Strings.string.firmware_common_value_kind_watch_interface
    FirmwareModuleKind.WatchBluetooth -> Strings.string.firmware_common_value_kind_watch_bluetooth
    FirmwareModuleKind.RemoteControlMain -> Strings.string.firmware_common_value_kind_remote_control
    FirmwareModuleKind.GpsTracker -> Strings.string.firmware_common_value_kind_gps_tracker
    FirmwareModuleKind.FuelSensor -> Strings.string.firmware_common_value_kind_fuel_sensor
    FirmwareModuleKind.BmwBypass -> Strings.string.firmware_common_value_kind_bmw_bypass
    FirmwareModuleKind.ChargingStationTester -> Strings.string.firmware_common_value_kind_charging_station_tester
    FirmwareModuleKind.CanModule -> Strings.string.firmware_common_value_kind_can_module
    FirmwareModuleKind.Microbot -> Strings.string.firmware_common_value_kind_microbot
    FirmwareModuleKind.Unknown -> Strings.string.firmware_common_value_kind_unknown
}
