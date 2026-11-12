package ru.alarmtrade.specmp.feature.device.peripherals.ui.presentation.model

import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralLink
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.PeripheralType
import ru.alarmtrade.specmp.feature.device.peripherals.domain.model.SystemPeripheral

/** Классы сигнала из макета: 4 — отличный, 1 — очень слабый. */
internal enum class SignalClass { VERY_WEAK, WEAK, GOOD, EXCELLENT }

private const val EXCELLENT_FROM = -60
private const val GOOD_FROM = -75
private const val WEAK_FROM = -85

internal fun signalClassOf(dbm: Int): SignalClass = when {
    dbm >= EXCELLENT_FROM -> SignalClass.EXCELLENT
    dbm >= GOOD_FROM -> SignalClass.GOOD
    dbm >= WEAK_FROM -> SignalClass.WEAK
    else -> SignalClass.VERY_WEAK
}

/** Заряд батарейки по напряжению; `null` — питание не от батарейки (бортовая сеть) или напряжение неизвестно. */
internal fun batteryPercent(type: PeripheralType, voltage: Float?): Int? {
    if (voltage == null || voltage > BATTERY_MAX_VOLTAGE) return null
    val (empty, full) = if (type == PeripheralType.FUEL_SENSOR) LITHIUM_RANGE else COIN_CELL_RANGE
    val share = ((voltage - empty) / (full - empty)).coerceIn(0f, 1f)
    return (share * PERCENT).toInt()
}

internal fun batteryLevel(percent: Int): Int = (percent / LEVEL_STEP + 1).coerceIn(1, LEVELS)

internal fun SystemPeripheral.batteryLow(): Boolean =
    lowBattery || (batteryPercent(type, voltage)?.let { it < LOW_PERCENT } ?: false)

internal fun SystemPeripheral.needsAttention(): Boolean = link == PeripheralLink.OFFLINE ||
    link == PeripheralLink.FAR ||
    batteryLow() ||
    signalDbm?.let { signalClassOf(it) == SignalClass.VERY_WEAK } == true

private const val BATTERY_MAX_VOLTAGE = 3.7f
private val COIN_CELL_RANGE = 2.2f to 3.0f
private val LITHIUM_RANGE = 3.0f to 3.6f
private const val PERCENT = 100
private const val LOW_PERCENT = 15
private const val LEVELS = 4
private const val LEVEL_STEP = 25
