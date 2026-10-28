package ru.alarmtrade.specmp.feature.vehicle.domain.model

/** Марка. [logoUrl] `null` — логотипа нет. */
data class CarBrand(val id: Int, val name: String, val logoUrl: String?)

/** Модель марки с годами выпуска (бывает один год). */
data class CarModel(val id: Int, val name: String, val imageUrl: String?, val years: IntRange)

/** Комплектация модели — то, что выбирается («автомобиль» в терминах сервера, `carId`). */
data class CarTrim(val id: Int, val name: String)

/**
 * Автомобиль одной строкой: результат выбора, строка поиска по CAN-коду, недавние, подпись CAN-кода в настройках.
 */
data class CarSummary(
    val id: Int,
    val brand: String,
    val model: String,
    val name: String,
    val years: IntRange,
    val canCode: String,
    val imageUrl: String?
)
