package ru.alarmtrade.specmp.feature.device.state.domain.model

enum class StateCommand {
    ARM,
    DISARM,
    START_ENGINE,
    EXTEND_AUTOSTART,
    STOP_ENGINE,
    OPEN_TRUNK,
    HEATER_ON,
    HEATER_OFF,

    /** Переключатель: SDK шлёт одну команду и на включение, и на выключение. */
    SERVICE_MODE_ON,
    SERVICE_MODE_OFF,
    PROGRAM_MODE_ON,
    PROGRAM_MODE_OFF,
    PANIC,
    LIGHT_WAY,
}

enum class CommandReply {
    DONE,

    REJECTED,
}
