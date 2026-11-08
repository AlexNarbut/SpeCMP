package ru.alarmtrade.specmp.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.mp.KoinPlatformTools
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycle
import ru.alarmtrade.specmp.core.lifecycle.AppLifecycleEvent

/**
 * Прежняя база ViewModel: VM переносятся на [BaseViewModel] по подэтапам миграции, затем класс удаляется.
 *
 * - [logger] с тегом по имени конкретного класса.
 * - Корутины запускаются через [safeLaunch] вместо `viewModelScope.launch`:
 *   в контекст добавлен [CoroutineExceptionHandler], который логирует необработанные
 *   исключения. Ожидаемые ошибки сюда не попадают — они возвращаются как
 *   `Response.Error` и живут в state; handler ловит именно сбои контракта.
 * - Жизненный цикл приложения — колбэки [onAppForeground] / [onAppBackground] (модуль `core:lifecycle`).
 */
abstract class LegacyBaseViewModel : ViewModel() {

    protected val logger: Logger = Logger.withTag(this::class.simpleName ?: DEFAULT_LOG_TAG)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "Unhandled coroutine exception" }
    }

    private val proceedMutex = Mutex()

    init {
        // Жизненный цикл приложения (core:lifecycle) из Koin; без DI (unit-тесты) — без событий.
        // Подписка приостанавливается сразу, колбэки не вызываются до конца конструктора наследника.
        KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<AppLifecycle>()?.let { appLifecycle ->
            viewModelScope.launch(exceptionHandler) {
                appLifecycle.events.collect { event ->
                    when (event) {
                        AppLifecycleEvent.FOREGROUND -> onAppForeground()
                        AppLifecycleEvent.BACKGROUND -> onAppBackground()
                    }
                }
            }
        }
    }

    /**
     * Приложение вернулось на передний план (из системных настроек, другого приложения, после блокировки).
     * Не вызывается при открытии экрана и при возврате с другого экрана приложения.
     */
    protected open fun onAppForeground() = Unit

    /** Приложение ушло в фон. */
    protected open fun onAppBackground() = Unit

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(exceptionHandler, block = block)

    protected fun safeLaunch(coroutineContext: CoroutineContext, block: suspend CoroutineScope.() -> Unit): Job =
        viewModelScope.launch(coroutineContext + exceptionHandler, block = block)

    /**
     * Стартовая работа экрана: [block] запускается отдельной корутиной VM при первом старте этого flow — первой
     * подписке UI на state, — и ровно один раз за жизнь VM. Ставится на цепочку state перед `stateIn`:
     * `combine(...).launchOnFirstStart { load() }.flowOn(...).stateIn(WhileSubscribed(...))`.
     *
     * Почему не голый `onStart { load() }`: он (а) блокирует цепочку до конца загрузки — `combine` ещё не собирает,
     * и `Loading`, записанный в state, экран не увидит; (б) повторяется на каждый перезапуск `WhileSubscribed`
     * (возврат на экран после stopTimeout). Здесь работа — в своей корутине (`Job` с `LAZY`, `start()` идемпотентен),
     * а повторы и перезагрузки — интенты или потоки внутри [block] (`retries.collectLatest { load() }`).
     */
    protected fun <T> Flow<T>.launchOnFirstStart(block: suspend CoroutineScope.() -> Unit): Flow<T> {
        val job = viewModelScope.launch(exceptionHandler, start = CoroutineStart.LAZY, block = block)
        return onStart { job.start() }
    }

    /**
     * Выполняет [block] под общим для этой ViewModel mutex'ом (один lock на все вызовы
     * внутри одного VM — операции, обёрнутые в этот хелпер, не выполняются параллельно).
     *
     * - [needSkipAnother] = true (по умолчанию): если другой block уже выполняется,
     *   новый вызов молча пропускается — защита от дублей (двойной клик, повторный интент).
     * - [needSkipAnother] = false: вызов дожидается освобождения mutex'а и выполняется
     *   следом — сериализация операций.
     *
     * От отмены mutex НЕ защищает: block выполняется в корутине вызывающего и отменится
     * вместе с ней (например, при очистке viewModelScope). Работа, которая должна пережить
     * экран, выносится в app-scoped singleton (см. docs/patterns.md).
     */
    protected suspend fun executeWithProceedLock(needSkipAnother: Boolean = true, block: suspend () -> Unit) {
        if (needSkipAnother) {
            // tryLock атомарен: проверка isLocked + withLock допускала бы гонку,
            // при которой оба конкурентных вызова выполнились бы последовательно.
            if (!proceedMutex.tryLock()) return
            try {
                block()
            } finally {
                proceedMutex.unlock()
            }
        } else {
            proceedMutex.withLock {
                block()
            }
        }
    }

    private companion object {
        const val DEFAULT_LOG_TAG = "LegacyBaseViewModel"
    }
}
