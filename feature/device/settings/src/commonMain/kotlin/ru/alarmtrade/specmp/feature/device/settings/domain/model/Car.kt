package ru.alarmtrade.specmp.feature.device.settings.domain.model

/** Автомобиль из базы (для CAN-кода): марка, модель, поколение и годы выпуска. */
data class Car(val id: Int, val brand: String, val model: String, val name: String, val years: IntRange)
