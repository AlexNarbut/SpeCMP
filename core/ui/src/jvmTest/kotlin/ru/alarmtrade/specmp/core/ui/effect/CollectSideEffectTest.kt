package ru.alarmtrade.specmp.core.ui.effect

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarDuration
import ru.alarmtrade.specmp.core.ui.view.dialog.CmpSnackbarHostState

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CollectSideEffectTest {

    // repeatOnLifecycle переключается на Dispatchers.Main, а в jvmTest платформенного Main нет.
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `close is handled at once while a launched snackbar is shown`() = runComposeUiTest {
        val effects = Channel<Effect>(Channel.BUFFERED)
        val snackbar = CmpSnackbarHostState()
        var closes = 0
        setContent {
            val scope = rememberCoroutineScope()
            effects.receiveAsFlow().collectSideEffect { effect ->
                when (effect) {
                    Effect.CommandFinished -> scope.launch { snackbar.awaitIndefinite() }
                    Effect.Close -> closes++
                }
            }
        }

        effects.trySend(Effect.CommandFinished)
        waitForIdle()
        effects.trySend(Effect.Close)
        waitForIdle()

        assertEquals(1, closes)
        assertNotNull(snackbar.delegate.currentSnackbarData, "snackbar is still shown")
    }

    @Test
    fun `handlers run one by one so an awaited snackbar holds the next effect`() = runComposeUiTest {
        val effects = Channel<Effect>(Channel.BUFFERED)
        val snackbar = CmpSnackbarHostState()
        var closes = 0
        setContent {
            effects.receiveAsFlow().collectSideEffect { effect ->
                when (effect) {
                    Effect.CommandFinished -> snackbar.awaitIndefinite()
                    Effect.Close -> closes++
                }
            }
        }

        effects.trySend(Effect.CommandFinished)
        effects.trySend(Effect.Close)
        waitForIdle()
        assertEquals(0, closes)

        snackbar.delegate.currentSnackbarData?.dismiss()
        waitForIdle()
        assertEquals(1, closes)
    }

    private suspend fun CmpSnackbarHostState.awaitIndefinite() {
        showSnackbar("done", duration = CmpSnackbarDuration.Indefinite)
    }

    private enum class Effect { CommandFinished, Close }
}
