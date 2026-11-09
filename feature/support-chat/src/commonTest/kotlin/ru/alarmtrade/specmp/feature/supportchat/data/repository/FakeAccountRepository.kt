package ru.alarmtrade.specmp.feature.supportchat.data.repository

import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository

internal class FakeAccountRepository(var profile: Response<AccountProfile>) : AccountRepository {
    override suspend fun currentProfile(): Response<AccountProfile> = profile
}
