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
 * Блокировка ввода PIN программирования на блоке после неверной попытки: [PIN_LOCKOUT] блок не принимает новый ввод.
 * SDK не умеет отличать неверный PIN от блокировки (оба — «неверный PIN»), поэтому отсчёт ведёт приложение:
 * пока он идёт, в блок ничего не отправляем, иначе блок сам продлит блокировку. Считается по блоку ([systemId]).
 * Монотонное время; перезапуск процесса отсчёт сбрасывает (блок ответит неверно ещё раз — отсчёт начнётся заново).
 */
class ServicePinCooldown(private val timeSource: TimeSource = TimeSource.Monotonic) : AuthCloseable {

    private val deadlines = MutableStateFlow<Map<Long, TimeMark>>(emptyMap())

    /** Неверный PIN у блока [systemId]: запустить (или перезапустить) отсчёт. */
    fun start(systemId: Long) {
        val deadline = timeSource.markNow() + PIN_LOCKOUT
        deadlines.update { it + (systemId to deadline) }
    }

    /** Сколько осталось ждать сейчас; [Duration.ZERO] — ввод разрешён. */
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

    /**
     * Блокировка для открытого диалога PIN: остаток по блоку из [systemId], округлённый вверх до целых секунд (диалог
     * показывает секунды — значение меняется раз в секунду, а не на каждый тик); `null` — ввод разрешён или блока нет
     * ([systemId] выдаёт `null`, например пока диалог закрыт).
     */
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
        /** Блокировка ввода на блоке после неверного PIN. */
        val PIN_LOCKOUT: Duration = 15.seconds

        private val TICK: Duration = 250.milliseconds
        private const val MILLIS_IN_SECOND = 1_000L
    }
}
