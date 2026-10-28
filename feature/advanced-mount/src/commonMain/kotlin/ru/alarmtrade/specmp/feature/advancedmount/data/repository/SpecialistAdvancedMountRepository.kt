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
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.VerificationInteractor
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.user.UserInteractor
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.common.map
import ru.alarmtrade.specmp.core.common.onError
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository

class SpecialistAdvancedMountRepository(
    private val verificationInteractor: VerificationInteractor,
    private val chatInteractor: ChatInteractor,
    private val userInteractor: UserInteractor
) : AdvancedMountRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<DeviceAccess> {
        val request =
            VerifyDeviceRequest(systemId = systemId, systemName = systemName, firmwareVersion = firmwareVersion)
        return verificationInteractor.verifyDeviceUseCase.execute(request).toResponse()
            .onError { logger.e(it) { "Failed to verify device" } }
            .map { DeviceAccess(it.hasAccess, it.message) }
    }

    override fun observeUnreadChatCount(): Flow<Response<Int>> = flow {
        while (currentCoroutineContext().isActive) {
            emit(chatInteractor.getUnreadMessagesCountUseCase.execute().toResponse().map { it.int })
            delay(UNREAD_POLL_INTERVAL_MILLIS)
        }
    }

    override fun remoteMountAvailable(): Flow<Boolean> = flow {
        // Статус удалённого монтажа приходит по каналу чата — открываем его, как старое приложение.
        chatInteractor.openChatUseCase.execute(ConnectChatRequest())
        emitAll(
            chatInteractor.isAvailableRemoteMountFlowUseCase.execute()
                .map { response -> (response as? SpecialistResponse.Success)?.value ?: false }
        )
    }
        .catch { e ->
            if (e is CancellationException) throw e
            logger.e(e) { "Remote mount availability failed" }
            emit(false)
        }

    override suspend fun isProductInfoEnabled(): Response<Boolean> =
        userInteractor.getUserAppFeaturesUseCase.execute().toResponse().map { UserAppFeature.PRODUCT_SEARCH in it }

    private companion object {
        const val UNREAD_POLL_INTERVAL_MILLIS = 60_000L
    }
}
