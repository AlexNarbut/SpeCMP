package ru.alarmtrade.specmp.feature.account.data.datasource

import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specialist.domain.user.User
import ru.alarmtrade.specialist.interactors.user.UserInteractor
import ru.alarmtrade.specialist.interactors.user.requests.UploadAvatarRequest

internal interface AccountDataSource {
    suspend fun currentUser(): SpecialistResponse<User>
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): SpecialistResponse<String>
}

internal class SpecialistAccountDataSource(private val userInteractor: UserInteractor) : AccountDataSource {
    override suspend fun currentUser(): SpecialistResponse<User> = userInteractor.getUserUseCase.execute()

    override suspend fun uploadAvatar(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): SpecialistResponse<String> =
        userInteractor.uploadAvatarUseCase.execute(UploadAvatarRequest(bytes, fileName, mimeType))
}
