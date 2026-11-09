package ru.alarmtrade.specmp.feature.advancedmount.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.IntWrapper
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.verification.VerifyDeviceState
import ru.alarmtrade.specialist.interactors.chat.ChatInteractor
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.VerificationInteractor
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specialist.interactors.user.UserInteractor

internal interface AdvancedMountDataSource {
    suspend fun verifyDevice(request: VerifyDeviceRequest): SpecialistResponse<VerifyDeviceState>
    suspend fun unreadChatMessagesCount(): SpecialistResponse<IntWrapper>
    suspend fun openChat(request: ConnectChatRequest): SpecialistResponse<Unit>
    fun remoteMountAvailability(): Flow<SpecialistResponse<Boolean>>
    suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>>
}

internal class SpecialistAdvancedMountDataSource(
    private val verificationInteractor: VerificationInteractor,
    private val chatInteractor: ChatInteractor,
    private val userInteractor: UserInteractor,
) : AdvancedMountDataSource {
    override suspend fun verifyDevice(request: VerifyDeviceRequest): SpecialistResponse<VerifyDeviceState> =
        verificationInteractor.verifyDeviceUseCase.execute(request)

    override suspend fun unreadChatMessagesCount(): SpecialistResponse<IntWrapper> =
        chatInteractor.getUnreadMessagesCountUseCase.execute()

    override suspend fun openChat(request: ConnectChatRequest): SpecialistResponse<Unit> =
        chatInteractor.openChatUseCase.execute(request)

    override fun remoteMountAvailability(): Flow<SpecialistResponse<Boolean>> = flow {
        emitAll(chatInteractor.isAvailableRemoteMountFlowUseCase.execute())
    }

    override suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>> =
        userInteractor.getUserAppFeaturesUseCase.execute()
}
