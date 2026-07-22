package ru.alarmtrade.specmp.feature.device.interaction.domain.model

/**
 * Доменное состояние процесса подключения к устройству (BLE/USB),
 * скрывает SDK-типы `ConnectingStatus` / `UsbConnectingState`.
 */
enum class DeviceConnectionState {
    /** Идёт подключение/сопряжение (промежуточные статусы). */
    CONNECTING,

    /** Сессия установлена — можно читать информацию об устройстве. */
    CONNECTED,

    /** Подключение не удалось. */
    FAILED,
}

/** Краткая доменная информация о подключённом рабочем устройстве. */
data class AlarmDeviceInfo(
    val systemId: String,
    val systemName: String,
    val firmwareVersion: String,
)

/** Данные устройства в режиме загрузчика (BOOT). */
data class BootData(
    val deviceName: String,
    val firmwareName: String,
    val firmwareVersion: String,
)

/** Итог подключения — что делать дальше на уровне навигации приложения. */
sealed interface DeviceConnectionResult {
    /** Устройство подключено в рабочем режиме. */
    data class Working(val device: AlarmDeviceInfo) : DeviceConnectionResult

    /** Устройство подключено в режиме загрузчика. */
    data class Boot(val bootData: BootData) : DeviceConnectionResult
}
