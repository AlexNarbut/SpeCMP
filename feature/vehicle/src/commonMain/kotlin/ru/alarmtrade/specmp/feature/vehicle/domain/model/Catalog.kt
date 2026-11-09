package ru.alarmtrade.specmp.feature.vehicle.domain.model

data class CarBrand(val id: Int, val name: String, val logoUrl: String?)

data class CarModel(val id: Int, val name: String, val imageUrl: String?, val years: IntRange)

/** Комплектация модели — то, что выбирается («автомобиль» в терминах сервера, `carId`). */
data class CarTrim(val id: Int, val name: String)

data class CarSummary(
    val id: Int,
    val brand: String,
    val model: String,
    val name: String,
    val years: IntRange,
    val canCode: String,
    val imageUrl: String?,
)

fun CarSummary.fullName(): String = listOf(brand, model, name).filter { it.isNotBlank() }.distinct().joinToString(" ")
