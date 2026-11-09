package ru.alarmtrade.specmp.feature.device.firmware.domain.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

/** @property bootTarget что переводим в загрузчик; `null` — на этом подключении модуль не обновить. */
data class FirmwareModule(
    val id: Long,
    val kind: FirmwareModuleKind,
    val systemName: String,
    val title: String,
    val bootTarget: BootModeTarget?,
) {
    val isBootSupported: Boolean get() = bootTarget != null
}

val FirmwareModule.displayTitle: String get() = "$title ($systemName)"

enum class FirmwareModuleKind {
    AlarmMain,
    BluetoothModule,
    BatteryCharger,
    WatchMain,
    WatchInterface,
    WatchBluetooth,
    RemoteControlMain,
    GpsTracker,
    FuelSensor,
    BmwBypass,
    ChargingStationTester,
    CanModule,
    Microbot,
    Unknown,
}
