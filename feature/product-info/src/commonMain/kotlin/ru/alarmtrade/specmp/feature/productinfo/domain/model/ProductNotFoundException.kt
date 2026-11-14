package ru.alarmtrade.specmp.feature.productinfo.domain.model

/** Сервер не знает товар с таким номером. */
class ProductNotFoundException(val id: Long, cause: Throwable? = null) : Exception("Product not found: $id", cause)
