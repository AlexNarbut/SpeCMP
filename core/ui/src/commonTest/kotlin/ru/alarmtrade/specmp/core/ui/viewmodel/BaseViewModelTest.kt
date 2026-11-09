package ru.alarmtrade.specmp.core.ui.viewmodel

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import ru.alarmtrade.specmp.core.common.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `reduce updates the state`() = runTest {
        val vm = CounterViewModel()
        subscribe(vm)

        vm.increment()
        vm.increment()

        assertEquals(2, vm.state.value.count)
    }

    @Test
    fun `onStart runs once across several subscriptions and resubscriptions`() = runTest {
        var starts = 0
        val vm = CounterViewModel(onStartWork = { starts++ })

        val first = subscribe(vm)
        val second = subscribe(vm)
        first.cancel()
        second.cancel()
        advanceTimeBy(STOP_TIMEOUT_PASSED)
        subscribe(vm).cancel()
        advanceTimeBy(STOP_TIMEOUT_PASSED)
        subscribe(vm)

        assertEquals(1, starts)
    }

    @Test
    fun `onStart does not run without a subscriber`() = runTest {
        var starts = 0
        CounterViewModel(onStartWork = { starts++ })

        runCurrent()

        assertEquals(0, starts)
    }

    @Test
    fun `repeatOnSubscription stops without subscribers and starts again on resubscription`() = runTest {
        var starts = 0
        var stops = 0
        val vm = CounterViewModel(
            subscribedWork = {
                starts++
                try {
                    awaitCancellation()
                } finally {
                    stops++
                }
            },
        )

        val subscription = subscribe(vm)
        assertEquals(1 to 0, starts to stops)

        subscription.cancel()
        advanceTimeBy(1.seconds)
        assertEquals(1 to 0, starts to stops, "keeps running within the stop timeout")

        advanceTimeBy(STOP_TIMEOUT_PASSED)
        assertEquals(1 to 1, starts to stops)

        subscribe(vm)
        assertEquals(2 to 1, starts to stops)
    }

    @Test
    fun `postSideEffect reaches the collector`() = runTest {
        val vm = CounterViewModel()
        val effects = mutableListOf<CounterEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.sideEffect.collect { effects += it } }

        vm.send("saved")

        assertEquals(listOf(CounterEffect("saved")), effects)
    }

    @Test
    fun `independent bindings update their own fields`() = runTest {
        val left = MutableStateFlow(1)
        val right = MutableStateFlow(2)
        val vm = CounterViewModel(left = left, right = right)
        subscribe(vm)
        assertEquals(CounterState(left = 1, right = 2), vm.state.value)

        left.value = 10

        assertEquals(CounterState(left = 10, right = 2), vm.state.value)
    }

    @Test
    fun `bindings stop without subscribers after the stop timeout and restart on resubscription`() = runTest {
        val left = MutableStateFlow(1)
        val right = MutableStateFlow(2)
        val vm = CounterViewModel(left = left, right = right)

        val subscription = subscribe(vm)
        assertEquals(1 to 1, left.subscriptionCount.value to right.subscriptionCount.value)

        subscription.cancel()
        advanceTimeBy(1.seconds)
        assertEquals(1 to 1, left.subscriptionCount.value to right.subscriptionCount.value)

        advanceTimeBy(STOP_TIMEOUT_PASSED)
        assertEquals(0 to 0, left.subscriptionCount.value to right.subscriptionCount.value)

        left.value = 5
        subscribe(vm)
        assertEquals(1 to 1, left.subscriptionCount.value to right.subscriptionCount.value)
        assertEquals(5, vm.state.value.left)
    }

    @Test
    fun `reduce from an intent and a binding on other fields do not overwrite each other`() = runTest {
        val left = MutableStateFlow(1)
        val vm = CounterViewModel(left = left)
        subscribe(vm)

        vm.increment()
        left.value = 7
        vm.increment()

        assertEquals(CounterState(count = 2, left = 7), vm.state.value)
    }

    @Test
    fun `response binding routes success to reducer and error to onError`() = runTest {
        val vm = CounterViewModel(
            loadable = flowOf(Response.Success(1), Response.Error(IllegalStateException("db closed"))),
        )
        subscribe(vm)

        assertEquals(CounterState(loaded = 1, loadError = "db closed"), vm.state.value)
    }

    @Test
    fun `reload resubscribes a binding that completed with an error`() = runTest {
        var subscriptions = 0
        val loadable = flow {
            subscriptions++
            if (subscriptions ==
                1
            ) {
                emit(Response.Error(IllegalStateException("db closed")))
            } else {
                emit(Response.Success(42))
            }
        }
        val vm = CounterViewModel(loadable = loadable)
        subscribe(vm)
        assertEquals(CounterState(loadError = "db closed"), vm.state.value)

        vm.retry()

        assertEquals(2, subscriptions)
        assertEquals(CounterState(loaded = 42), vm.state.value)
    }

    @Test
    fun `reload restarts every binding once and keeps them tied to subscription`() = runTest {
        var leftStarts = 0
        val left = MutableStateFlow(1)
        val vm = CounterViewModel(left = left.onStart { leftStarts++ })
        val subscription = subscribe(vm)

        vm.retry()
        assertEquals(2, leftStarts)
        assertEquals(1, left.subscriptionCount.value)

        subscription.cancel()
        advanceTimeBy(STOP_TIMEOUT_PASSED)
        vm.retry()
        assertEquals(2 to 0, leftStarts to left.subscriptionCount.value, "reload without subscribers does not collect")
    }

    @Test
    fun `select restarts the derived subscription only when its part of the state changes`() = runTest {
        val requests = mutableListOf<String>()
        val vm = CounterViewModel(search = { query -> flowOf("found:$query").onStart { requests += query } })
        subscribe(vm)
        assertEquals(listOf(""), requests)
        assertEquals("found:", vm.state.value.found)

        vm.setQuery("milk")
        vm.setQuery("milk")
        vm.increment()

        assertEquals(listOf("", "milk"), requests, "same value and other fields do not restart the subscription")
        assertEquals("found:milk", vm.state.value.found)
    }

    @Test
    fun `select based binding is restarted by reload with the current value and stops without subscribers`() = runTest {
        val requests = mutableListOf<String>()
        val upstream = MutableStateFlow(0)
        val vm = CounterViewModel(
            search = { query -> upstream.map { "$query#$it" }.onStart { requests += query } },
        )
        val subscription = subscribe(vm)
        vm.setQuery("pear")

        vm.retry()
        assertEquals(listOf("", "pear", "pear"), requests)
        assertEquals(1, upstream.subscriptionCount.value)

        subscription.cancel()
        advanceTimeBy(STOP_TIMEOUT_PASSED)
        assertEquals(0, upstream.subscriptionCount.value)

        vm.setQuery("plum")
        upstream.value = 3
        subscribe(vm)
        assertEquals(listOf("", "pear", "pear", "plum"), requests)
        assertEquals("plum#3", vm.state.value.found)
    }

    @Test
    fun `launchReplacing cancels the previous job with the same key only`() = runTest {
        val vm = CounterViewModel()
        val log = mutableListOf<String>()
        fun worker(name: String): suspend () -> Unit = {
            try {
                awaitCancellation()
            } finally {
                log += "$name cancelled"
            }
        }

        vm.launchKeyed("poll", worker("poll-1"))
        vm.launchKeyed("deadline", worker("deadline-1"))
        vm.launchKeyed("poll", worker("poll-2"))

        assertEquals(listOf("poll-1 cancelled"), log)

        vm.cancelKeyed("deadline")
        assertEquals(listOf("poll-1 cancelled", "deadline-1 cancelled"), log)
    }

    @Test
    fun `a finished keyed job does not affect its replacement - cancelLaunched cancels the current one`() = runTest {
        val vm = CounterViewModel()
        val log = mutableListOf<String>()

        vm.launchKeyed("timer") {
            delay(100.milliseconds)
            log += "first done"
        }
        advanceTimeBy(200.milliseconds)
        vm.launchKeyed("timer") {
            try {
                awaitCancellation()
            } finally {
                log += "second cancelled"
            }
        }
        vm.cancelKeyed("timer")
        vm.cancelKeyed("timer")

        assertEquals(listOf("first done", "second cancelled"), log)
    }

    @Test
    fun `executeWithProceedLock skipping another runs the block once under parallel calls`() = runTest {
        val vm = CounterViewModel()
        val gate = CompletableDeferred<Unit>()
        val runs = MutableStateFlow(0)
        val finished = MutableStateFlow(0)

        withContext(Dispatchers.Default) {
            repeat(PARALLEL_CALLS) {
                launch {
                    vm.proceedSkippingAnother {
                        runs.update { it + 1 }
                        gate.await()
                    }
                    finished.update { it + 1 }
                }
            }
            finished.first { it == PARALLEL_CALLS - 1 }
            gate.complete(Unit)
        }

        assertEquals(1, runs.value)
    }

    private fun TestScope.subscribe(vm: CounterViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }

    private companion object {
        const val PARALLEL_CALLS = 32
        val STOP_TIMEOUT_PASSED = 5.seconds + 1.milliseconds
    }
}
