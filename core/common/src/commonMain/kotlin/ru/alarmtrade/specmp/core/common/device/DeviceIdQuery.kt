package ru.alarmtrade.specmp.core.common.device

enum class DeviceIdQueryError { EMPTY, NOT_DIGITS, TOO_LONG }

/** ID устройства, как его ввели или отсканировали со штрих-кода (информация о товаре, паспорт без блока). */
sealed interface DeviceIdQuery {
    data class Valid(val id: Long) : DeviceIdQuery

    data class Invalid(val error: DeviceIdQueryError) : DeviceIdQuery

    companion object {
        private val Separators = Regex("""[\s\-–—]""")

        /** Пробелы и дефисы из скопированного штрих-кода убираются; номер — только цифры и влезает в `Long`. */
        fun parse(raw: String): DeviceIdQuery {
            val digits = raw.replace(Separators, "")
            return when {
                digits.isEmpty() -> Invalid(DeviceIdQueryError.EMPTY)
                !digits.all { it in '0'..'9' } -> Invalid(DeviceIdQueryError.NOT_DIGITS)
                else -> digits.toLongOrNull()?.let(::Valid) ?: Invalid(DeviceIdQueryError.TOO_LONG)
            }
        }
    }
}
