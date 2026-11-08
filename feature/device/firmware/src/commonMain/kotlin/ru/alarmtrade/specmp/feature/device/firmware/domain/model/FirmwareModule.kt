package ru.alarmtrade.specmp.feature.device.firmware.domain.model

import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget

/**
 * Обновляемый модуль блока (подсистема): основной блок, Bluetooth-модуль, часы и т.д.
 *
 * @property bootTarget что переводим в загрузчик для обновления; `null` — на этом подключении приложение
 *   обновить модуль не может («Не поддерживается»).
 */
data class FirmwareModule(
    val id: Long,
    val kind: FirmwareModuleKind,
    val systemName: String,
    val title: String,
    val bootTarget: BootModeTarget?,
) {
    val isBootSupported: Boolean get() = bootTarget != null
}

/** Подпись модуля «Основная программа (DX-9X)». */
val FirmwareModule.displayTitle: String get() = "$title ($systemName)"

/** Тип модуля — по нему UI подбирает название типа и иллюстрацию. */
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
