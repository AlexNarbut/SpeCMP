package ru.alarmtrade.specmp.core.push

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Тап по уведомлению, ещё не обработанный навигацией. Объект процесса, а не Koin: Android-активити кладёт тап
 * в `onCreate` до старта DI (Koin поднимается внутри композиции).
 */
object PushOpenRequests {

    private val _pending = MutableStateFlow<PushTarget?>(null)
    val pending: StateFlow<PushTarget?> = _pending.asStateFlow()

    /** Данные push из intent / userInfo; запуск не из уведомления игнорируется. Последний тап заменяет прежний. */
    fun offer(data: Map<String, String>) {
        val target = PushPayload.targetOf(data) ?: return
        _pending.update { target }
    }

    /** Тап без данных push (desktop: уведомление показало само приложение). */
    fun open(target: PushTarget) {
        _pending.update { target }
    }

    /** Забрать [target], если он ещё ждёт: повторный сбор того же тапа его не выполнит. */
    fun consume(target: PushTarget): Boolean = _pending.compareAndSet(target, null)

    fun clear() {
        _pending.update { null }
    }
}
