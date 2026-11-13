package ru.alarmtrade.specmp.feature.techrequests.domain.model

enum class TechRequestFailure {
    /** Слишком много незакрытых заявок. */
    Limit,

    /** Заявка не найдена на сервере. */
    NotFound,

    /** Не удалось прочитать информацию или настройки блока перед отправкой. */
    DeviceRead,
}

class TechRequestException(val failure: TechRequestFailure, cause: Throwable) :
    Exception("Tech request failure: $failure", cause)
