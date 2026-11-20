package ru.alarmtrade.specmp.feature.mountassistant.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.network.monitor.NetworkMonitor
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.CanInfo
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.ConnectionTransport
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceFeature
import ru.alarmtrade.specmp.feature.device.interaction.domain.model.DeviceLinkState
import ru.alarmtrade.specmp.feature.device.interaction.testing.FakeDeviceSessionRepository
import ru.alarmtrade.specmp.feature.device.interaction.testing.testAlarmDevice
import ru.alarmtrade.specmp.feature.device.interaction.testing.testDeviceSession
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantCapability
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.AssistantTransport
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.CanFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.DeviceFacts
import ru.alarmtrade.specmp.feature.mountassistant.domain.model.TodayPassport
import ru.alarmtrade.specmp.feature.mountassistant.domain.repository.AssistantPassportRepository
import ru.alarmtrade.specmp.feature.mountassistant.testing.FakeAssistantProductRepository

class AssistantDeviceRepositoryImplTest {
    private class FakeNetworkMonitor : NetworkMonitor {
        val online = MutableStateFlow(true)
        override val isAvailable: StateFlow<Boolean> = online
    }

    private val device = testAlarmDevice.copy(
        features = setOf(DeviceFeature.SETTINGS, DeviceFeature.CLONE, DeviceFeature.VOICE_FIRMWARE),
        can = CanInfo("12.05", "1234", isFactory = false),
    )
    private val sessionRepository = FakeDeviceSessionRepository(device)
    private val network = FakeNetworkMonitor()

    private val products = FakeAssistantProductRepository(enabled = false)
    private val passports = object : AssistantPassportRepository {
        val requests = mutableListOf<Long>()

        override suspend fun today(systemId: Long): Response<TodayPassport?> {
            requests += systemId
            return Response.Success(TodayPassport("p1", "20261119-1", issued = true))
        }
    }

    private fun TestScope.repository(monitor: NetworkMonitor? = network) = AssistantDeviceRepositoryImpl(
        testDeviceSession(
            sessionRepository,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
            transport = ConnectionTransport.USB,
        ),
        monitor,
        products,
        passports,
    )

    private suspend fun AssistantDeviceRepositoryImpl.ready(): DeviceFacts? =
        (facts().filter { (it as Response.Success).value != null }.first() as Response.Success).value

    @Test
    fun `ready device maps to facts`() = runTest {
        val facts = repository().ready()
        assertEquals(
            DeviceFacts(
                systemId = 77,
                title = "DX4G  77",
                firmwareVersion = "3.57",
                transport = AssistantTransport.USB,
                capabilities = setOf(AssistantCapability.SETTINGS, AssistantCapability.CLONE),
                can = CanFacts(factory = false, code = "1234", version = "12.05"),
                online = true,
            ),
            facts,
        )
    }

    @Test
    fun `model missing from the database has no capabilities, network follows the monitor`() = runTest {
        sessionRepository.device = device.copy(modelInDatabase = false)
        network.online.value = false
        val facts = repository().ready()
        assertNull(facts?.capabilities)
        assertEquals(false, facts?.online)
    }

    @Test
    fun `without a network monitor the network counts as available`() = runTest {
        assertEquals(true, repository(monitor = null).ready()?.online)
    }

    @Test
    fun `lost link gives no facts`() = runTest {
        val repository = repository()
        repository.ready()
        sessionRepository.link.value = DeviceLinkState.DISCONNECTED
        assertNull((repository.facts().first() as Response.Success).value)
    }

    @Test
    fun `product search follows the account feature`() = runTest {
        val repository = repository()
        assertEquals(false, repository.ready()?.productSearch)

        products.enabled.value = true

        assertEquals(
            true,
            repository.facts().first {
                (it as Response.Success).value?.productSearch == true
            }.let { true },
        )
    }

    @Test
    fun `today passport comes from the passport port`() = runTest {
        val passport = assertIs<Response.Success<TodayPassport?>>(repository().passportToday(77)).value

        assertEquals("20261119-1", passport?.number)
        assertEquals(listOf(77L), passports.requests)
    }
}
