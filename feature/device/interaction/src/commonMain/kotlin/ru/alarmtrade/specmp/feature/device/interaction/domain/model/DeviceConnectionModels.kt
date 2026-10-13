package ru.alarmtrade.specmp.feature.device.interaction.domain.model

/**
 * Доменное состояние процесса подключения к устройству (BLE/USB),
 * скрывает SDK-типы `ConnectingStatus` / `UsbConnectingState`.
 */
enum class DeviceConnectionState {
    /** Идёт подключение/сопряжение (промежуточные статусы). */
    CONNECTING,

    /** Сессия установлена — дальше устройством владеет [DeviceSession][ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession]. */
    CONNECTED,

    /** Подключение не удалось. */
    FAILED,
}

/** Транспорт, по которому создана сессия. */
enum class ConnectionTransport { BLE, USB }

/** Результат поиска: сессия с устройством создана — дальше flow работы с устройством. */
data class ConnectedDevice(
    val id: String,
    val name: String,
    val transport: ConnectionTransport,
)
