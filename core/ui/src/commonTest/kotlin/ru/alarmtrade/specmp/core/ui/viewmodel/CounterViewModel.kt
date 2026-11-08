package ru.alarmtrade.specmp.core.ui.viewmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
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
    override suspend fun onStart() {
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

    fun setQuery(query: String) {
        reduce { copy(query = query) }
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
        postSideEffect(CounterEffect(message))
    }
}
