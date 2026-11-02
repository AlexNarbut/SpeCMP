package ru.alarmtrade.specmp.feature.fastmount.ui.presentation.screen.master

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession
import ru.alarmtrade.specmp.feature.fastmount.domain.model.MountDeviceClass
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeConnectionRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeFastMountRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.FakeSessionRepository
import ru.alarmtrade.specmp.feature.fastmount.ui.testDevice

@OptIn(ExperimentalCoroutinesApi::class)
class FastMountViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val sessionRepository = FakeSessionRepository()
    private val repository = FakeFastMountRepository()
    private val viewModels = mutableListOf<FastMountViewModel>()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    // USB: без цикла переподключения BLE — связь в тесте переключает сам тест.
    private fun TestScope.viewModel(): FastMountViewModel {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        session.attach(ConnectionTransport.USB)
        val vm = FastMountViewModel(session, repository, dispatchers).also { viewModels += it }
        runCurrent()
        return vm
    }

    private fun TestScope.reconnect(systemId: Long) {
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        sessionRepository.device = testDevice(systemId)
        sessionRepository.link.value = DeviceLinkState.READY
        runCurrent()
    }

    @Test
    fun `alarm unit is fixed and ready`() = test {
        val vm = viewModel()
        assertEquals(DeviceCheck.Ready, vm.state.value.check)
        assertEquals(77, vm.state.value.fixed?.systemId)
    }

    @Test
    fun `microbot and other units are not supported`() = test {
        repository.deviceClass = Response.Success(MountDeviceClass.MICROBOT)
        assertEquals(DeviceCheck.Unsupported(UnsupportedReason.MICROBOT), viewModel().state.value.check)
    }

    @Test
    fun `unit without model in database is not supported without a server request`() = test {
        sessionRepository.device = testDevice(modelInDatabase = false)
        val vm = viewModel()
        assertEquals(DeviceCheck.Unsupported(UnsupportedReason.NOT_IN_DATABASE), vm.state.value.check)
        assertEquals(0, repository.classRequests)
    }

    @Test
    fun `failed check can be retried`() = test {
        repository.deviceClass = Response.Error(IllegalStateException())
        val vm = viewModel()
        assertIs<DeviceCheck.Failed>(vm.state.value.check)

        repository.deviceClass = Response.Success(MountDeviceClass.ALARM)
        vm.onRetry()
        runCurrent()
        assertEquals(DeviceCheck.Ready, vm.state.value.check)
    }

    @Test
    fun `another unit after reconnect stops the wizard until the fixed one returns`() = test {
        val vm = viewModel()
        reconnect(systemId = 99)
        val wrong = assertIs<DeviceCheck.WrongDevice>(vm.state.value.check)
        assertEquals(testDevice(99).systemName + "  99", wrong.connectedTitle)
        assertEquals(77, vm.state.value.fixed?.systemId)

        reconnect(systemId = 77)
        assertEquals(DeviceCheck.Ready, vm.state.value.check)
        // Тот же блок вернулся — класс не перепроверяется.
        assertEquals(1, repository.classRequests)
    }
}
