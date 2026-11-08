package ru.alarmtrade.specmp.feature.device.state.domain.model

/** Команда на блок с экрана проверки состояния. */
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

/** Ответ блока на команду. */
enum class CommandReply {
    DONE,

    /** Блок ответил, что команду не выполнил. */
    REJECTED,
}
