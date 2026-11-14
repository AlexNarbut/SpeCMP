package ru.alarmtrade.specmp.core.barcode

/**
 * Принимает код, только если то же значение прочитано в двух кадрах подряд не дольше [windowMillis]: одиночное
 * ложное чтение размытого штрих-кода в поиск не уходит.
 */
internal class ScanStabilizer(private val windowMillis: Long = DEFAULT_WINDOW_MILLIS) {
    private var last: String? = null
    private var lastAt: Long = 0

    fun offer(value: String, nowMillis: Long): Boolean {
        val confirmed = value == last && nowMillis - lastAt <= windowMillis
        last = value
        lastAt = nowMillis
        return confirmed
    }

    private companion object {
        const val DEFAULT_WINDOW_MILLIS = 1_000L
    }
}
