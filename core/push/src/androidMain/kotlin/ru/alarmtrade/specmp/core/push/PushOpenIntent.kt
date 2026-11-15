package ru.alarmtrade.specmp.core.push

import android.content.Intent

/** Тап по уведомлению из `onCreate` / `onNewIntent` launcher-активити; открытие из «Недавних» — не тап. */
fun PushOpenRequests.offer(intent: Intent?) {
    val extras = intent?.extras ?: return
    if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return
    @Suppress("DEPRECATION") // типизированный get(key, Class) — с API 33, нужно любое значение-строку
    val data = extras.keySet().mapNotNull { key -> (extras.get(key) as? String)?.let { key to it } }.toMap()
    offer(data)
}
