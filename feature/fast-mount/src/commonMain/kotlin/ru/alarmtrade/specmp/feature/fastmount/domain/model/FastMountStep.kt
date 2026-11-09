package ru.alarmtrade.specmp.feature.fastmount.domain.model

/** Порядок шагов задаёт SDK (`StateTitle`), приложение только вызывает «вперёд/назад». */
enum class FastMountStep {
    /** Запись настроек конфигурации в блок (`INITIAL_STATE`); GSM/GPRS блока сохраняются. */
    WRITING_SETTINGS,

    /** Монтаж по карте (`MOUNT_START`); «Далее» перезагружает блок (выход из режима программирования). */
    MOUNT,

    /** Переход между шагами с перезагрузкой блока (`WAITING`, `WAITING_REBOOT`); SDK переходит дальше сам. */
    REBOOTING,

    KEY_BYPASS,

    CLONE,

    SLAVE,

    SYSTEM_CHECK,

    FEEDBACK,

    FINISH,
}
