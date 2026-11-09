package ru.alarmtrade.specmp.feature.device.interaction.ui.presentation.host

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceReboot
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceSessionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.service.DeviceSession
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession

class DeviceInteractionViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val device = AlarmDevice(1, "DX4G", "3.57", DeviceOperationMode.MAIN, emptySet(), can = null)

    private val sessionRepository = FakeDeviceSessionRepository(device)
    private val link = sessionRepository.link
    private val connectionRepository = FakeDeviceConnectionRepository()

    private fun TestScope.session() =
        testDeviceSession(sessionRepository, connectionRepository, dispatcher = dispatcher)

    private fun TestScope.viewModel(session: DeviceSession = session()): DeviceInteractionViewModel {
        val viewModel = DeviceInteractionViewModel(session)
        backgroundScope.launch { viewModel.state.collect {} }
        return viewModel
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `keeps last device while disconnected`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()

        assertEquals(DeviceSessionState.Disconnected, viewModel.state.value.session)
        assertEquals(device, viewModel.state.value.device)
    }

    @Test
    fun `exit closes session and emits Exit`() = runTest(dispatcher) {
        val viewModel = viewModel()
        val effects = mutableListOf<DeviceInteractionEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffect.collect { effects += it } }

        viewModel.onExit()
        viewModel.onExit()
        advanceUntilIdle()

        assertEquals(1, connectionRepository.closeCount)
        assertEquals(listOf<DeviceInteractionEffect>(DeviceInteractionEffect.Exit), effects)
    }

    @Test
    fun `bootloader drops the child flow and return to main brings it back`() = runTest(dispatcher) {
        val viewModel = viewModel()
        advanceUntilIdle()
        assertEquals(device, viewModel.state.value.device)

        val bootDevice = device.copy(mode = DeviceOperationMode.BOOT)
        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertEquals(device, viewModel.state.value.device)
        sessionRepository.device = bootDevice
        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.FirmwareBoot(bootDevice), viewModel.state.value.session)
        assertNull(viewModel.state.value.device)

        link.value = DeviceLinkState.DISCONNECTED
        advanceUntilIdle()
        assertNull(viewModel.state.value.device)
        sessionRepository.device = device
        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)
        assertEquals(device, viewModel.state.value.device)
    }

    @Test
    fun `boot content is shown for the bootloader and stays through a link drop until the block is back`() =
        runTest(dispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            assertFalse(viewModel.state.value.showBootContent)
            assertFalse(viewModel.state.value.wasBoot)

            link.value = DeviceLinkState.DISCONNECTED
            sessionRepository.device = device.copy(mode = DeviceOperationMode.BOOT)
            advanceUntilIdle()
            // Потеря связи в рабочем режиме — обычный оверлей, не экран загрузчика.
            assertFalse(viewModel.state.value.showBootContent)

            link.value = DeviceLinkState.READY
            advanceUntilIdle()
            assertTrue(viewModel.state.value.wasBoot)
            assertTrue(viewModel.state.value.showBootContent)

            // Обрыв в загрузчике (например, при передаче) — решает экран загрузчика, а не оверлей хоста.
            link.value = DeviceLinkState.DISCONNECTED
            advanceUntilIdle()
            assertEquals(DeviceSessionState.Disconnected, viewModel.state.value.session)
            assertTrue(viewModel.state.value.showBootContent)

            sessionRepository.device = device
            link.value = DeviceLinkState.READY
            advanceUntilIdle()
            assertFalse(viewModel.state.value.wasBoot)
            assertFalse(viewModel.state.value.showBootContent)
        }

    @Test
    fun `expected reboot to the bootloader shows boot content before the link drops`() = runTest(dispatcher) {
        val session = session()
        val viewModel = viewModel(session)
        advanceUntilIdle()

        session.expectReboot(DeviceReboot.ToBoot)
        advanceUntilIdle()
        assertEquals(DeviceReboot.ToBoot, viewModel.state.value.reboot)
        assertTrue(viewModel.state.value.showBootContent)
        // Рабочий flow остаётся под экраном: если команда не удалась, пользователь вернётся туда же.
        assertEquals(device, viewModel.state.value.device)

        session.cancelReboot()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.showBootContent)
    }

    @Test
    fun `reboot to main keeps boot content after the block returns until it is acknowledged`() = runTest(dispatcher) {
        sessionRepository.device = device.copy(mode = DeviceOperationMode.BOOT)
        val session = session()
        val viewModel = viewModel(session)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.showBootContent)

        session.expectReboot(DeviceReboot.ToMain)
        link.value = DeviceLinkState.DISCONNECTED
        sessionRepository.device = device
        link.value = DeviceLinkState.READY
        advanceUntilIdle()
        assertEquals(DeviceSessionState.Ready(device), viewModel.state.value.session)
        assertTrue(viewModel.state.value.showBootContent)

        session.acknowledgeReboot()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.showBootContent)
        assertEquals(device, viewModel.state.value.device)
    }
}
