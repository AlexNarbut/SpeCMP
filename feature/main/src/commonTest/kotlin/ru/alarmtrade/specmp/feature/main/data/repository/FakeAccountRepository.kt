package ru.alarmtrade.specmp.feature.main.data.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository

internal class FakeAccountRepository(var profile: Response<AccountProfile>) : AccountRepository {
    var avatarResult: Response<String> = Response.Success("https://example.com/new.jpg")
    val uploadedAvatars = mutableListOf<Triple<Int, String, String>>()

    override suspend fun currentProfile(): Response<AccountProfile> = profile

    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): Response<String> {
        uploadedAvatars += Triple(bytes.size, fileName, mimeType)
        return avatarResult
    }
}
