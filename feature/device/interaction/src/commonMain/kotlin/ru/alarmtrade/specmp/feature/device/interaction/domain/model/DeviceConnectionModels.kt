package ru.alarmtrade.specmp.feature.device.interaction.domain.model

/**
 * Доменное состояние процесса подключения к устройству (BLE/USB),
 * скрывает SDK-типы `ConnectingStatus` / `UsbConnectingState`.
 */
enum class DeviceConnectionState {
    /** Идёт подключение/сопряжение (промежуточные статусы). */
    CONNECTING,

    /**
     * Сессия установлена — дальше устройством владеет
     * [DeviceSession][ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession].
     */
    CONNECTED,

    /** Подключение не удалось. */
    FAILED,

    /**
     * BLE: сопряжение не удалось — блок не в режиме сопряжения (старое сопряжение на телефоне SDK уже снял).
     * Монтажнику нужно перевести блок в режим сопряжения и повторить.
     */
    PAIRING_REQUIRED
}

/** Транспорт, по которому создана сессия. */
enum class ConnectionTransport { BLE, USB }
