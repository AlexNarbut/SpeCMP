package ru.alarmtrade.specmp.feature.device.presentation.core.viewmodel

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

class LegacyBaseDeviceViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val device = testDevice
    private val link = MutableStateFlow(DeviceLinkState.READY)

    private class TestViewModel(deviceSession: DeviceSession) : LegacyBaseDeviceViewModel(deviceSession) {
        val readyCalls = mutableListOf<Long>()

        init {
            onEachDeviceReady { readyCalls += it.systemId }
        }

        fun work(block: suspend CoroutineScope.() -> Unit): Job? = launchOnDevice(block)

        fun device(): AlarmDevice? = currentDevice
    }

    private fun TestScope.viewModel() = TestViewModel(testDeviceSession(link, dispatcher))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `device work is cancelled on disconnect and onReady reruns on reconnect`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(device, viewModel.device())

        val never = CompletableDeferred<Unit>()
        val job = viewModel.work { never.await() }
        assertTrue(job != null && job.isActive)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertTrue(job.isCancelled)
        assertNull(viewModel.work { })
        assertNull(viewModel.device())

        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(listOf(device.systemId, device.systemId), viewModel.readyCalls)
        assertTrue(viewModel.work { } != null)
    }
}
