package ru.alarmtrade.specmp.feature.device.firmware.ui.presentation.model

import org.jetbrains.compose.resources.StringResource
import ru.alarmtrade.specmp.feature.device.firmware.domain.model.FirmwareModuleKind
import specmp.feature.device.firmware_ui.generated.resources.Res
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_alarm_main
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_battery_charger
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_bluetooth_module
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_bmw_bypass
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_can_module
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_charging_station_tester
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_fuel_sensor
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_gps_tracker
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_microbot
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_remote_control
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_unknown
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_watch_bluetooth
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_watch_interface
import specmp.feature.device.firmware_ui.generated.resources.firmware_kind_watch_main

/** Название типа модуля («Базовый блок сигнализации» и т.д.). */
// Таблица соответствия типов: сложность — число веток, делить нечего
@Suppress("CyclomaticComplexMethod")
internal fun FirmwareModuleKind.titleRes(): StringResource = when (this) {
    FirmwareModuleKind.AlarmMain -> Res.string.firmware_kind_alarm_main
    FirmwareModuleKind.BluetoothModule -> Res.string.firmware_kind_bluetooth_module
    FirmwareModuleKind.BatteryCharger -> Res.string.firmware_kind_battery_charger
    FirmwareModuleKind.WatchMain -> Res.string.firmware_kind_watch_main
    FirmwareModuleKind.WatchInterface -> Res.string.firmware_kind_watch_interface
    FirmwareModuleKind.WatchBluetooth -> Res.string.firmware_kind_watch_bluetooth
    FirmwareModuleKind.RemoteControlMain -> Res.string.firmware_kind_remote_control
    FirmwareModuleKind.GpsTracker -> Res.string.firmware_kind_gps_tracker
    FirmwareModuleKind.FuelSensor -> Res.string.firmware_kind_fuel_sensor
    FirmwareModuleKind.BmwBypass -> Res.string.firmware_kind_bmw_bypass
    FirmwareModuleKind.ChargingStationTester -> Res.string.firmware_kind_charging_station_tester
    FirmwareModuleKind.CanModule -> Res.string.firmware_kind_can_module
    FirmwareModuleKind.Microbot -> Res.string.firmware_kind_microbot
    FirmwareModuleKind.Unknown -> Res.string.firmware_kind_unknown
}
