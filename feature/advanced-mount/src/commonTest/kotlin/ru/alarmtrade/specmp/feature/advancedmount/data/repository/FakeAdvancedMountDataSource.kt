package ru.alarmtrade.specmp.feature.advancedmount.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.common.core.serialization.IntWrapper
import ru.alarmtrade.specialist.domain.user.UserAppFeature
import ru.alarmtrade.specialist.domain.verification.VerifyDeviceState
import ru.alarmtrade.specialist.interactors.chat.requests.ConnectChatRequest
import ru.alarmtrade.specialist.interactors.deviceVerification.request.VerifyDeviceRequest
import ru.alarmtrade.specmp.feature.advancedmount.data.datasource.AdvancedMountDataSource

internal class FakeAdvancedMountDataSource : AdvancedMountDataSource {
    var verifyResult: SpecialistResponse<VerifyDeviceState> = SpecialistResponse.Success(VerifyDeviceState(true, ""))
    val unreadCounts = ArrayDeque<SpecialistResponse<IntWrapper>>()
    var remoteMount: Flow<SpecialistResponse<Boolean>> = flow { }
    var features: SpecialistResponse<List<UserAppFeature>> = SpecialistResponse.Success(emptyList())

    val calls = mutableListOf<String>()
    val verifyRequests = mutableListOf<VerifyDeviceRequest>()

    override suspend fun verifyDevice(request: VerifyDeviceRequest): SpecialistResponse<VerifyDeviceState> {
        verifyRequests += request
        return verifyResult
    }

    override suspend fun unreadChatMessagesCount(): SpecialistResponse<IntWrapper> {
        calls += "unread"
        return unreadCounts.removeFirst()
    }

    override suspend fun openChat(request: ConnectChatRequest): SpecialistResponse<Unit> {
        calls += "openChat"
        return SpecialistResponse.Success(Unit)
    }

    override fun remoteMountAvailability(): Flow<SpecialistResponse<Boolean>> = flow {
        calls += "remoteMount"
        emitAll(remoteMount)
    }

    override suspend fun userAppFeatures(): SpecialistResponse<List<UserAppFeature>> = features
}
