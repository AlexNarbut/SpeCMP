package ru.alarmtrade.specmp.core.specialist

/** Без запроса разрешения и UI; `null` — SDK отправит `0,0`. */
fun interface SilentLocationProvider {
    suspend fun lastKnown(): Pair<Float, Float>?
}
