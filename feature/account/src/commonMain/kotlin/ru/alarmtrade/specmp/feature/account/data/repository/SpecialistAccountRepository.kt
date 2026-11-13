package ru.alarmtrade.specmp.feature.account.data.repository

import co.touchlab.kermit.Logger
import ru.alarmtrade.common.core.response.Response as SpecialistResponse
import ru.alarmtrade.specmp.core.common.Response
import ru.alarmtrade.specmp.core.specialist.sdk.toResponse
import ru.alarmtrade.specmp.feature.account.data.datasource.AccountDataSource
import ru.alarmtrade.specmp.feature.account.domain.model.AccountProfile
import ru.alarmtrade.specmp.feature.account.domain.repository.AccountRepository

internal class SpecialistAccountRepository(private val dataSource: AccountDataSource) : AccountRepository {

    private val logger = Logger.withTag(this::class.simpleName.orEmpty())

    override suspend fun currentProfile(): Response<AccountProfile> = when (val response = dataSource.currentUser()) {
        is SpecialistResponse.Success -> {
            val user = response.value
            Response.Success(
                AccountProfile(
                    name = user.userContactInformation.fullName,
                    email = user.login,
                    phone = user.userContactInformation.phoneNumber.takeIf { it.isNotBlank() },
                ),
            )
        }

        is SpecialistResponse.Error -> {
            logger.d(response.exception) { "Failed to fetch current account profile" }
            response.toResponse()
        }
    }
}
