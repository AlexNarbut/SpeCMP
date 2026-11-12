package ru.alarmtrade.specmp.feature.device.firmware.domain.model

/** Откуда известна установленная версия модуля. */
enum class InstalledVersionSource { DEVICE, BLUETOOTH_MODULE, NONE }

val FirmwareModuleKind.installedVersionSource: InstalledVersionSource
    get() = when (this) {
        FirmwareModuleKind.AlarmMain,
        FirmwareModuleKind.WatchMain,
        FirmwareModuleKind.GpsTracker,
        FirmwareModuleKind.BmwBypass,
        FirmwareModuleKind.RemoteControlMain,
        -> InstalledVersionSource.DEVICE

        FirmwareModuleKind.BluetoothModule -> InstalledVersionSource.BLUETOOTH_MODULE

        FirmwareModuleKind.BatteryCharger,
        FirmwareModuleKind.WatchInterface,
        FirmwareModuleKind.WatchBluetooth,
        FirmwareModuleKind.FuelSensor,
        FirmwareModuleKind.ChargingStationTester,
        FirmwareModuleKind.CanModule,
        FirmwareModuleKind.Microbot,
        FirmwareModuleKind.Unknown,
        -> InstalledVersionSource.NONE
    }

/** CAN-фильтр — только у основной программы сигнализации: прошивки остальных модулей к CAN-коду не привязаны. */
fun FirmwareModuleKind.canFilter(deviceCan: FirmwareCan?): FirmwareCan? =
    deviceCan.takeIf { this == FirmwareModuleKind.AlarmMain }

/** Версия на сервере — обновление, если она новее установленной или установленная неизвестна. */
fun FirmwareVersion.isUpdateFor(installed: String?): Boolean =
    installed == null || version.relationTo(installed) == VersionRelation.Newer
