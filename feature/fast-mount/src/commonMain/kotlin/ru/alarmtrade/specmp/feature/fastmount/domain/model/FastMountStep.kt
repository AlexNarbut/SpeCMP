package ru.alarmtrade.specmp.feature.fastmount.domain.model

/**
 * Шаг мастера быстрого монтажа — текущее состояние сессии SDK (`StateTitle`). Порядок шагов задаёт SDK, приложение
 * только вызывает «вперёд/назад».
 */
enum class FastMountStep {
    /** Запись настроек конфигурации в блок (`INITIAL_STATE`); GSM/GPRS блока сохраняются. */
    WRITING_SETTINGS,

    /** Монтаж по карте (`MOUNT_START`); «Далее» перезагружает блок (выход из режима программирования). */
    MOUNT,

    /** Переход между шагами с перезагрузкой блока (`WAITING`, `WAITING_REBOOT`); SDK переходит дальше сам. */
    REBOOTING,

    KEY_BYPASS,

    /** Только если обход вернул «нужен Clone». */
    CLONE,

    SLAVE,

    /** Проверка системы по вопросам конфигурации — только Bluetooth. */
    SYSTEM_CHECK,

    FEEDBACK,

    /** Монтаж завершён; отчёт уходит на сервер в фоне. */
    FINISH
}
