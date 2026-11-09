package ru.alarmtrade.specmp.feature.device.interaction.domain.session

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.alarmtrade.specmp.core.common.AuthCloseable

/**
 * SDK не отличает неверный PIN от блокировки, поэтому отсчёт [PIN_LOCKOUT] ведёт приложение: пока он идёт, в блок
 * ничего не шлём — иначе блок продлит блокировку. Монотонное время; перезапуск процесса отсчёт сбрасывает.
 */
class ServicePinCooldown(private val timeSource: TimeSource = TimeSource.Monotonic) : AuthCloseable {

    private val deadlines = MutableStateFlow<Map<Long, TimeMark>>(emptyMap())

    fun start(systemId: Long) {
        val deadline = timeSource.markNow() + PIN_LOCKOUT
        deadlines.update { it + (systemId to deadline) }
    }

    fun remainingNow(systemId: Long): Duration = deadlines.value[systemId]?.left() ?: Duration.ZERO

    /**
     * Остаток блокировки раз в [TICK]; дойдя до [Duration.ZERO], замолкает до нового [start] (поток не завершается).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun remaining(systemId: Long): Flow<Duration> = deadlines
        .map { it[systemId] }
        .distinctUntilChanged()
        .flatMapLatest { deadline ->
            if (deadline == null) {
                flowOf(Duration.ZERO)
            } else {
                flow {
                    while (true) {
                        val left = deadline.left()
                        emit(left)
                        if (left == Duration.ZERO) break
                        delay(TICK)
                    }
                }
            }
        }

    /** Округлено вверх до секунд — значение меняется раз в секунду, а не на каждый тик; `null` — ввод разрешён. */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun lockedFor(systemId: Flow<Long?>): Flow<Duration?> = systemId
        .distinctUntilChanged()
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                remaining(id)
                    .map { left -> left.takeIf { it > Duration.ZERO }?.ceilToSeconds() }
                    .distinctUntilChanged()
            }
        }

    override suspend fun release() {
        deadlines.update { emptyMap() }
    }

    private fun TimeMark.left(): Duration = (-elapsedNow()).coerceAtLeast(Duration.ZERO)

    private fun Duration.ceilToSeconds(): Duration =
        ((inWholeMilliseconds + MILLIS_IN_SECOND - 1) / MILLIS_IN_SECOND).seconds

    companion object {
        val PIN_LOCKOUT: Duration = 15.seconds

        private val TICK: Duration = 250.milliseconds
        private const val MILLIS_IN_SECOND = 1_000L
    }
}
