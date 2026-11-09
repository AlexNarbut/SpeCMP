package ru.alarmtrade.specmp.feature.advancedmount.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.advancedmount.data.datasource.AdvancedMountDataSource
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository

internal class SpecialistAdvancedMountRepository(private val dataSource: AdvancedMountDataSource) :
    AdvancedMountRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<DeviceAccess> {
        val request =
            VerifyDeviceRequest(systemId = systemId, systemName = systemName, firmwareVersion = firmwareVersion)
        return dataSource.verifyDevice(request).toResponse()
            .onError { logger.e(it) { "Failed to verify device" } }
            .map { DeviceAccess(it.hasAccess, it.message) }
    }

    override fun observeUnreadChatCount(): Flow<Response<Int>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(dataSource.unreadChatMessagesCount().toResponse().map { it.int })
            delay(UNREAD_POLL_INTERVAL_MILLIS)
        }
    }

    override fun remoteMountAvailable(): Flow<Response<Boolean>> = flow {
        // Статус удалённого монтажа приходит по каналу чата — открываем его, как старое приложение.
        dataSource.openChat(ConnectChatRequest())
        emitAll(dataSource.remoteMountAvailability().map { it.toResponse() })
    }
        .catch { e ->
            if (e is CancellationException) throw e
            logger.e(e) { "Remote mount availability failed" }
            emit(Response.Error(e))
        }

    override suspend fun isProductInfoEnabled(): Response<Boolean> =
        dataSource.userAppFeatures().toResponse().map { UserAppFeature.PRODUCT_SEARCH in it }

    private companion object {
        const val UNREAD_POLL_INTERVAL_MILLIS = 60_000L
    }
}
