package ru.alarmtrade.specmp.feature.advancedmount.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.advancedmount.domain.model.DeviceAccess
import ru.alarmtrade.specmp.feature.advancedmount.domain.repository.AdvancedMountRepository
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.VerificationInteractor
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.user.UserInteractor

class SpecialistAdvancedMountRepository(
    private val verificationInteractor: VerificationInteractor,
    private val chatInteractor: ChatInteractor,
    private val userInteractor: UserInteractor,
) : AdvancedMountRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun verify(systemId: Long, systemName: String, firmwareVersion: String): Response<DeviceAccess> {
        val request = VerifyDeviceRequest(systemId = systemId, systemName = systemName, firmwareVersion = firmwareVersion)
        return when (val response = verificationInteractor.verifyDeviceUseCase.execute(request)) {
            is SpecialistResponse.Success -> Response.Success(DeviceAccess(response.value.hasAccess, response.value.message))
            is SpecialistResponse.Error -> {
                logger.e(response.exception) { "Failed to verify device" }
                Response.Error(response.exception, response.message)
            }
        }
    }

    override suspend fun unreadChatCount(): Response<Int> =
        when (val response = chatInteractor.getUnreadMessagesCountUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(response.value.int)
            is SpecialistResponse.Error -> Response.Error(response.exception, response.message)
        }

    override fun remoteMountAvailable(): Flow<Boolean> = flow {
        // Статус удалённого монтажа приходит по каналу чата — открываем его, как старое приложение.
        chatInteractor.openChatUseCase.execute(ConnectChatRequest())
        emitAll(
            chatInteractor.isAvailableRemoteMountFlowUseCase.execute()
                .map { response -> (response as? SpecialistResponse.Success)?.value ?: false },
        )
    }
        .catch { e ->
            if (e is CancellationException) throw e
            logger.e(e) { "Remote mount availability failed" }
            emit(false)
        }

    override suspend fun isProductInfoEnabled(): Response<Boolean> =
        when (val response = userInteractor.getUserAppFeaturesUseCase.execute()) {
            is SpecialistResponse.Success -> Response.Success(UserAppFeature.PRODUCT_SEARCH in response.value)
            is SpecialistResponse.Error -> Response.Error(response.exception, response.message)
        }
}
