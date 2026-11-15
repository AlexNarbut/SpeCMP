package ru.alarmtrade.specmp.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmName
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onFailure
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.alarmtrade.specmp.core.common.Response

/** Контракт и правила использования — docs/patterns.md, «BaseViewModel». */
abstract class BaseViewModel<S : Any, E : Any>(initialState: S) : ViewModel() {

    protected val logger: Logger = Logger.withTag(this::class.simpleName ?: DEFAULT_LOG_TAG)

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.e(throwable) { "Unhandled coroutine exception" }
    }

    private val proceedMutex = Mutex()

    private val keyedJobs = MutableStateFlow<Map<Any, Job>>(emptyMap())

    // Стартовая работа — своей корутиной (LAZY, `start()` идемпотентен): цепочка state не ждёт её конца, и повторный
    // старт `WhileSubscribed` после возврата на экран её не перезапускает.
    private val startJob = viewModelScope.launch(exceptionHandler, start = CoroutineStart.LAZY) {
        IntentScope(this.coroutineContext).onStart()
    }

    private val _state = MutableStateFlow(initialState)
    private val isSubscribed = MutableStateFlow(false)
    private val reloadRequests = MutableStateFlow(0)

    // Как FAN_OUT в Orbit: без подписчика (экран ниже STARTED) эффекты копятся в буфере, каждый получает один сборщик.
    private val _sideEffect = Channel<E>(Channel.BUFFERED)

    val state: StateFlow<S> = _state
        .onStart { isSubscribed.update { true } }
        .onCompletion { isSubscribed.update { false } }
        .onStart { startJob.start() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), initialState)

    val sideEffect: Flow<E> = _sideEffect.receiveAsFlow()

    protected val currentState: S get() = _state.value

    // Как Syntax в Orbit: reduce и postSideEffect есть только у получателя корутин VM (intent, onStart и т. п.).
    protected inner class IntentScope internal constructor(override val coroutineContext: CoroutineContext) :
        CoroutineScope {
        fun reduce(transform: S.() -> S) {
            _state.update(transform)
        }

        fun postSideEffect(effect: E) {
            _sideEffect.trySend(effect).onFailure { logger.w { "Side effect dropped, buffer is full: $effect" } }
        }
    }

    // Как subIntent в Orbit: хелпер интента без своей корутины; launch внутри — ребёнок вызывающего, без ожидания.
    protected suspend fun <T> subIntent(block: suspend IntentScope.() -> T): T =
        IntentScope(currentCoroutineContext()).block()

    /** Стартует на Main: вызываемые функции сами переключают dispatcher (`withContext(dispatchers.io)`). */
    protected open suspend fun IntentScope.onStart() = Unit

    protected fun intent(block: suspend IntentScope.() -> Unit): Job =
        viewModelScope.launch(exceptionHandler) { IntentScope(this.coroutineContext).block() }

    protected fun intent(coroutineContext: CoroutineContext, block: suspend IntentScope.() -> Unit): Job =
        viewModelScope.launch(coroutineContext + exceptionHandler) { IntentScope(this.coroutineContext).block() }

    /** Отменяет предыдущую корутину с тем же [key]; завершившаяся снимает себя с ключа, не трогая заменившую. */
    protected fun launchReplacing(
        key: Any,
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        block: suspend IntentScope.() -> Unit,
    ): Job {
        // LAZY: на Main.immediate тело стартует синхронно — ключ должен указывать на job до первой строки тела.
        val job = viewModelScope.launch(coroutineContext + exceptionHandler, start = CoroutineStart.LAZY) {
            IntentScope(this.coroutineContext).block()
        }
        keyedJobs.getAndUpdate { it + (key to job) }[key]?.cancel()
        job.invokeOnCompletion { keyedJobs.update { jobs -> if (jobs[key] === job) jobs - key else jobs } }
        job.start()
        return job
    }

    protected fun cancelLaunched(key: Any) {
        keyedJobs.getAndUpdate { it - key }[key]?.cancel()
    }

    // [needSkipAnother] = true — вызов во время другого block молча пропускается (двойной клик), false — ждёт
    // и выполняется следом. От отмены не защищает.
    protected suspend fun executeWithProceedLock(needSkipAnother: Boolean = true, block: suspend () -> Unit) {
        if (needSkipAnother) {
            // tryLock атомарен: проверка isLocked + withLock пропустила бы оба конкурентных вызова по очереди.
            if (!proceedMutex.tryLock()) return
            try {
                block()
            } finally {
                proceedMutex.unlock()
            }
        } else {
            proceedMutex.withLock { block() }
        }
    }

    // Поток, отдающий state целиком, должен быть единственным источником state; иначе — `copy(...)` только своих полей.
    protected fun <T> Flow<T>.collectIntoState(reducer: S.(T) -> S): Job =
        bind(this) { value -> _state.update { it.reducer(value) } }

    /** После `Error` поток от `safeFlowRun` завершён; «Повторить» — [reload]. */
    protected fun <T> Flow<Response<T>>.collectIntoState(onError: S.(Throwable) -> S, reducer: S.(T) -> S): Job =
        bind(this) { response ->
            when (response) {
                is Response.Success -> _state.update { it.reducer(response.value) }
                is Response.Error -> _state.update { it.onError(response.exception) }
            }
        }

    /** Не даёт привязке без `onError` молча принять `Flow<Response<T>>` и отдать в reducer сырой `Response`. */
    @Deprecated(
        message = "Flow<Response<T>> привязывается с явным onError: collectIntoState(onError = { … }) { value -> … }",
        level = DeprecationLevel.ERROR,
    )
    @JvmName("collectResponseIntoStateWithoutOnError")
    protected fun <T> Flow<Response<T>>.collectIntoState(reducer: S.(Response<T>) -> S): Job =
        bind(this) { value -> _state.update { it.reducer(value) } }

    protected fun <T> select(selector: S.() -> T): Flow<T> = _state.map { it.selector() }.distinctUntilChanged()

    /** Переподписывает все привязки [collectIntoState]. */
    protected fun reload() {
        reloadRequests.update { it + 1 }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> bind(flow: Flow<T>, onValue: (T) -> Unit): Job = intent {
        repeatOnSubscription { reloadRequests.flatMapLatest { flow }.collect { value -> onValue(value) } }
    }

    /** Отменяет [block], когда у [state] не остаётся подписчиков, и запускает заново при следующей подписке. */
    protected suspend fun repeatOnSubscription(block: suspend () -> Unit) {
        isSubscribed.collectLatest { subscribed -> if (subscribed) block() }
    }

    private companion object {
        const val DEFAULT_LOG_TAG = "BaseViewModel"
        val STOP_TIMEOUT = 5.seconds
    }
}
