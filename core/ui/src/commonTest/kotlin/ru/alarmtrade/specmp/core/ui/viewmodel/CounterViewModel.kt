package ru.alarmtrade.specmp.core.ui.viewmodel

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.alarmtrade.specmp.core.common.Response

internal class CounterViewModel(
    private val onStartWork: suspend () -> Unit = {},
    private val subscribedWork: (suspend () -> Unit)? = null,
    private val left: Flow<Int>? = null,
    private val right: Flow<Int>? = null,
    private val loadable: Flow<Response<Int>>? = null,
    private val search: ((String) -> Flow<String>)? = null,
) : BaseViewModel<CounterState, CounterEffect>(CounterState()) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun IntentScope.onStart() {
        onStartWork()
        left?.collectIntoState { value -> copy(left = value) }
        right?.collectIntoState { value -> copy(right = value) }
        loadable?.collectIntoState(onError = { e -> copy(loadError = e.message) }) { value ->
            copy(loaded = value, loadError = null)
        }
        search?.let { search ->
            select { query }.flatMapLatest(search).collectIntoState { value -> copy(found = value) }
        }
        subscribedWork?.let { repeatOnSubscription(it) }
    }

    fun increment() {
        intent { reduce { copy(count = count + 1) } }
    }

    fun addTwiceInBackground(release: CompletableDeferred<Unit>) {
        intent {
            val added = addOne()
            launch { release.await() }
            reduce { copy(left = added) }
            addOne()
        }
    }

    private suspend fun addOne(): Int = subIntent {
        reduce { copy(count = count + 1) }
        currentState.count
    }

    fun setQuery(query: String) {
        intent { reduce { copy(query = query) } }
    }

    fun retry() {
        reload()
    }

    fun launchKeyed(key: String, work: suspend () -> Unit) {
        launchReplacing(key) { work() }
    }

    fun cancelKeyed(key: String) {
        cancelLaunched(key)
    }

    fun send(message: String) {
        intent { postSideEffect(CounterEffect(message)) }
    }

    // Стресс-тест зовёт напрямую с потоков Dispatchers.Default, мимо viewModelScope (Main).
    @Suppress("ViewModelPublicApi")
    suspend fun proceedSkippingAnother(block: suspend () -> Unit) {
        executeWithProceedLock(needSkipAnother = true, block = block)
    }
}
