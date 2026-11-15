package ru.alarmtrade.specmp.core.push

private const val CODE_NOTIFICATION = 1
private const val CODE_WARNING = 4
private const val CODE_CHAT = 5

/** Поддерживаемые типы push (`type_id` сервера). Новости (2), видео (3) и статьи (6) не показываем. */
enum class PushType(val code: Int) {
    NOTIFICATION(CODE_NOTIFICATION),
    WARNING(CODE_WARNING),
    CHAT(CODE_CHAT),
}
