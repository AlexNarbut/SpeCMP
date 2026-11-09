package ru.alarmtrade.specmp.feature.device.interaction.domain.model

enum class DeviceConnectionState {
    CONNECTING,

    CONNECTED,

    FAILED,

    /** Блок не в режиме сопряжения (старое сопряжение на телефоне SDK уже снял). */
    PAIRING_REQUIRED,
}

/** [SERIAL] — COM-порт (приложение прошивки): блок обслуживается как по USB. */
enum class ConnectionTransport { BLE, USB, SERIAL }
