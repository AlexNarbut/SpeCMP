package ru.alarmtrade.specmp.feature.advancedmount.presentation.screen.root

import androidx.lifecycle.viewModelScope
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import ru.alarmtrade.specmp.core.common.AppDispatchers
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specmp.feature.advancedmount.presentation.model.MountSection
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.AlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BluetoothModule
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeResult
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.BootModeTarget
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceConnectionState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceOperationMode
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ServicePin
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceConnectionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.repository.DeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.domain.session.DeviceSession

@OptIn(ExperimentalCoroutinesApi::class)
class AdvancedMountRootViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private val device = AlarmDevice(
        systemId = 1076884189,
        systemName = "DX4G",
        firmwareVersion = "VER3.57",
        mode = DeviceOperationMode.MAIN,
        features = setOf(DeviceFeature.FIRMWARE_UPDATE, DeviceFeature.SETTINGS, DeviceFeature.CLONE),
        can = null
    )

    private class FakeSessionRepository(var device: AlarmDevice) : DeviceSessionRepository {
        val link = MutableStateFlow(DeviceLinkState.READY)
        var bootModeCount = 0
        override fun linkState(): Flow<DeviceLinkState> = link
        override suspend fun readBluetoothModule(): Response<BluetoothModule> =
            Response.Error(IllegalStateException("usb only"))
        override suspend fun readDevice(): Response<AlarmDevice> = Response.Success(device)
        override suspend fun readImei(): Response<String> = Response.Success("351756051523999")
        override suspend fun reconnect(): Response<Unit> = Response.Success(Unit)
        override suspend fun disconnect(): Response<Unit> = Response.Success(Unit)
        override suspend fun setBootMode(target: BootModeTarget): Response<BootModeResult> {
            bootModeCount++
            return Response.Success(BootModeResult.Entered)
        }
        override suspend fun exitBootMode(target: BootModeTarget): Response<Unit> = Response.Success(Unit)
        override suspend fun setServicePin(pin: ServicePin): Response<Unit> = Response.Success(Unit)
    }

    private class FakeConnectionRepository : DeviceConnectionRepository {
        override fun connectBle(deviceId: String, timeoutMillis: Long): Flow<Response<DeviceConnectionState>> =
            emptyFlow()
        override fun connectUsb(deviceId: String): Flow<Response<DeviceConnectionState>> = emptyFlow()
        override suspend fun closeSessions(): Response<Unit> = Response.Success(Unit)
    }

    private class FakeMountRepository : AdvancedMountRepository {
        var access: Response<DeviceAccess> = Response.Success(DeviceAccess(hasAccess = true, message = ""))
        var verifyCount = 0
        var unread = 0
        override suspend fun verify(
            systemId: Long,
            systemName: String,
            firmwareVersion: String
        ): Response<DeviceAccess> {
            verifyCount++
            return access
        }
        override fun observeUnreadChatCount(): Flow<Response<Int>> = flow {
            while (true) {
                emit(Response.Success(unread))
                delay(60_000)
            }
        }
        override fun remoteMountAvailable(): Flow<Boolean> = flowOf(true)
        override suspend fun isProductInfoEnabled(): Response<Boolean> = Response.Success(true)
    }

    private val sessionRepository = FakeSessionRepository(device)
    private val mountRepository = FakeMountRepository()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val viewModels = mutableListOf<AdvancedMountRootViewModel>()

    /** VM опрашивает непрочитанные бесконечно — в конце теста гасим его скоуп, иначе runTest не завершится. */
    private fun test(block: suspend TestScope.() -> Unit): TestResult = runTest(dispatcher) {
        try {
            block()
        } finally {
            viewModels.forEach { it.viewModelScope.cancel() }
        }
    }

    private fun TestScope.viewModel(): Pair<AdvancedMountRootViewModel, MutableList<AdvancedMountRootEffect>> {
        val dispatchers = AppDispatchers(dispatcher, dispatcher, dispatcher)
        val session =
            DeviceSession(sessionRepository, FakeConnectionRepository(), dispatchers, backgroundScope.coroutineContext)
        session.attach(ConnectionTransport.USB)
        val vm = AdvancedMountRootViewModel(session, mountRepository, dispatchers).also { viewModels += it }
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.state.collect {} }
        val effects = mutableListOf<AdvancedMountRootEffect>()
        backgroundScope.launch(start = CoroutineStart.UNDISPATCHED) { vm.effect.collect { effects += it } }
        runCurrent()
        return vm to effects
    }

    @Test
    fun `ready device is verified and sections are shown`(): TestResult = test {
        val (vm, _) = viewModel()
        val state = vm.state.value

        assertEquals(device, state.device)
        assertEquals(ConnectionTransport.USB, state.transport)
        assertEquals("351756051523999", state.imei)
        assertTrue(state.remoteAvailable)
        assertTrue(state.productInfo)
        val content = assertIs<MountContent.Ready>(state.content)
        assertEquals(false, content.unsupported)
        assertTrue(MountSection.CLONE in content.sections)
    }

    @Test
    fun `verify error then retry`(): TestResult = test {
        mountRepository.access = Response.Error(IllegalStateException("offline"))
        val (vm, _) = viewModel()
        assertEquals(MountContent.VerifyError, vm.state.value.content)

        mountRepository.access = Response.Success(DeviceAccess(true, ""))
        vm.onRetry()
        runCurrent()
        assertIs<MountContent.Ready>(vm.state.value.content)
    }

    @Test
    fun `denied access shows the server message`(): TestResult = test {
        mountRepository.access = Response.Success(DeviceAccess(hasAccess = false, message = "Блок заблокирован"))
        val (vm, _) = viewModel()

        assertEquals(MountContent.Denied("Блок заблокирован"), vm.state.value.content)
    }

    @Test
    fun `unread features show the unsupported menu`(): TestResult = test {
        sessionRepository.device = device.copy(features = null)
        val (vm, _) = viewModel()

        val content = assertIs<MountContent.Ready>(vm.state.value.content)
        assertTrue(content.unsupported)
        assertEquals(
            listOf(MountSection.FIRMWARE_UPDATE, MountSection.SUPPORT_CHAT, MountSection.VEHICLES),
            content.sections
        )
    }

    @Test
    fun `factory CAN locks configuration sections behind the CAN dialog`(): TestResult = test {
        sessionRepository.device = device.copy(can = CanInfo(firmwareVersion = "12.05", code = "", isFactory = true))
        val (vm, effects) = viewModel()
        assertTrue(vm.state.value.canLocked)

        vm.onSectionClick(MountSection.CLONE)
        assertEquals(MountDialog.CAN_UPDATE, vm.state.value.dialog)
        assertTrue(effects.isEmpty())

        vm.onSectionClick(MountSection.FIRMWARE_UPDATE)
        assertEquals(
            listOf<AdvancedMountRootEffect>(AdvancedMountRootEffect.OpenSection(MountSection.FIRMWARE_UPDATE)),
            effects
        )

        effects.clear()
        vm.onCanUpdateConfirm()
        runCurrent()
        assertEquals(null, vm.state.value.dialog)
        // Переход к обновлению — экран проверки прошивок (он сам запрашивает PIN и вход в загрузчик), не прямая команда.
        assertEquals(
            listOf<AdvancedMountRootEffect>(AdvancedMountRootEffect.OpenSection(MountSection.FIRMWARE_UPDATE)),
            effects
        )
        assertEquals(0, sessionRepository.bootModeCount)
    }

    @Test
    fun `chat tile opens support chat and unread count is polled`(): TestResult = test {
        mountRepository.unread = 2
        val (vm, effects) = viewModel()
        assertEquals(2, vm.state.value.unreadCount)

        mountRepository.unread = 5
        advanceTimeBy(60_001)
        runCurrent()
        assertEquals(5, vm.state.value.unreadCount)

        vm.onSectionClick(MountSection.SUPPORT_CHAT)
        assertEquals(listOf<AdvancedMountRootEffect>(AdvancedMountRootEffect.OpenSupportChat), effects)
    }

    @Test
    fun `reconnect of the same device keeps sections without re-verify`(): TestResult = test {
        val (vm, _) = viewModel()
        assertEquals(1, mountRepository.verifyCount)

        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        runCurrent()
        sessionRepository.link.value = DeviceLinkState.READY
        runCurrent()

        assertEquals(1, mountRepository.verifyCount)
        assertIs<MountContent.Ready>(vm.state.value.content)
    }
}
