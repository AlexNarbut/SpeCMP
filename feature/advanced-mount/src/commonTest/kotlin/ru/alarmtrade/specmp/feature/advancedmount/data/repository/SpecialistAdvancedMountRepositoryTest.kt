package ru.alarmtrade.specmp.feature.advancedmount.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import ru.alarmtrade.common.core.exceptions.NetworkIsNotAvailableException
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.IntWrapper
import ru.alarmtrade.specialist.domain.errors.TokenIsExpired
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.verification.VerifyDeviceState
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specmp.core.common.ApiError
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.appError
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess

class SpecialistAdvancedMountRepositoryTest {
    private val dataSource = FakeAdvancedMountDataSource()
    private val repository = SpecialistAdvancedMountRepository(dataSource)

    @Test
    fun `verify sends the device and maps the access`() = runTest {
        dataSource.verifyResult = SpecialistResponse.Success(VerifyDeviceState(hasAccess = false, message = "Чужой"))

        val result = repository.verify(systemId = 42, systemName = "DX-9X", firmwareVersion = "2.1")

        assertEquals(Response.Success(DeviceAccess(hasAccess = false, message = "Чужой")), result)
        assertEquals(listOf(VerifyDeviceRequest(42, "DX-9X", "2.1")), dataSource.verifyRequests)
    }

    @Test
    fun `failed verification keeps the SDK exception`() = runTest {
        val failure = TokenIsExpired("expired")
        dataSource.verifyResult = SpecialistResponse.Error.General(failure)

        assertSame(failure, assertIs<Response.Error>(repository.verify(42, "DX-9X", "2.1")).exception)
    }

    @Test
    fun `unread count is polled once a minute and errors do not stop polling`() = runTest {
        dataSource.unreadCounts += SpecialistResponse.Success(IntWrapper(3))
        dataSource.unreadCounts += SpecialistResponse.Error.Network(NetworkIsNotAvailableException())
        dataSource.unreadCounts += SpecialistResponse.Success(IntWrapper(0))

        val counts = repository.observeUnreadChatCount().take(3).toList()

        assertEquals(Response.Success(3), counts[0])
        assertEquals(ApiError.NoConnection, assertIs<Response.Error>(counts[1]).exception.appError)
        assertEquals(Response.Success(0), counts[2])
        assertEquals(120_000L, currentTime)
    }

    @Test
    fun `remote mount opens the chat before listening to availability`() = runTest {
        dataSource.remoteMount = flowOf(SpecialistResponse.Success(false), SpecialistResponse.Success(true))

        val values = repository.remoteMountAvailable().toList()

        assertEquals(listOf<Response<Boolean>>(Response.Success(false), Response.Success(true)), values)
        assertEquals(listOf("openChat", "remoteMount"), dataSource.calls)
    }

    @Test
    fun `failed availability flow ends with an error`() = runTest {
        val failure = IllegalStateException("socket closed")
        dataSource.remoteMount = flow {
            emit(SpecialistResponse.Success(true))
            throw failure
        }

        val values = repository.remoteMountAvailable().toList()

        assertEquals(Response.Success(true), values.first())
        assertSame(failure, assertIs<Response.Error>(values.last()).exception)
    }

    @Test
    fun `product info is enabled by the product search feature`() = runTest {
        dataSource.features = SpecialistResponse.Success(listOf(UserAppFeature.CAN_SCANNER))
        assertEquals(Response.Success(false), repository.isProductInfoEnabled())

        dataSource.features = SpecialistResponse.Success(listOf(UserAppFeature.PRODUCT_SEARCH))
        assertEquals(Response.Success(true), repository.isProductInfoEnabled())
    }
}
