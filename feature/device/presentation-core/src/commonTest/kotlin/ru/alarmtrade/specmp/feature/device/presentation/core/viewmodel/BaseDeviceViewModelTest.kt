package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState

class BaseDeviceViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val link = MutableStateFlow(DeviceLinkState.READY)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `device work is cancelled on loss, lost and ready callbacks rerun`() = runTest(dispatcher) {
        val viewModel = DeviceProbeViewModel(testDeviceSession(link, dispatcher))
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        viewModel.onReadCurrent()
        assertEquals(testDevice, viewModel.state.value.current)

        val cancelled = CompletableDeferred<Unit>()
        viewModel.onWork {
            try {
                awaitCancellation()
            } finally {
                cancelled.complete(Unit)
            }
        }
        assertEquals(true, viewModel.state.value.launched)
        assertFalse(cancelled.isCompleted)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertTrue(cancelled.isCompleted)
        viewModel.onWork {}
        assertEquals(false, viewModel.state.value.launched)
        viewModel.onReadCurrent()
        assertNull(viewModel.state.value.current)

        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        // Сессия стартует не в Ready — первый lost до подключения.
        assertEquals(listOf("lost", "ready:1", "lost", "ready:1"), viewModel.state.value.events)
        viewModel.onWork {}
        assertEquals(true, viewModel.state.value.launched)
    }
}
