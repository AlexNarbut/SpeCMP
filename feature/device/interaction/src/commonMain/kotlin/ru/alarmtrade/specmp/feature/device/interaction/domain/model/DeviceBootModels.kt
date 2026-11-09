package ru.alarmtrade.specmp.feature.device.interaction.domain.model

enum class BootModeTarget { MainProgram, BluetoothModule }

/** Неверный PIN — не ошибка операции, а исход: экран обрабатывает его диалогом ввода PIN. */
enum class BootModeResult {
    /** Блок принял команду (или уже в загрузчике): дальше он перезагрузится. */
    Entered,

    WrongPin,
}

/** Связь при ней обрывается намеренно — экраны не показывают это как потерю связи. */
enum class DeviceReboot {
    ToBoot,

    ToMain,

    /** Выход из режима программирования (быстрый монтаж): ожидание рисует сам экран, flow остаётся в композиции. */
    Restart,
}
