package ru.alarmtrade.specmp.core.specialist

/**
 * Координаты для отчётов SDK (старт быстрого монтажа) — **без запроса разрешения и без UI**: есть разрешение и
 * известное положение — координаты, иначе `null` (SDK отправит `0,0`). Реализацию регистрирует composition root
 * там, где она есть (Android); нет реализации — дефолт SDK (`null`).
 */
fun interface SilentLocationProvider {
    /** Широта и долгота или `null`. */
    suspend fun lastKnown(): Pair<Float, Float>?
}
